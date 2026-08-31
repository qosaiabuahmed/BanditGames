"""
Self-Play Module for Connect Four

Enables AI vs AI games to collect training data.
Logs best moves, outcomes, and relevant properties to the database.
"""

import os
import sys
import time
import argparse
import requests
from typing import Optional, List
from dataclasses import dataclass
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

from src.gameLogic.connect_four import GameEngine, GameConfig, Player
from src.logs.gameplay_logger import GameplayLogger, PlayerInfo, board_to_serializable

# AI Service URL
AI_SERVICE_URL = os.getenv("AI_SERVICE_URL", "http://localhost:5001")


@dataclass
class SelfPlayConfig:
    """Configuration for self-play sessions."""
    num_games: int = 10
    difficulty_x: int = 2  # 1=Easy, 2=Medium, 3=Hard
    difficulty_o: int = 2
    rows: int = 6
    cols: int = 7
    connect_length: int = 4
    verbose: bool = True
    timeout: int = 60  # seconds per AI move


@dataclass
class GameResult:
    """Result of a single self-play game."""
    game_id: int
    winner: str
    total_moves: int
    score_x: int
    score_o: int
    duration_seconds: float


class SelfPlayEngine:
    """
    Manages AI vs AI self-play games for data collection.

    Both players use the MCTS AI service to make moves.
    All moves are logged to the database for training.
    """

    def __init__(self, config: SelfPlayConfig, logger: Optional[GameplayLogger] = None):
        """
        Initialize the self-play engine.

        Args:
            config: Self-play configuration
            logger: Database logger (creates one if not provided)
        """
        self.config = config
        self.logger = logger or GameplayLogger()
        self.results: List[GameResult] = []

        # Create AI players in database
        self.player_x_id = self._create_ai_player("AI_X", self.config.difficulty_x)
        self.player_o_id = self._create_ai_player("AI_O", self.config.difficulty_o)

        if self.config.verbose:
            print(f"✓ Self-play engine initialized")
            print(f"  AI Service: {AI_SERVICE_URL}")
            print(f"  Player X: AI (difficulty {self.config.difficulty_x})")
            print(f"  Player O: AI (difficulty {self.config.difficulty_o})")

    def _create_ai_player(self, name: str, difficulty: int) -> int:
        """Create or get an AI player in the database."""
        player_info = PlayerInfo(
            username=f"{name}_diff{difficulty}",
            player_type="ai_mcts",
            ai_algorithm="MCTS",
            ai_difficulty=difficulty,
            ai_config={"difficulty": difficulty}
        )
        return self.logger.create_player(player_info)

    def _get_ai_move(self, game: GameEngine, difficulty: int) -> Optional[int]:
        """
        Get a move from the AI service.

        Args:
            game: Current game engine
            difficulty: Difficulty level (1-3)

        Returns:
            Column to play, or None if AI service fails
        """
        state = game.get_state()
        board_data = [[cell.value for cell in row] for row in state.board]

        request_data = {
            "board": board_data,
            "score_x": state.score_x,
            "score_o": state.score_o,
            "current_player": state.current_player.value,
            "column_positions": state.column_positions,
            "rows": self.config.rows,
            "cols": self.config.cols,
            "connect_length": self.config.connect_length,
            "difficulty": difficulty
        }

        try:
            response = requests.post(
                f"{AI_SERVICE_URL}/get-move",
                json=request_data,
                timeout=self.config.timeout
            )

            if response.status_code == 200:
                result = response.json()
                column = result.get('column')
                # Validate column is in range AND the column is not full
                if column is not None and 0 <= column < self.config.cols:
                    # Double-check against column_positions (>=0 means column has space)
                    if request_data['column_positions'][column] >= 0:
                        return column
                    else:
                        print(f"  ⚠ AI returned full column: {column}")
                        # Try to find any valid column as fallback
                        for c in range(self.config.cols):
                            if request_data['column_positions'][c] >= 0:
                                print(f"  ⚠ Using fallback column: {c}")
                                return c
                        return None
                else:
                    print(f"  ⚠ AI returned invalid column: {column} (expected 0-{self.config.cols-1})")
                    return None
            else:
                print(f"  ⚠ AI service error: {response.status_code}")
                return None

        except requests.exceptions.Timeout:
            print(f"  ⚠ AI service timeout")
            return None
        except requests.exceptions.ConnectionError:
            print(f"  ⚠ AI service not available")
            return None
        except Exception as e:
            print(f"  ⚠ AI error: {e}")
            return None

    def play_game(self) -> Optional[GameResult]:
        """
        Play a single AI vs AI game.

        Returns:
            GameResult with game statistics, or None if game failed
        """
        start_time = time.time()

        # Create game configuration
        game_config = GameConfig(
            rows=self.config.rows,
            cols=self.config.cols,
            connect_length=self.config.connect_length
        )

        # Create game in database
        game_id = self.logger.create_game(
            player_x_id=self.player_x_id,
            player_o_id=self.player_o_id,
            rows=self.config.rows,
            cols=self.config.cols,
            game_mode="ai_vs_ai"
        )

        # Initialize game engine with logging
        game = GameEngine(game_config, logger=self.logger, game_id=game_id)
        game.player_x_id = self.player_x_id
        game.player_o_id = self.player_o_id

        if self.config.verbose:
            print(f"\n  Game {game_id} started...")

        # Play until game over
        move_count = 0
        while not game.is_game_over():
            current_player = game.get_current_player()

            # Get difficulty for current player
            if current_player == Player.X:
                difficulty = self.config.difficulty_x
            else:
                difficulty = self.config.difficulty_o

            # Get AI move
            column = self._get_ai_move(game, difficulty)

            if column is None:
                print(f"  ⚠ Game {game_id} aborted - AI failed to provide move")
                return None

            # Make move (automatically logged by GameEngine)
            success = game.make_move(column)

            if not success:
                print(f"  ⚠ Game {game_id} aborted - invalid move {column}")
                return None

            move_count += 1

            if self.config.verbose and move_count % 10 == 0:
                print(f"    Move {move_count}: {current_player.value} -> column {column}")

        # Game finished
        duration = time.time() - start_time
        winner = game.get_winner()

        # End game in database
        self.logger.end_game(
            game_id=game_id,
            winner=winner,
            final_score_x=game.score_x,
            final_score_o=game.score_o,
            final_board_state=board_to_serializable(game.board.grid)
        )

        result = GameResult(
            game_id=game_id,
            winner=winner,
            total_moves=move_count,
            score_x=game.score_x,
            score_o=game.score_o,
            duration_seconds=duration
        )

        if self.config.verbose:
            print(f"  ✓ Game {game_id} finished: {winner} wins ({move_count} moves, {duration:.1f}s)")

        return result

    def run_session(self) -> List[GameResult]:
        """
        Run a full self-play session with parallel execution.

        Returns:
            List of game results
        """
        print(f"\n{'='*60}")
        print(f"Starting Self-Play Session: {self.config.num_games} games (4 parallel threads)")
        print(f"{'='*60}")

        session_start = time.time()
        self.results = []
        completed_count = 0

        # Run games in parallel using ThreadPoolExecutor (4 workers = 4x faster)
        with ThreadPoolExecutor(max_workers=4) as executor:
            # Submit all games to the thread pool
            futures = {executor.submit(self.play_game): i for i in range(self.config.num_games)}

            # Process results as they complete
            for future in as_completed(futures):
                game_num = futures[future]
                try:
                    result = future.result()
                    completed_count += 1

                    if result:
                        self.results.append(result)
                        print(f"  ✓ Game {completed_count}/{self.config.num_games} completed ({result.winner} won in {result.total_moves} moves)")
                    else:
                        print(f"  ✗ Game {completed_count}/{self.config.num_games} failed")

                except Exception as e:
                    completed_count += 1
                    print(f"  ✗ Game {completed_count}/{self.config.num_games} failed with error: {e}")

        session_duration = time.time() - session_start

        # Print summary
        self._print_summary(session_duration)

        return self.results

    def _print_summary(self, duration: float):
        """Print session summary statistics."""
        print(f"\n{'='*60}")
        print(f"Self-Play Session Complete")
        print(f"{'='*60}")

        if not self.results:
            print("No games completed successfully.")
            return

        total_games = len(self.results)
        x_wins = sum(1 for r in self.results if r.winner == 'X')
        o_wins = sum(1 for r in self.results if r.winner == 'O')
        ties = sum(1 for r in self.results if r.winner == 'Tie')

        total_moves = sum(r.total_moves for r in self.results)
        avg_moves = total_moves / total_games
        avg_duration = duration / total_games

        print(f"Games completed: {total_games}/{self.config.num_games}")
        print(f"Results:")
        print(f"  X wins: {x_wins} ({100*x_wins/total_games:.1f}%)")
        print(f"  O wins: {o_wins} ({100*o_wins/total_games:.1f}%)")
        print(f"  Ties:   {ties} ({100*ties/total_games:.1f}%)")
        print(f"Statistics:")
        print(f"  Total moves logged: {total_moves}")
        print(f"  Avg moves/game: {avg_moves:.1f}")
        print(f"  Avg time/game: {avg_duration:.1f}s")
        print(f"  Total duration: {duration:.1f}s")
        print(f"{'='*60}")


def main():
    """Main entry point for self-play script."""
    parser = argparse.ArgumentParser(description="Run AI vs AI self-play games")
    parser.add_argument("-n", "--num-games", type=int, default=10,
                        help="Number of games to play (default: 10)")
    parser.add_argument("--difficulty-x", type=int, default=2, choices=[1, 2, 3],
                        help="Difficulty for Player X: 1=Easy, 2=Medium, 3=Hard (default: 2)")
    parser.add_argument("--difficulty-o", type=int, default=2, choices=[1, 2, 3],
                        help="Difficulty for Player O: 1=Easy, 2=Medium, 3=Hard (default: 2)")
    parser.add_argument("-q", "--quiet", action="store_true",
                        help="Suppress verbose output")
    parser.add_argument("--timeout", type=int, default=60,
                        help="Timeout per AI move in seconds (default: 60)")

    args = parser.parse_args()

    config = SelfPlayConfig(
        num_games=args.num_games,
        difficulty_x=args.difficulty_x,
        difficulty_o=args.difficulty_o,
        verbose=not args.quiet,
        timeout=args.timeout
    )

    try:
        engine = SelfPlayEngine(config)
        results = engine.run_session()

        if results:
            print(f"\n✓ Successfully generated {len(results)} games of training data")
            print(f"  Game IDs: {[r.game_id for r in results]}")
        else:
            print("\n✗ No games completed successfully")
            sys.exit(1)

    except Exception as e:
        print(f"\n✗ Self-play failed: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
