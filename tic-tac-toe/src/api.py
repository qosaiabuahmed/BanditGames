from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, List, Optional
import uuid
import os
import sys
import requests

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from src.tic_tac_toe import TicTacToe, Move, EMPTY_CELL
from src.gameplay_logger import GameplayLogger, PlayerInfo, MoveLog, DatabaseConfig
from psycopg2.extras import RealDictCursor

# AI Service configuration
AI_SERVICE_URL = os.getenv('AI_SERVICE_URL', 'http://localhost:5000')

app = FastAPI(title="Tic-Tac-Toe API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize database logger
try:
    logger = GameplayLogger()
    print("[OK] Database logger initialized")
except Exception as e:
    print(f"[WARNING] Failed to initialize database logger: {e}")
    logger = None


@app.on_event("startup")
async def startup_event():
    print("=" * 70)
    print(" TIC-TAC-TOE API - Starting Up")
    print("=" * 70)

    try:
        from game_registration import register_with_platform
        register_with_platform()
    except ImportError:
        print("  pika not installed - skipping platform registration")
        print("   (Install with: pip install pika>=1.3.2)")
    except Exception as e:
        print(f"  Platform registration failed: {e}")
        print("   Game will run in standalone mode")

    print("=" * 70)
    print(" TIC-TAC-TOE API - Ready to accept requests")
    print("=" * 70)


games: Dict[str, TicTacToe] = {}
# Store game metadata (including database IDs)
game_metadata: Dict[str, Dict] = {}
match_to_game: Dict[str, str] = {}  # match_id -> game_id




def get_legal_moves(game: TicTacToe) -> List[Dict[str, int]]:
    """Get all legal moves for the current game state."""
    legal_moves = []
    for r in range(game.size):
        for c in range(game.size):
            if game.is_cell_empty(r, c):
                legal_moves.append({"row": r, "col": c})
    return legal_moves


def log_move_to_db(game: TicTacToe, metadata: Dict, current_player: str,
                   row: int, col: int, board_before: List[List[str]],
                   board_after: List[List[str]], legal_moves: List[Dict[str, int]]):
    """Log a move to the database."""
    if not logger or "db_game_id" not in metadata:
        return

    try:
        current_player_id = (
            metadata["player_x_id"] if current_player == "x"
            else metadata["player_o_id"]
        )

        move_log = MoveLog(
            game_id=metadata["db_game_id"],
            player_id=current_player_id,
            move_number=metadata["move_count"],
            row=row,
            col=col,
            board_state_before=board_before,
            board_state_after=board_after,
            legal_moves=legal_moves
        )

        logger.log_move(move_log)
        metadata["move_count"] += 1
        print(f"[OK] Logged move {metadata['move_count']} for game {metadata['db_game_id']}")
    except Exception as e:
        print(f"[WARNING] Failed to log move: {e}")


def end_game_in_db(metadata: Dict, winner: str, final_board_state: List[List[str]]):
    """End a game in the database."""
    if not logger or "db_game_id" not in metadata:
        return

    try:
        logger.end_game(
            game_id=metadata["db_game_id"],
            winner=winner,
            final_board_state=final_board_state
        )
        print(f"[OK] Game {metadata['db_game_id']} ended - Winner: {winner}")
    except Exception as e:
        print(f"[WARNING] Failed to end game in database: {e}")


def check_game_end(game: TicTacToe, metadata: Dict, current_player: str) -> tuple:
    """
    Check if game has ended and return (winner, is_draw, message).
    Also handles database logging for game end.
    """
    winner = None
    is_draw = False
    message = None

    if game.has_winner(current_player):
        winner = current_player
        message = f"Player {current_player} wins!"
        end_game_in_db(metadata, current_player, game.board)
    elif game.is_full():
        is_draw = True
        message = "Game is a draw!"
        end_game_in_db(metadata, "draw", game.board)

    return winner, is_draw, message


class GameResponse(BaseModel):
    game_id: str
    board: List[List[str]]
    current_player: str
    size: int
    player_x_username: Optional[str] = None
    player_o_username: Optional[str] = None
    winner: Optional[str] = None
    is_draw: bool = False


class MoveRequest(BaseModel):
    row: int
    col: int


class MoveResponse(BaseModel):
    success: bool
    board: List[List[str]]
    current_player: str
    winner: Optional[str] = None
    is_draw: bool = False
    message: str


class NewGameRequest(BaseModel):
    size: int = 3
    player_x_username: str = "Guest_X"
    player_o_username: str = "Guest_O"
    game_mode: str = "pvp"  # "pvp" or "pve"
    ai_difficulty: int = 2  # 1=Easy, 2=Medium, 3=Hard
    ai_player: str = "o"  # "x" or "o" - which player is AI
    match_id: Optional[str] = None  # For platform multiplayer matchmaking


class ConfigUpdateRequest(BaseModel):
    game_type: str  # "connect4" or "tictactoe"
    difficulty: int  # 1, 2, or 3
    max_iterations: Optional[int] = None
    exploration_constant: Optional[float] = None
    mistake_probability: Optional[float] = None


@app.get("/")
def read_root():
    return {
        "message": "Tic-Tac-Toe API with AI",
        "endpoints": {
            "POST /api/game/new": "Create a new game (PvP or vs AI)",
            "GET /api/game/{game_id}": "Get game state",
            "POST /api/game/{game_id}/move": "Make a move (human player)",
            "POST /api/game/{game_id}/ai-move": "Make AI move (vs AI games only)",
            "DELETE /api/game/{game_id}": "Delete a game",
            "GET /api/games": "List all active games",
            "GET /config": "Get AI configuration for all games and difficulties",
            "PUT /config": "Update AI configuration for a specific game and difficulty"
        },
        "ai_info": {
            "game_modes": ["pvp", "pve"],
            "difficulty_levels": {
                "1": "Easy (20 iterations, 30% mistakes)",
                "2": "Medium (100 iterations, 5% mistakes)",
                "3": "Hard (500 iterations, no mistakes, tactical play)"
            }
        }
    }


@app.post("/api/game/new", response_model=GameResponse)
def create_game(request: NewGameRequest = NewGameRequest()):
    if request.match_id:
        print(f"[MULTIPLAYER] Request with match_id: {request.match_id}")

        if request.match_id in match_to_game:
            game_id = match_to_game[request.match_id]
            print(f"[MULTIPLAYER] Joining existing game {game_id} as Player O")

            # Update Player O's username
            metadata = game_metadata[game_id]
            metadata["player_o_username"] = request.player_x_username  # The second joiner becomes Player O

            # Return existing game state
            game = games[game_id]
            return GameResponse(
                game_id=game_id,
                board=game.board,
                current_player=game.current_player,
                size=game.size,
                player_x_username=metadata["player_x_username"],
                player_o_username=metadata["player_o_username"]
            )
        else:
            print(f"[MULTIPLAYER] Creating new game for match_id: {request.match_id}")
            game_id = str(uuid.uuid4())
            match_to_game[request.match_id] = game_id
    else:
        # Normal game creation
        game_id = str(uuid.uuid4())

    game = TicTacToe(size=request.size)
    games[game_id] = game

    metadata = {
        "move_count": 0,
        "player_x_username": request.player_x_username,
        "player_o_username": request.player_o_username if not request.match_id else "Waiting for opponent...",
        "game_mode": request.game_mode,
        "ai_difficulty": request.ai_difficulty,
        "ai_player": request.ai_player.lower()
    }

    if logger:
        try:
            player_x_type = "ai" if request.ai_player.lower() == "x" and request.game_mode == "pve" else "human"
            player_o_type = "ai" if request.ai_player.lower() == "o" and request.game_mode == "pve" else "human"

            player_x = PlayerInfo(username=request.player_x_username, player_type=player_x_type)
            player_o = PlayerInfo(username=request.player_o_username, player_type=player_o_type)

            player_x_id = logger.create_player(player_x)
            player_o_id = logger.create_player(player_o)

            # Create game in database
            db_game_id = logger.create_game(
                player_x_id=player_x_id,
                player_o_id=player_o_id,
                board_size=request.size,
                game_mode=request.game_mode
            )

            metadata["db_game_id"] = db_game_id
            metadata["player_x_id"] = player_x_id
            metadata["player_o_id"] = player_o_id

            print(f"[OK] Created game in database: {db_game_id}")
        except Exception as e:
            print(f"[WARNING] Failed to create game in database: {e}")

    game_metadata[game_id] = metadata

    return GameResponse(
        game_id=game_id,
        board=game.board,
        current_player=game.current_player,
        size=game.size,
        player_x_username=metadata["player_x_username"],
        player_o_username=metadata["player_o_username"]
    )


@app.get("/api/game/{game_id}", response_model=GameResponse)
def get_game_state(game_id: str):
    if game_id not in games:
        raise HTTPException(status_code=404, detail="Game not found")

    game = games[game_id]
    metadata = game_metadata.get(game_id, {})

    # Check if game has ended
    winner = None
    is_draw = False

    if game.has_winner('x'):
        winner = 'x'
    elif game.has_winner('o'):
        winner = 'o'
    elif game.is_full():
        is_draw = True

    return GameResponse(
        game_id=game_id,
        board=game.board,
        current_player=game.current_player,
        size=game.size,
        player_x_username=metadata.get("player_x_username"),
        player_o_username=metadata.get("player_o_username"),
        winner=winner,
        is_draw=is_draw
    )


@app.post("/api/game/{game_id}/move", response_model=MoveResponse)
def make_move(game_id: str, move_request: MoveRequest):
    if game_id not in games:
        raise HTTPException(status_code=404, detail="Game not found")

    game = games[game_id]
    metadata = game_metadata.get(game_id, {})
    move = Move(row=move_request.row, col=move_request.col)

    current_player = game.current_player

    # Capture board state BEFORE move for logging
    board_before = [row[:] for row in game.board]
    legal_moves = get_legal_moves(game)

    success = game.make_move(move)

    if not success:
        return MoveResponse(
            success=False,
            board=game.board,
            current_player=game.current_player,
            message="Invalid move. Cell is occupied or out of bounds."
        )

    # Capture board state AFTER move
    board_after = [row[:] for row in game.board]

    # Log move to database
    log_move_to_db(game, metadata, current_player, move_request.row, move_request.col,
                   board_before, board_after, legal_moves)

    # Check if game has ended
    winner, is_draw, end_message = check_game_end(game, metadata, current_player)

    if winner or is_draw:
        message = end_message
    else:
        game.switch_player()
        message = f"Move successful. Player {game.current_player}'s turn."

    return MoveResponse(
        success=True,
        board=game.board,
        current_player=game.current_player,
        winner=winner,
        is_draw=is_draw,
        message=message
    )



@app.delete("/api/game/{game_id}")
def delete_game(game_id: str):
    if game_id not in games:
        raise HTTPException(status_code=404, detail="Game not found")

    del games[game_id]
    if game_id in game_metadata:
        del game_metadata[game_id]
    return {"message": "Game deleted successfully"}


@app.get("/api/games")
def list_games():
    return {
        "active_games": len(games),
        "game_ids": list(games.keys())
    }


@app.get("/config")
def get_ai_config():
    """Proxy endpoint to get AI configuration from AI service."""
    try:
        response = requests.get(
            f"{AI_SERVICE_URL}/config",
            timeout=10
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=response.status_code,
                detail=f"AI service returned error: {response.text}"
            )

        return response.json()

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="AI service is not available. Please ensure the AI service is running."
        )
    except requests.exceptions.Timeout:
        raise HTTPException(
            status_code=504,
            detail="AI service timeout. The request took too long to complete."
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to retrieve config: {str(e)}")


@app.put("/config")
@app.patch("/config")
def update_ai_config(config_request: ConfigUpdateRequest):
    """Proxy endpoint to update AI configuration in AI service."""
    try:
        # Convert request to dict, excluding None values
        config_data = config_request.model_dump(exclude_none=True)

        response = requests.put(
            f"{AI_SERVICE_URL}/config",
            json=config_data,
            timeout=10
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=response.status_code,
                detail=f"AI service returned error: {response.text}"
            )

        return response.json()

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="AI service is not available. Please ensure the AI service is running."
        )
    except requests.exceptions.Timeout:
        raise HTTPException(
            status_code=504,
            detail="AI service timeout. The request took too long to complete."
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to update config: {str(e)}")


@app.post("/api/game/{game_id}/ai-move", response_model=MoveResponse)
def make_ai_move(game_id: str):
    """Get and execute AI move for the current game state."""
    if game_id not in games:
        raise HTTPException(status_code=404, detail="Game not found")

    game = games[game_id]
    metadata = game_metadata.get(game_id, {})

    # Check if this is an AI game (support both "pve" and legacy "vs_ai")
    game_mode = metadata.get("game_mode")
    if game_mode not in ["pve", "vs_ai"]:
        raise HTTPException(status_code=400, detail="This is not an AI game")

    # Check if it's the AI's turn
    ai_player = metadata.get("ai_player", "o")
    if game.current_player != ai_player:
        raise HTTPException(
            status_code=400,
            detail=f"It's not the AI's turn. Current player: {game.current_player}"
        )

    # Check if game is over
    if game.is_full() or game.has_winner("x") or game.has_winner("o"):
        raise HTTPException(status_code=400, detail="Game is already over")

    # Convert game state to AI format
    board_2d = game.board
    current_player_str = game.current_player.upper()

    # Get difficulty level
    difficulty = metadata.get("ai_difficulty", 2)

    # Prepare request for AI service
    ai_request = {
        "game_type": "tictactoe",
        "board": board_2d,
        "current_player": current_player_str,
        "difficulty": difficulty
    }

    # Call AI service to get best move
    try:
        response = requests.post(
            f"{AI_SERVICE_URL}/get-move",
            json=ai_request,
            timeout=60
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=500,
                detail=f"AI service returned error: {response.text}"
            )

        ai_result = response.json()
        best_position = ai_result['move']  # AI service returns 'move' not 'position'

        # Convert position (0-8) to row, col
        row = best_position // 3
        col = best_position % 3

        move = Move(row=row, col=col)

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="AI service is not available. Please ensure the AI service is running."
        )
    except requests.exceptions.Timeout:
        raise HTTPException(
            status_code=504,
            detail="AI service timeout. The request took too long to complete."
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI failed to calculate move: {str(e)}")

    current_player = game.current_player

    # Capture board state BEFORE move for logging
    board_before = [row[:] for row in game.board]
    legal_moves = get_legal_moves(game)

    # Make the move
    success = game.make_move(move)

    if not success:
        raise HTTPException(status_code=500, detail="AI made an invalid move")

    # Capture board state AFTER move
    board_after = [row[:] for row in game.board]

    # Log move to database
    log_move_to_db(game, metadata, current_player, row, col,
                   board_before, board_after, legal_moves)

    # Check if game has ended
    winner, is_draw, end_message = check_game_end(game, metadata, current_player)

    if winner:
        message = f"AI ({current_player.upper()}) wins!"
    elif is_draw:
        message = "Game is a draw!"
    else:
        game.switch_player()
        message = f"AI placed {current_player.upper()} at ({row}, {col}). Your turn!"

    return MoveResponse(
        success=True,
        board=game.board,
        current_player=game.current_player,
        winner=winner,
        is_draw=is_draw,
        message=message
    )



@app.get("/api/analytics/player/{username}")
def get_player_analytics(username: str):
    """Get detailed statistics for a player."""
    if not logger:
        raise HTTPException(status_code=503, detail="Database logging not available")

    try:
        player = logger.get_player(username)
        if not player:
            raise HTTPException(status_code=404, detail="Player not found")

        stats = logger.get_player_stats(player['player_id'])
        games = logger.get_player_games(player['player_id'], limit=10)

        return {
            "player": dict(stats) if stats else None,
            "recent_games": [dict(game) for game in games]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/analytics/game/{game_id}/moves")
def get_game_moves_history(game_id: str):
    """Get all moves for a specific game."""
    if not logger:
        raise HTTPException(status_code=503, detail="Database logging not available")

    if game_id not in game_metadata:
        raise HTTPException(status_code=404, detail="Game not found")

    metadata = game_metadata[game_id]
    if "db_game_id" not in metadata:
        raise HTTPException(status_code=404, detail="Game not logged in database")

    try:
        moves = logger.get_game_moves(metadata["db_game_id"])
        return {
            "game_id": game_id,
            "db_game_id": metadata["db_game_id"],
            "moves": [dict(move) for move in moves]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/analytics/leaderboard")
def get_leaderboard(limit: int = 10):
    """Get player leaderboard."""
    if not logger:
        raise HTTPException(status_code=503, detail="Database logging not available")

    try:
        with logger.get_connection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute("""
                    SELECT * FROM tictactoe.v_player_leaderboard
                    LIMIT %s
                """, (limit,))
                leaderboard = cur.fetchall()

        return {
            "leaderboard": [dict(player) for player in leaderboard]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8189)
