"""
FastAPI Backend for Connect Four Game

Provides REST API endpoints for:
- Game creation and management
- Move execution
- Game state retrieval (including MCTS-formatted state)
- Move history access
"""

from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import Response
from pydantic import BaseModel, Field, field_validator
from typing import List, Optional, Dict, Any
from enum import Enum
import json
from datetime import datetime
import requests
import os
import numpy as np
import asyncio
import psycopg2
from psycopg2.extras import Json
import pandas as pd
from io import StringIO

from src.gameLogic.connect_four import GameEngine, Player, GameConfig, GameState
from src.logs.gameplay_logger import GameplayLogger, PlayerInfo, MoveLog
from src.utils.utils import calculate_difficulty

# Environment variable to enable/disable prediction logging
ENABLE_PREDICTION_LOGGING = os.getenv("ENABLE_PREDICTION_LOGGING", "false").lower() == "true"


# Pydantic Models for API
class PlayerType(str, Enum):
    HUMAN = "human"
    AI_MCTS = "ai_mcts"
    AI_EASY = "ai_easy"
    AI_HARD = "ai_hard"
    ML_MODEL = "ml_model"
    AI = "ai" # Keep for backward compatibility if needed


class CreateGameRequest(BaseModel):
    player_x_username: str = Field(..., min_length=1, max_length=50)
    player_o_username: str = Field(..., min_length=1, max_length=50)
    player_x_type: PlayerType = PlayerType.HUMAN
    player_o_type: PlayerType = PlayerType.HUMAN
    ai_algorithm: Optional[str] = None
    ai_difficulty: Optional[int] = Field(None, ge=1, le=10)
    rows: int = Field(6, ge=4, le=10)
    cols: int = Field(7, ge=4, le=10)
    connect_length: int = Field(4, ge=3, le=6)
    match_id: Optional[str] = Field(None, description="Platform matchId for multiplayer games")


class MakeMoveRequest(BaseModel):
    column: int = Field(..., ge=0, le=6, description="Column to play (0-6)")
    player: str = Field(..., description="Player making the move: 'X' or 'O'")

    @field_validator('player')
    @classmethod
    def validate_player(cls, v):
        if v not in ['X', 'O']:
            raise ValueError('Player must be "X" or "O"')
        return v


class GameResponse(BaseModel):
    game_id: int
    player_x_id: int
    player_o_id: int
    player_x_username: str
    player_o_username: str
    current_player: str
    current_turn: int
    score_x: int
    score_o: int
    game_over: bool
    winner: Optional[str] = None
    board: List[List[str]]
    valid_moves: List[int]


class MCTSStateResponse(BaseModel):
    """Game state formatted for MCTS AI consumption."""
    board: List[List[str]]
    score_x: int
    score_o: int
    current_player: str
    column_positions: List[int]
    rows: int
    cols: int
    connect_length: int
    valid_moves: List[int]
    game_over: bool


class MoveResponse(BaseModel):
    move_id: int
    game_id: int
    column_played: int
    row_placed: int
    player: str
    score_x: int
    score_o: int
    board_after: List[List[str]]
    valid_moves: List[int]
    game_over: bool
    winner: Optional[str] = None
    ai_recommendation: Optional[Dict[str, Any]] = None  # MCTS suggestion for next move


class MoveHistoryItem(BaseModel):
    move_number: int
    column_played: int
    row_placed: int
    player: str
    score_x_before: int
    score_o_before: int
    score_x_after: int
    score_o_after: int


class AIRecommendationResponse(BaseModel):
    """Response from AI move recommendation."""
    column: int
    confidence: float
    nodes_explored: int
    computation_time_ms: int
    player: str


# Initialize FastAPI app
app = FastAPI(
    title="Connect Four API",
    description="REST API for Connect Four game with MCTS AI support",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure this properly in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory storage for active games (use Redis/database in production)
active_games: Dict[int, GameEngine] = {}

# Store game metadata including player types
game_metadata: Dict[int, Dict[str, Any]] = {}

# Store matchId to gameId mapping for multiplayer sessions
match_to_game: Dict[str, int] = {}

# Try to initialize database logger, but continue without it if it fails
try:
    # Force IPv4 by using DatabaseConfig with explicit host
    from src.logs.gameplay_logger import DatabaseConfig
    import os

    db_config = DatabaseConfig()
    # Force IPv4 connection
    if db_config.host == 'localhost':
        db_config.host = '127.0.0.1'

    logger = GameplayLogger(config=db_config)
    print("✓ Database connection successful")
except Exception as e:
    print(f"⚠ Warning: Database connection failed: {e}")
    print("⚠ Running in TEST MODE without database logging")
    logger = None

# Get AI Service URL from environment (defaults to localhost for dev)
AI_SERVICE_URL = os.getenv('AI_SERVICE_URL', 'http://localhost:5000')
print(f"✓ AI Service URL: {AI_SERVICE_URL}")

# Counter for game IDs when running without database
next_game_id = 1
next_player_id = 1

@app.on_event("startup")
async def startup_event():
    print("=" * 70)
    print(" CONNECT FOUR API - Starting Up")
    print("=" * 70)

    try:
        from src.game_registration import register_with_platform
        register_with_platform()
    except ImportError:
        print("  pika not installed - skipping platform registration")
        print("   (Install with: pip install pika>=1.3.2)")
    except Exception as e:
        print(f"  Platform registration failed: {e}")
        print("   Game will run in standalone mode")

    print("=" * 70)
    print(" CONNECT FOUR API - Ready to accept requests")
    print("=" * 70)


@app.get("/")
def root():
    """Health check endpoint."""
    return {
        "service": "Connect Four API",
        "status": "running",
        "version": "1.0.0"
    }


@app.post("/api/games", response_model=GameResponse, status_code=status.HTTP_201_CREATED)
def create_game(request: CreateGameRequest):
    """
    Create a new Connect Four game or retrieve existing game for matchId.

    If match_id is provided and a game already exists for it, returns that game.
    Otherwise creates a new game and stores the matchId mapping.

    Returns the initial game state with game_id.
    """
    global next_game_id, next_player_id

    try:
        # Check if game already exists for this matchId (multiplayer)
        if request.match_id and request.match_id in match_to_game:
            existing_game_id = match_to_game[request.match_id]
            if existing_game_id in active_games:
                print(f"[MULTIPLAYER] Second player joining game {existing_game_id} for matchId {request.match_id}")
                # Return existing game state
                game = active_games[existing_game_id]
                state = game.get_state()
                valid_moves = [col for col in range(game.config.cols) if game.board.is_valid_column(col)]
                board_str = [[cell.value for cell in row] for row in state.board]

                # Get usernames from metadata
                metadata = game_metadata.get(existing_game_id, {})
                player_x_username = metadata.get('player_x_username', 'Player X')

                # Update Player O username when second player joins
                # Player O's username is in request.player_x_username (since they think they're X on their end)
                player_o_username = request.player_x_username
                game_metadata[existing_game_id]['player_o_username'] = player_o_username
                print(f"[MULTIPLAYER] Updated Player O username to: {player_o_username}")

                return GameResponse(
                    game_id=existing_game_id,
                    player_x_id=game.player_x_id,
                    player_o_id=game.player_o_id,
                    player_x_username=player_x_username,
                    player_o_username=player_o_username,
                    current_player=state.current_player.value,
                    current_turn=state.current_turn,
                    score_x=state.score_x,
                    score_o=state.score_o,
                    game_over=game.is_game_over(),
                    winner=None,
                    board=board_str,
                    valid_moves=valid_moves
                )
        # Create game configuration
        config = GameConfig(
            rows=request.rows,
            cols=request.cols,
            connect_length=request.connect_length
        )

        if logger:
            # DATABASE MODE: Use database for persistence
            player_x_info = PlayerInfo(
                username=request.player_x_username,
                player_type=request.player_x_type.value,
                ai_algorithm=request.ai_algorithm if request.player_x_type == PlayerType.AI_MCTS else None,
                ai_difficulty=request.ai_difficulty if request.player_x_type == PlayerType.AI_MCTS else None
            )

            player_o_info = PlayerInfo(
                username=request.player_o_username,
                player_type=request.player_o_type.value,
                ai_algorithm=request.ai_algorithm if request.player_o_type == PlayerType.AI_MCTS else None,
                ai_difficulty=request.ai_difficulty if request.player_o_type == PlayerType.AI_MCTS else None
            )

            # Create players in database
            player_x_id = logger.create_player(player_x_info)
            player_o_id = logger.create_player(player_o_info)

            # Determine game mode
            if player_x_info.player_type == "ai" and player_o_info.player_type == "ai":
                game_mode = "ai_vs_ai"
            elif player_x_info.player_type == "ai" or player_o_info.player_type == "ai":
                game_mode = "pve"
            else:
                game_mode = "pvp"

            # Create game in database
            game_id = logger.create_game(
                player_x_id=player_x_id,
                player_o_id=player_o_id,
                rows=config.rows,
                cols=config.cols,
                game_mode=game_mode
            )
        else:
            # TEST MODE: Use in-memory IDs
            player_x_id = next_player_id
            next_player_id += 1
            player_o_id = next_player_id
            next_player_id += 1
            game_id = next_game_id
            next_game_id += 1

        # Initialize game engine with the game_id
        game_engine = GameEngine(config=config, logger=logger, game_id=game_id)
        game_engine.player_x_id = player_x_id
        game_engine.player_o_id = player_o_id
        # Store in active games
        active_games[game_id] = game_engine

        # Store game metadata (player types AND usernames)
        game_metadata[game_id] = {
            'player_x_type': request.player_x_type.value,
            'player_o_type': request.player_o_type.value,
            'player_x_username': request.player_x_username,
            'player_o_username': request.player_o_username,
            'ai_algorithm': request.ai_algorithm,
            'ai_difficulty': request.ai_difficulty
        }
        print(f"DEBUG: Created game {game_id} with metadata: {game_metadata[game_id]}")

        # Store matchId mapping for multiplayer
        if request.match_id:
            match_to_game[request.match_id] = game_id
            print(f"[MULTIPLAYER] Stored matchId {request.match_id} -> gameId {game_id}")

        # Get current state
        state = game_engine.get_state()

        # Get valid moves
        valid_moves = [col for col in range(config.cols) if game_engine.board.is_valid_column(col)]

        # Convert board to string format
        board_str = [[cell.value for cell in row] for row in state.board]

        return GameResponse(
            game_id=game_id,
            player_x_id=game_engine.player_x_id,
            player_o_id=game_engine.player_o_id,
            player_x_username=request.player_x_username,
            player_o_username=request.player_o_username,
            current_player=state.current_player.value,
            current_turn=state.current_turn,
            score_x=state.score_x,
            score_o=state.score_o,
            game_over=game_engine.is_game_over(),
            winner=None,
            board=board_str,
            valid_moves=valid_moves
        )

    except Exception as e:
        import traceback
        print("--- ERROR IN CREATE_GAME ---")
        traceback.print_exc()
        print("--------------------------")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to create game: {str(e)}"
        )


@app.get("/api/games/{game_id}", response_model=GameResponse)
def get_game(game_id: int):
    """
    Get current state of a game.
    """
    if game_id not in active_games:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Game {game_id} not found"
        )

    game = active_games[game_id]
    state = game.get_state()

    # Get valid moves
    valid_moves = [col for col in range(game.config.cols) if game.board.is_valid_column(col)]

    # Convert board
    board_str = [[cell.value for cell in row] for row in state.board]

    # Determine winner if game is over
    winner = None
    if game.is_game_over():
        winner = game.get_winner()

    # Get usernames from metadata
    metadata = game_metadata.get(game_id, {})
    player_x_username = metadata.get('player_x_username', 'Player X')
    player_o_username = metadata.get('player_o_username', 'Player O')

    return GameResponse(
        game_id=game_id,
        player_x_id=game.player_x_id,
        player_o_id=game.player_o_id,
        player_x_username=player_x_username,
        player_o_username=player_o_username,
        current_player=state.current_player.value,
        current_turn=state.current_turn,
        score_x=state.score_x,
        score_o=state.score_o,
        game_over=game.is_game_over(),
        winner=winner,
        board=board_str,
        valid_moves=valid_moves
    )


@app.get("/api/games/{game_id}/mcts-state", response_model=MCTSStateResponse)
def get_mcts_state(game_id: int):
    """
    Get game state in format expected by MCTS AI.

    This endpoint formats the game state exactly as the MCTS algorithm expects,
    matching the structure in the test script.
    """
    if game_id not in active_games:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Game {game_id} not found"
        )

    game = active_games[game_id]
    state = game.get_state()

    # Get valid moves
    valid_moves = [col for col in range(game.config.cols) if game.board.is_valid_column(col)]

    # Convert board to string format (matching MCTS expectations)
    board_str = [[cell.value for cell in row] for row in state.board]

    return MCTSStateResponse(
        board=board_str,
        score_x=state.score_x,
        score_o=state.score_o,
        current_player=state.current_player.value,
        column_positions=state.column_positions.copy(),
        rows=game.config.rows,
        cols=game.config.cols,
        connect_length=game.config.connect_length,
        valid_moves=valid_moves,
        game_over=game.is_game_over()
    )


@app.post("/api/games/{game_id}/moves", response_model=MoveResponse, status_code=status.HTTP_201_CREATED)
def make_move(game_id: int, request: MakeMoveRequest):
    """
    Make a move in the game.

    Executes the move, updates scores, logs to database, and returns new state.
    If Player X makes a move and the game is not over, Player O (AI) will automatically make its move.
    """
    if game_id not in active_games:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Game {game_id} not found"
        )

    game = active_games[game_id]

    # Validate it's the correct player's turn
    current_player = game.get_current_player()
    if current_player.value != request.player:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Not {request.player}'s turn. Current player is {current_player.value}"
        )

    # Validate column is valid
    if not game.board.is_valid_column(request.column):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid move: Column {request.column} is full or out of range"
        )

    # Make the move
    success = game.make_move(request.column)

    if not success:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Failed to make move in column {request.column}"
        )

    # Get updated state
    state = game.get_state()

    # Get the last move details from move history
    if state.move_history:
        move_number = len(state.move_history) - 1
        column_played = state.move_history[-1]

        # Find the row where the piece was placed
        row_placed = None
        for row in range(game.config.rows):
            if state.board[row][column_played] != Player.NONE:
                row_placed = row
                break
    else:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Move was made but history is empty"
        )

    # AUTO-PLAY AI MOVE FOR PLAYER O (only if Player O is AI)
    # If game is not over and it's now Player O's turn, and Player O is AI, automatically make AI move
    metadata = game_metadata.get(game_id, {})
    player_o_type = metadata.get('player_o_type', 'human')  # Default to human for backward compatibility

    print(f"DEBUG: Game {game_id} - Metadata: {metadata}")
    print(f"DEBUG: Player O Type: {player_o_type}")
    print(f"DEBUG: Current Player: {game.get_current_player()}")
    print(f"DEBUG: Will auto-play AI? {not game.is_game_over() and game.get_current_player() == Player.O and player_o_type == 'ai_mcts'}")

    if not game.is_game_over() and game.get_current_player() == Player.O and player_o_type == 'ai_mcts':
        try:
            # Get valid moves
            valid_moves = [col for col in range(game.config.cols) if game.board.is_valid_column(col)]

            # Convert board for AI service
            board_str = [[cell.value for cell in row] for row in state.board]

            # Get Player X's rating for difficulty calculation
            # (AI should adapt to the human player's skill level)
            player_x_rating = 1000  # Default rating
            if logger and game.player_x_id:
                rating = logger.get_player_score(game.player_x_id)
                if rating:
                    player_x_rating = rating

            # Calculate difficulty based on Player X's rating and in-game score
            difficulty = calculate_difficulty(player_x_rating, state.score_x, state.score_o)

            # Prepare game state for MCTS service
            mcts_state = {
                "board": board_str,
                "score_x": state.score_x,
                "score_o": state.score_o,
                "current_player": state.current_player.value,
                "column_positions": state.column_positions.copy(),
                "difficulty": difficulty
            }

            # Call MCTS service to get AI move
            mcts_response = requests.post(
                f"{AI_SERVICE_URL}/get-move",
                json=mcts_state,
                timeout=60
            )

            if mcts_response.status_code == 200:
                ai_result = mcts_response.json()
                ai_column = ai_result['column']

                # Execute AI's move
                game.make_move(ai_column)

                # Update state after AI move
                state = game.get_state()

                print(f"AI (Player O) automatically played column {ai_column}")

        except requests.exceptions.ConnectionError:
            print("Warning: AI service not available - Player O move skipped")
        except requests.exceptions.Timeout:
            print("Warning: AI service timeout - Player O move skipped")
        except Exception as e:
            print(f"Warning: AI move failed: {e}")

    # Get final state after all moves
    state = game.get_state()

    # Get valid moves after this move
    valid_moves = [col for col in range(game.config.cols) if game.board.is_valid_column(col)]

    # Convert board
    board_str = [[cell.value for cell in row] for row in state.board]

    # Determine winner if game is over
    winner = None
    if game.is_game_over():
        winner = game.get_winner()
        if logger:
            try:
                from src.logs.gameplay_logger import board_to_serializable
                final_board_state = board_to_serializable(game.get_state().board)
                logger.end_game(
                    game_id=game_id,
                    winner=winner,
                    final_score_x=state.score_x,
                    final_score_o=state.score_o,
                    final_board_state=final_board_state
                )
            except Exception as e:
                print(f"Warning: Failed to log end of game: {e}")
    
    if logger:
        print("Logger is available, move should be logged.")
    else:
        print("Logger is not available, running in test mode.")

    # AUTOMATICALLY CALL MCTS FOR NEXT MOVE RECOMMENDATION (only in PvE mode)
    ai_recommendation = None

    # Only call MCTS automatically in PvE mode (when at least one player is AI)
    # For PvP mode, hints are only available via the explicit /ai-move endpoint
    player_x_type = metadata.get('player_x_type', 'human')
    is_pve_mode = player_x_type == 'ai_mcts' or player_o_type == 'ai_mcts'

    if not game.is_game_over() and game.get_current_player() == Player.X and is_pve_mode:
        try:
            # Get current player's rating for difficulty calculation
            current_player_rating = 1000  # Default rating
            if logger:
                if state.current_player.value == 'X' and game.player_x_id:
                    rating = logger.get_player_score(game.player_x_id)
                    if rating:
                        current_player_rating = rating
                elif state.current_player.value == 'O' and game.player_o_id:
                    rating = logger.get_player_score(game.player_o_id)
                    if rating:
                        current_player_rating = rating

            # Calculate difficulty based on player rating and in-game score
            difficulty = calculate_difficulty(current_player_rating, state.score_x, state.score_o)

            # Prepare game state in EXACT format MCTS expects
            mcts_state = {
                "board": board_str,
                "score_x": state.score_x,
                "score_o": state.score_o,
                "current_player": state.current_player.value,
                "column_positions": state.column_positions.copy(),
                "difficulty": difficulty
            }

            # Call MCTS service (timeout 60s for AI calculation)
            mcts_response = requests.post(
                f"{AI_SERVICE_URL}/get-move",
                json=mcts_state,
                timeout=60
            )

            if mcts_response.status_code == 200:
                ai_recommendation = mcts_response.json()
                # Add which player this recommendation is for
                ai_recommendation['recommended_for_player'] = state.current_player.value

        except requests.exceptions.ConnectionError:
            # MCTS service not running - that's OK, continue without it
            pass
        except requests.exceptions.Timeout:
            # MCTS took too long - continue without it
            pass
        except Exception as e:
            # Any other error - log but don't fail the move
            print(f"Warning: MCTS call failed: {e}")

    # Note: move_id would come from database, using turn number as placeholder
    return MoveResponse(
        move_id=state.current_turn,
        game_id=game_id,
        column_played=column_played,
        row_placed=row_placed,
        player=request.player,
        score_x=state.score_x,
        score_o=state.score_o,
        board_after=board_str,
        valid_moves=valid_moves,
        game_over=game.is_game_over(),
        winner=winner,
        ai_recommendation=ai_recommendation  # Include MCTS recommendation
    )


@app.get("/api/games/{game_id}/moves", response_model=List[MoveHistoryItem])
def get_move_history(game_id: int):
    """
    Get complete move history for a game from the database.
    """
    if game_id not in active_games:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Game {game_id} not found"
        )

    if not logger:
        # TEST MODE: No database, return empty history
        return []

    try:
        moves = logger.get_game_moves(game_id)

        if not moves:
            # Game exists but has no moves yet
            return []

        # Convert database records to response format
        move_history = []
        for move in moves:
            move_history.append(MoveHistoryItem(
                move_number=move['move_number'],
                column_played=move['column_played'],
                row_placed=move['row_placed'],
                player=move['player_name'],
                score_x_before=move['score_x_before'],
                score_o_before=move['score_o_before'],
                score_x_after=move['score_x_after'],
                score_o_after=move['score_o_after']
            ))

        return move_history

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve move history: {str(e)}"
        )


@app.post("/api/games/{game_id}/ai-move", response_model=AIRecommendationResponse)
def get_ai_move_recommendation(game_id: int, mcts_url: Optional[str] = None):
    """
    Get AI move recommendation from MCTS service.

    This endpoint:
    1. Gets current game state
    2. Calls MCTS AI service
    3. Returns the AI's recommended move

    Args:
        game_id: ID of the game
        mcts_url: URL of MCTS service (default: from AI_SERVICE_URL env var)

    Returns:
        AI move recommendation with confidence score
    """
    # Use environment variable if no URL provided
    if mcts_url is None:
        mcts_url = AI_SERVICE_URL

    if game_id not in active_games:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Game {game_id} not found"
        )

    game = active_games[game_id]
    state = game.get_state()

    # Get valid moves
    valid_moves = [col for col in range(game.config.cols) if game.board.is_valid_column(col)]

    # Convert board to string format for MCTS
    board_str = [[cell.value for cell in row] for row in state.board]

    # Get current player's rating for difficulty calculation
    current_player_rating = 1000  # Default rating
    if logger:
        if state.current_player.value == 'X' and game.player_x_id:
            rating = logger.get_player_score(game.player_x_id)
            if rating:
                current_player_rating = rating
        elif state.current_player.value == 'O' and game.player_o_id:
            rating = logger.get_player_score(game.player_o_id)
            if rating:
                current_player_rating = rating

    # Calculate difficulty based on player rating and in-game score
    difficulty = calculate_difficulty(current_player_rating, state.score_x, state.score_o)

    # Prepare game state for MCTS service
    mcts_state = {
        "board": board_str,
        "score_x": state.score_x,
        "score_o": state.score_o,
        "current_player": state.current_player.value,
        "column_positions": state.column_positions.copy(),
        "rows": game.config.rows,
        "cols": game.config.cols,
        "connect_length": game.config.connect_length,
        "difficulty": difficulty
    }

    # Call MCTS AI service (timeout 20s for AI calculation)
    try:
        response = requests.post(
            f"{mcts_url}/get-move",
            json=mcts_state,
            timeout=20
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"MCTS service returned error: {response.status_code}"
            )

        ai_result = response.json()

        return AIRecommendationResponse(
            column=ai_result['column'],
            confidence=ai_result['confidence'],
            nodes_explored=ai_result['nodes_explored'],
            computation_time_ms=ai_result['computation_time_ms'],
            player=state.current_player.value
        )

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Could not connect to MCTS AI service. Make sure it's running on port 5000"
        )
    except requests.exceptions.Timeout:
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="MCTS service timeout"
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error calling MCTS service: {str(e)}"
        )


@app.post("/api/games/{game_id}/ai-move/execute", response_model=MoveResponse, status_code=status.HTTP_201_CREATED)
def get_and_execute_ai_move(game_id: int, mcts_url: Optional[str] = None):
    """
    Get AI move recommendation AND execute it automatically.

    This is a convenience endpoint that:
    1. Calls MCTS service to get best move
    2. Executes that move in the game
    3. Returns the updated game state

    Args:
        game_id: ID of the game
        mcts_url: URL of MCTS service (default: from AI_SERVICE_URL env var)

    Returns:
        Move response with updated game state
    """
    # Get AI recommendation
    try:
        ai_recommendation = get_ai_move_recommendation(game_id, mcts_url)
    except HTTPException:
        raise

    # Execute the move
    request_data = MakeMoveRequest(
        column=ai_recommendation.column,
        player=ai_recommendation.player
    )

    return make_move(game_id, request_data)


@app.delete("/api/games/{game_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_game(game_id: int):
    """
    Remove a game from active games (cleanup).

    Note: Game data remains in database for analytics.
    """
    if game_id in active_games:
        del active_games[game_id]
    if game_id in game_metadata:
        del game_metadata[game_id]
    return None


@app.get("/api/games", response_model=List[int])
def list_active_games():
    """
    List all active game IDs.
    """
    return list(active_games.keys())


@app.get("/api/games/{game_id}/debug")
def debug_game(game_id: int):
    """
    DEBUG ENDPOINT: Show game metadata
    """
    if game_id not in active_games:
        raise HTTPException(status_code=404, detail="Game not found")

    metadata = game_metadata.get(game_id, {})

    return {
        "game_id": game_id,
        "metadata": metadata,
        "has_metadata": game_id in game_metadata,
        "metadata_keys": list(game_metadata.keys()),
        "active_game_ids": list(active_games.keys())
    }


# =============================================================================
# SELF-PLAY AND DATA EXPORT ENDPOINTS
# =============================================================================

class SelfPlayRequest(BaseModel):
    """Request for self-play game generation."""
    num_games: int = Field(10, ge=1, le=1000, description="Number of games to generate")
    difficulty_x: int = Field(2, ge=1, le=3, description="Difficulty for Player X (1-3)")
    difficulty_o: int = Field(2, ge=1, le=3, description="Difficulty for Player O (1-3)")


class SelfPlayGameResult(BaseModel):
    """Result of a single self-play game."""
    game_id: int
    winner: str
    total_moves: int
    score_x: int
    score_o: int
    duration_seconds: float


class SelfPlayResponse(BaseModel):
    """Response from self-play session."""
    games_completed: int
    games_requested: int
    results: List[SelfPlayGameResult]
    total_moves_logged: int
    x_wins: int
    o_wins: int
    ties: int
    total_duration_seconds: float


@app.post("/api/self-play", response_model=SelfPlayResponse)
def run_self_play(request: SelfPlayRequest):
    """
    Run AI vs AI self-play games to generate training data.

    This endpoint:
    1. Creates AI players in the database
    2. Runs N games between two AI agents
    3. Logs all moves to database automatically
    4. Returns summary statistics

    Use this to generate training data for ML models.
    """
    from src.gameLogic.self_play import SelfPlayEngine, SelfPlayConfig

    config = SelfPlayConfig(
        num_games=request.num_games,
        difficulty_x=request.difficulty_x,
        difficulty_o=request.difficulty_o,
        verbose=False  # Quiet mode for API
    )

    try:
        engine = SelfPlayEngine(config, logger=logger)
        results = engine.run_session()

        # Calculate statistics
        total_moves = sum(r.total_moves for r in results)
        x_wins = sum(1 for r in results if r.winner == 'X')
        o_wins = sum(1 for r in results if r.winner == 'O')
        ties = sum(1 for r in results if r.winner == 'Tie')
        total_duration = sum(r.duration_seconds for r in results)

        return SelfPlayResponse(
            games_completed=len(results),
            games_requested=request.num_games,
            results=[
                SelfPlayGameResult(
                    game_id=r.game_id,
                    winner=r.winner,
                    total_moves=r.total_moves,
                    score_x=r.score_x,
                    score_o=r.score_o,
                    duration_seconds=r.duration_seconds
                ) for r in results
            ],
            total_moves_logged=total_moves,
            x_wins=x_wins,
            o_wins=o_wins,
            ties=ties,
            total_duration_seconds=total_duration
        )

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Self-play failed: {str(e)}"
        )


class ExportRequest(BaseModel):
    """Request for data export."""
    game_mode: Optional[str] = Field(None, description="Filter by game mode: pvp, pve, ai_vs_ai")
    limit: Optional[int] = Field(None, ge=1, description="Maximum number of games to export")


class ExportResponse(BaseModel):
    """Response from data export."""
    file_path: str
    moves_exported: int
    games_included: int
    file_size_bytes: int


@app.post("/api/export/training-data", response_model=ExportResponse)
def export_training_data(request: ExportRequest):
    """
    Export game data to Parquet file for ML training.

    Exports completed games with all moves, board states, and outcomes.
    Files are saved to the data/ directory.
    """
    import pandas as pd
    from datetime import datetime
    import os

    if not logger:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database not available for export"
        )

    try:
        # Create data directory if it doesn't exist
        data_dir = "/app/data" if os.path.exists("/app") else "data"
        os.makedirs(data_dir, exist_ok=True)

        # Generate filename with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"training_data_{timestamp}.parquet"
        filepath = os.path.join(data_dir, filename)

        # Build query with optional filters
        with logger.get_connection() as conn:
            query = """
                SELECT
                    m.move_id,
                    m.game_id,
                    m.player_id,
                    m.move_number,
                    m.column_played,
                    m.row_placed,
                    m.board_state_before,
                    m.board_state_after,
                    m.score_x_before,
                    m.score_o_before,
                    m.score_x_after,
                    m.score_o_after,
                    m.heuristic_value,
                    m.heuristic_value_x,
                    m.heuristic_value_o,
                    m.legal_moves,
                    m.move_timestamp,
                    g.winner,
                    g.final_score_x,
                    g.final_score_o,
                    g.total_moves as game_total_moves,
                    g.game_mode,
                    p.player_type,
                    p.ai_algorithm,
                    p.ai_difficulty
                FROM moves m
                JOIN games g ON m.game_id = g.game_id
                JOIN players p ON m.player_id = p.player_id
                WHERE g.game_status = 'completed'
            """

            params = []
            if request.game_mode:
                query += " AND g.game_mode = %s"
                params.append(request.game_mode)

            if request.limit:
                # Limit by number of games, not moves
                # Build the CTE-based query
                limited_games_cte = f"""
                    WITH limited_games AS (
                        SELECT DISTINCT game_id FROM games
                        WHERE game_status = 'completed'
                        {"AND game_mode = %s" if request.game_mode else ""}
                        ORDER BY game_id DESC
                        LIMIT %s
                    )
                """
                # Add the filter BEFORE ORDER BY
                query += " AND m.game_id IN (SELECT game_id FROM limited_games)"
                query += " ORDER BY m.game_id, m.move_number"
                # Prepend the CTE
                query = limited_games_cte + query
                # Set params for CTE query
                if request.game_mode:
                    params = [request.game_mode, request.limit, request.game_mode]
                else:
                    params = [request.limit]
            else:
                # No limit - just add ORDER BY
                query += " ORDER BY m.game_id, m.move_number"

            df = pd.read_sql_query(query, conn, params=params if params else None)

        if df.empty:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="No completed games found for export"
            )

        # Export to Parquet
        df.to_parquet(filepath, index=False)

        # Get file size
        file_size = os.path.getsize(filepath)

        # Count unique games
        games_count = df['game_id'].nunique()

        return ExportResponse(
            file_path=filepath,
            moves_exported=len(df),
            games_included=games_count,
            file_size_bytes=file_size
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Export failed: {str(e)}"
        )


# =============================================================================
# ML MODEL PREDICTION ENDPOINTS
# =============================================================================

class PredictMoveRequest(BaseModel):
    """Request for ML move prediction."""
    board: List[List[str]] = Field(..., description="Board state as 2D array")
    legal_moves: Optional[List[int]] = Field(None, description="List of legal column indices")


class PredictMoveResponse(BaseModel):
    """Response from ML move prediction."""
    column: int
    probability: float
    all_probabilities: List[float]


class PredictWinProbRequest(BaseModel):
    """Request for win probability prediction."""
    board: List[List[str]] = Field(..., description="Board state as 2D array")


class PredictWinProbResponse(BaseModel):
    """Response from win probability prediction."""
    value: float
    win_percentage: float
    evaluation: str


class ModelInfo(BaseModel):
    """Information about a loaded model."""
    name: str
    model_type: str
    loaded: bool
    path: Optional[str]
    exists: bool


class ModelsInfoResponse(BaseModel):
    """Response with information about all models."""
    policy_cnn: ModelInfo
    policy_lr: ModelInfo
    value_cnn: ModelInfo
    value_lr: ModelInfo


# Global model instances (lazy loaded) - CNN and LR only
_policy_model_cnn = None
_policy_model_lr = None
_value_model_cnn = None
_value_model_lr = None


def load_policy_model(model_type: str = "cnn"):
    """
    Load policy model (lazy loading).

    Args:
        model_type: Type of model to load ("cnn" or "lr")

    Returns:
        Loaded model or None if not found
    """
    global _policy_model_cnn, _policy_model_lr

    if model_type == "cnn":
        if _policy_model_cnn is None:
            try:
                from src.ml.serving.policy_cnn_wrapper import PolicyModelCNN
                model_path = "/app/data/models/policy_cnn_model.keras"
                if os.path.exists(model_path):
                    _policy_model_cnn = PolicyModelCNN()
                    _policy_model_cnn.load(model_path)
                    print(f"✓ Loaded CNN policy model from {model_path}")
                else:
                    print(f"⚠ CNN policy model not found at {model_path}")
            except Exception as e:
                print(f"⚠ Failed to load CNN policy model: {e}")
        return _policy_model_cnn

    elif model_type == "lr":
        if _policy_model_lr is None:
            try:
                from src.ml.serving.policy_lr_wrapper import PolicyModelLR
                model_path = "/app/data/models/policy_lr_model.pkl"
                if os.path.exists(model_path):
                    _policy_model_lr = PolicyModelLR()
                    _policy_model_lr.load(model_path)
                    print(f"✓ Loaded LR policy model from {model_path}")
                else:
                    print(f"⚠ LR policy model not found at {model_path}")
            except Exception as e:
                print(f"⚠ Failed to load LR policy model: {e}")
        return _policy_model_lr

    else:
        raise ValueError(f"Invalid model_type: {model_type}. Must be 'cnn' or 'lr'")


def load_value_model(model_type: str = "cnn"):
    """
    Load value model (lazy loading).

    Args:
        model_type: Type of model to load ("cnn" or "lr")

    Returns:
        Loaded model or None if not found
    """
    global _value_model_cnn, _value_model_lr

    if model_type == "cnn":
        if _value_model_cnn is None:
            try:
                from src.ml.serving.value_cnn_wrapper import ValueModelCNN
                model_path = "/app/data/models/value_cnn_model.keras"
                if os.path.exists(model_path):
                    _value_model_cnn = ValueModelCNN()
                    _value_model_cnn.load(model_path)
                    print(f"✓ Loaded CNN value model from {model_path}")
                else:
                    print(f"⚠ CNN value model not found at {model_path}")
            except Exception as e:
                print(f"⚠ Failed to load CNN value model: {e}")
        return _value_model_cnn

    elif model_type == "lr":
        if _value_model_lr is None:
            try:
                from src.ml.serving.value_lr_wrapper import ValueModelLR
                model_path = "/app/data/models/value_lr_model.pkl"
                if os.path.exists(model_path):
                    _value_model_lr = ValueModelLR()
                    _value_model_lr.load(model_path)
                    print(f"✓ Loaded LR value model from {model_path}")
                else:
                    print(f"⚠ LR value model not found at {model_path}")
            except Exception as e:
                print(f"⚠ Failed to load LR value model: {e}")
        return _value_model_lr

    else:
        raise ValueError(f"Invalid model_type: {model_type}. Must be 'cnn' or 'lr'")


def board_str_to_numpy(board: List[List[str]]) -> np.ndarray:
    """
    Convert board from string format to numpy array.

    Args:
        board: Board as list of lists with "E", ".", "X", "O"

    Returns:
        Numpy array with numeric encoding (0, 1, -1)
    """
    encoding = {"E": 0, ".": 0, "X": 1, "O": -1}
    return np.array([[encoding[cell] for cell in row] for row in board])


async def log_prediction_to_db(
    prediction_type: str,
    model_type: str,
    board_state: List[List[str]],
    legal_moves: Optional[List[int]] = None,
    predicted_move: Optional[int] = None,
    move_probabilities: Optional[List[float]] = None,
    win_probability: Optional[float] = None,
    response_time_ms: Optional[int] = None
):
    """
    Log ML prediction to database (non-blocking).
    Only runs if ENABLE_PREDICTION_LOGGING environment variable is true.
    """
    if not ENABLE_PREDICTION_LOGGING:
        return

    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=int(os.getenv("DB_PORT")),
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD")
        )

        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO ml_predictions
            (prediction_type, model_type, board_state, legal_moves,
             predicted_move, move_probabilities, win_probability, response_time_ms)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """, (
            prediction_type,
            model_type,
            Json(board_state),
            legal_moves,
            predicted_move,
            Json(move_probabilities) if move_probabilities else None,
            win_probability,
            response_time_ms
        ))

        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Warning: Failed to log prediction: {e}")


@app.post("/api/predict/move", response_model=PredictMoveResponse)
async def predict_move(request: PredictMoveRequest, model_type: str = "cnn"):
    """
    Predict the best move using the ML policy model.

    This endpoint uses a trained neural network to predict the best column
    to play given the current board state.

    Args:
        request: Board state and optional legal moves
        model_type: Type of model to use ("cnn" or "lr", default: "cnn")

    Returns:
        Predicted best move with probabilities
    """
    if model_type not in ["cnn", "lr"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid model_type: {model_type}. Must be 'cnn' or 'lr'"
        )

    policy_model = load_policy_model(model_type)

    if policy_model is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Policy {model_type} model not loaded. Train the model first using train_policy.py or train_policy_cnn.py"
        )

    try:
        # Convert board to numpy
        board_array = board_str_to_numpy(request.board)

        # Get prediction
        best_col, best_prob = policy_model.get_best_move(
            board_array,
            legal_moves=request.legal_moves
        )

        # Get all probabilities
        all_probs = policy_model.predict_single(board_array).tolist()

        # Log prediction (non-blocking)
        asyncio.create_task(log_prediction_to_db(
            prediction_type="policy",
            model_type=model_type,
            board_state=request.board,
            legal_moves=request.legal_moves,
            predicted_move=int(best_col),
            move_probabilities=all_probs
        ))

        return PredictMoveResponse(
            column=best_col,
            probability=best_prob,
            all_probabilities=all_probs
        )

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction failed: {str(e)}"
        )


@app.post("/api/predict/win-probability", response_model=PredictWinProbResponse)
async def predict_win_probability(request: PredictWinProbRequest, model_type: str = "cnn"):
    """
    Predict win probability using the ML value model.

    This endpoint uses a trained neural network to predict the likelihood
    of winning from the current board position.

    Args:
        request: Board state
        model_type: Type of model to use ("cnn" or "lr", default: "cnn")

    Returns:
        Win probability and position evaluation
    """
    if model_type not in ["cnn", "lr"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid model_type: {model_type}. Must be 'cnn' or 'lr'"
        )

    value_model = load_value_model(model_type)

    if value_model is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Value {model_type} model not loaded. Train the model first using train_value.py or train_value_cnn.py"
        )

    try:
        # Convert board to numpy
        board_array = board_str_to_numpy(request.board)

        # Get evaluation
        evaluation = value_model.evaluate_position(board_array)

        # Log prediction (non-blocking)
        asyncio.create_task(log_prediction_to_db(
            prediction_type="value",
            model_type=model_type,
            board_state=request.board,
            win_probability=float(evaluation['value'])
        ))

        return PredictWinProbResponse(
            value=evaluation['value'],
            win_percentage=evaluation['win_percentage'],
            evaluation=evaluation['evaluation']
        )

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction failed: {str(e)}"
        )


@app.get("/api/models/info", response_model=ModelsInfoResponse)
def get_models_info():
    """
    Get information about loaded ML models.

    Returns status of all policy and value models (CNN and LR).
    """
    policy_cnn_path = "/app/data/models/policy_cnn_model.keras"
    policy_lr_path = "/app/data/models/policy_lr_model.pkl"
    value_cnn_path = "/app/data/models/value_cnn_model.keras"
    value_lr_path = "/app/data/models/value_lr_model.pkl"

    return ModelsInfoResponse(
        policy_cnn=ModelInfo(
            name="policy_cnn_model",
            model_type="cnn",
            loaded=_policy_model_cnn is not None,
            path=policy_cnn_path if os.path.exists(policy_cnn_path) else None,
            exists=os.path.exists(policy_cnn_path)
        ),
        policy_lr=ModelInfo(
            name="policy_lr_model",
            model_type="lr",
            loaded=_policy_model_lr is not None,
            path=policy_lr_path if os.path.exists(policy_lr_path) else None,
            exists=os.path.exists(policy_lr_path)
        ),
        value_cnn=ModelInfo(
            name="value_cnn_model",
            model_type="cnn",
            loaded=_value_model_cnn is not None,
            path=value_cnn_path if os.path.exists(value_cnn_path) else None,
            exists=os.path.exists(value_cnn_path)
        ),
        value_lr=ModelInfo(
            name="value_lr_model",
            model_type="lr",
            loaded=_value_model_lr is not None,
            path=value_lr_path if os.path.exists(value_lr_path) else None,
            exists=os.path.exists(value_lr_path)
        )
    )


# =====================================================
# ADMIN ENDPOINTS - ML Prediction Monitoring (Part 2b)
# =====================================================

@app.get("/api/admin/predictions/stats")
def get_prediction_stats():
    """
    Get summary statistics of ML predictions.

    Returns counts and performance metrics grouped by model type.
    """
    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=int(os.getenv("DB_PORT")),
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD")
        )

        cursor = conn.cursor()
        cursor.execute("""
            SELECT
                prediction_type,
                model_type,
                COUNT(*) as total_predictions,
                AVG(response_time_ms) as avg_response_time,
                MIN(predicted_at) as first_prediction,
                MAX(predicted_at) as last_prediction
            FROM ml_predictions
            GROUP BY prediction_type, model_type
            ORDER BY prediction_type, model_type
        """)

        results = cursor.fetchall()
        cursor.close()
        conn.close()

        return {
            "stats": [
                {
                    "prediction_type": row[0],
                    "model_type": row[1],
                    "total_predictions": row[2],
                    "avg_response_time_ms": row[3],
                    "first_prediction": row[4].isoformat() if row[4] else None,
                    "last_prediction": row[5].isoformat() if row[5] else None
                }
                for row in results
            ]
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch prediction stats: {str(e)}"
        )


@app.get("/api/admin/predictions/export")
def export_predictions(
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    model_type: Optional[str] = None,
    prediction_type: Optional[str] = None,
    format: str = "json"
):
    """
    Export prediction history for analysis.

    Query parameters:
    - start_date: ISO format (2024-12-01)
    - end_date: ISO format (2024-12-08)
    - model_type: 'cnn' or 'lr'
    - prediction_type: 'policy' or 'value'
    - format: 'json' or 'csv' (default: json)

    Returns:
        Prediction data in requested format
    """
    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=int(os.getenv("DB_PORT")),
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD")
        )

        # Build query with filters
        query = "SELECT * FROM ml_predictions WHERE 1=1"
        params = []

        if start_date:
            query += " AND predicted_at >= %s"
            params.append(start_date)

        if end_date:
            query += " AND predicted_at <= %s"
            params.append(end_date)

        if model_type:
            query += " AND model_type = %s"
            params.append(model_type)

        if prediction_type:
            query += " AND prediction_type = %s"
            params.append(prediction_type)

        query += " ORDER BY predicted_at DESC LIMIT 10000"

        # Execute query
        cursor = conn.cursor()
        cursor.execute(query, params)
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]
        cursor.close()
        conn.close()

        # Return in requested format
        if format == "csv":
            # Use pandas for CSV export
            df = pd.DataFrame(rows, columns=columns)
            csv_buffer = StringIO()
            df.to_csv(csv_buffer, index=False)
            return Response(
                content=csv_buffer.getvalue(),
                media_type="text/csv",
                headers={"Content-Disposition": "attachment; filename=predictions.csv"}
            )
        else:
            # Manually construct JSON-safe dicts
            results = []
            for row in rows:
                record = {}
                for i, col in enumerate(columns):
                    value = row[i]
                    # Convert timestamps to string
                    if hasattr(value, 'isoformat'):
                        record[col] = value.isoformat()
                    # Handle None
                    elif value is None:
                        record[col] = None
                    # Everything else (including JSONB and arrays) should be fine
                    else:
                        record[col] = value
                results.append(record)
            return results

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to export predictions: {str(e)}"
        )


# =============================================================================
# AI CONFIGURATION ENDPOINTS
# =============================================================================

class AIConfigResponse(BaseModel):
    """Response containing AI configuration for all games and difficulties."""
    connect4: Dict[str, Dict[str, Any]]
    tictactoe: Dict[str, Dict[str, Any]]


class UpdateConfigRequest(BaseModel):
    """Request to update AI configuration."""
    game_type: str = Field(..., description="Game type: connect4 or tictactoe")
    difficulty: int = Field(..., ge=1, le=3, description="Difficulty level: 1 (Easy), 2 (Medium), 3 (Hard)")
    max_iterations: Optional[int] = Field(None, gt=0, description="Maximum MCTS iterations")
    exploration_constant: Optional[float] = Field(None, gt=0, description="UCB exploration constant")
    mistake_probability: Optional[float] = Field(None, ge=0, le=1, description="Probability of making a mistake")

    @field_validator('game_type')
    @classmethod
    def validate_game_type(cls, v):
        if v not in ['connect4', 'tictactoe']:
            raise ValueError('game_type must be "connect4" or "tictactoe"')
        return v


class UpdateConfigResponse(BaseModel):
    """Response after updating AI configuration."""
    success: bool
    message: str
    game_type: str
    difficulty: int
    updated_fields: List[str]
    updated_config: Dict[str, Any]


@app.get("/config", response_model=AIConfigResponse)
def get_ai_config():
    """
    Get current AI configuration for all games and difficulty levels.

    Proxies the request to the AI service to retrieve MCTS parameters
    (max_iterations, exploration_constant, mistake_probability) for
    Connect Four and Tic-Tac-Toe at all difficulty levels.
    """
    try:
        response = requests.get(
            f"{AI_SERVICE_URL}/config",
            timeout=5
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"AI service returned error: {response.status_code}"
            )

        return response.json()

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Could not connect to AI service. Make sure it's running."
        )
    except requests.exceptions.Timeout:
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="AI service timeout"
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error retrieving AI config: {str(e)}"
        )


@app.put("/config", response_model=UpdateConfigResponse)
@app.patch("/config", response_model=UpdateConfigResponse)
def update_ai_config(request: UpdateConfigRequest):
    """
    Update AI configuration for a specific game and difficulty level.

    Proxies the request to the AI service to update MCTS parameters.
    Only provided fields will be updated (supports partial updates).

    Args:
        request: Configuration update request with game_type, difficulty,
                and optional parameters to update

    Returns:
        Success status, message, and updated configuration
    """
    try:
        # Prepare payload - only include fields that were provided
        payload = {
            "game_type": request.game_type,
            "difficulty": request.difficulty
        }

        if request.max_iterations is not None:
            payload["max_iterations"] = request.max_iterations
        if request.exploration_constant is not None:
            payload["exploration_constant"] = request.exploration_constant
        if request.mistake_probability is not None:
            payload["mistake_probability"] = request.mistake_probability

        response = requests.put(
            f"{AI_SERVICE_URL}/config",
            json=payload,
            timeout=5
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"AI service returned error: {response.status_code}"
            )

        return response.json()

    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Could not connect to AI service. Make sure it's running."
        )
    except requests.exceptions.Timeout:
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="AI service timeout"
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error updating AI config: {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
