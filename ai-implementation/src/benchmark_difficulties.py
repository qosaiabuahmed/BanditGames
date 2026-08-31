"""
Benchmark script to test all difficulty levels via the API.

Measures computation time and validates that different difficulties
produce different skill levels by running tournaments through the actual API.
"""

import time
import requests
from typing import Optional
from game_simulator import GameSimulator, Board, Player
from config import AIConfig


# API Configuration
# Note: Use localhost instead of 0.0.0.0 for client connections
API_HOST = "localhost" if AIConfig.SERVER_HOST == "0.0.0.0" else AIConfig.SERVER_HOST
API_URL = f"http://{API_HOST}:{AIConfig.SERVER_PORT}"
GET_MOVE_ENDPOINT = f"{API_URL}/get-move"
HEALTH_ENDPOINT = f"{API_URL}/health"


def check_api_health() -> bool:
    """
    Check if the API is running and healthy.

    Returns:
        True if API is healthy, False otherwise
    """
    try:
        response = requests.get(HEALTH_ENDPOINT, timeout=2)
        return response.status_code == 200
    except requests.exceptions.RequestException:
        return False


def get_move_from_api(game_state: GameSimulator, difficulty: int) -> tuple[int, dict]:
    """
    Get the best move from the API for the given game state.

    Args:
        game_state: Current game state
        difficulty: Difficulty level (1, 2, or 3)

    Returns:
        Tuple of (column, api_response_dict)
    """
    # Convert board grid to list of strings (convert Player enums to their string values)
    board_list = [
        [cell.value for cell in row]
        for row in game_state.board.grid
    ]

    # Convert game state to API format
    payload = {
        "board": board_list,
        "current_player": game_state.current_player.value,
        "score_x": game_state.score_x,
        "score_o": game_state.score_o,
        "column_positions": game_state.board.column_positions,
        "difficulty": difficulty,
        "rows": game_state.board.rows,
        "cols": game_state.board.cols,
        "connect_length": 4
    }

    response = requests.post(GET_MOVE_ENDPOINT, json=payload, timeout=30)
    response.raise_for_status()

    data = response.json()
    return data['column'], data


def benchmark_difficulty(difficulty1: int, num_tests: int = 5):
    """
    Benchmark a specific difficulty level via the API.

    Args:
        difficulty1: Difficulty level (1, 2, or 3)
        num_tests: Number of test positions to evaluate
    """
    profile = AIConfig.DIFFICULTY_PROFILES[difficulty1]

    print(f"\n{'='*60}")
    print(f"Difficulty {difficulty1} - {profile['max_iterations']} iterations")
    print(f"Mistake probability: {profile.get('mistake_probability', 0.0)*100:.0f}%")
    print(f"{'='*60}")

    times = []

    for test_num in range(num_tests):
        # Create a test position (empty board)
        board = Board(6, 7)
        game_state = GameSimulator(board, Player.X, 0, 0)

        # Time the API call
        start = time.time()

        try:
            best_move, api_data = get_move_from_api(game_state, difficulty1)
            elapsed = time.time() - start
            times.append(elapsed)

            api_time = api_data.get('computation_time_ms', 0)
            nodes = api_data.get('nodes_explored', 0)
            confidence = api_data.get('confidence', 0)

            print(
                f"Test {test_num + 1}: {elapsed:.3f}s total "
                f"(API: {api_time}ms, nodes: {nodes}, "
                f"confidence: {confidence:.2%}) - Move: {best_move}"
            )
        except Exception as e:
            print(f"Test {test_num + 1}: FAILED - {str(e)}")
            continue

    if times:
        avg_time = sum(times) / len(times)
        print(f"\nAverage time: {avg_time:.3f}s ({avg_time * 1000:.0f}ms)")
        return avg_time
    else:
        print("\nAll tests failed!")
        return None


def play_game_with_difficulties(difficulty1: int, difficulty2: int) -> tuple:
    """
    Play a single game between two difficulty levels using the API.

    Args:
        difficulty1: Difficulty for Player X
        difficulty2: Difficulty for Player O

    Returns:
        Tuple of (winner, score_x, score_o, move_count)
    """
    board = Board(6, 7)
    game_state = GameSimulator(board, Player.X, 0, 0)

    move_count = 0
    max_moves = 42  # 6 rows * 7 cols

    while not game_state.is_game_over() and move_count < max_moves:
        # Determine which difficulty to use
        if game_state.current_player == Player.X:
            difficulty = difficulty1
        else:
            difficulty = difficulty2

        try:
            # Get move from API
            best_move, _ = get_move_from_api(game_state, difficulty)

            # Make the move
            game_state.make_move(best_move)
            move_count += 1

        except Exception as e:
            print(f"\nError getting move from API: {e}")
            # Pick a random valid move as fallback
            valid_moves = game_state.get_valid_moves()
            if valid_moves:
                best_move = valid_moves[0]
                game_state.make_move(best_move)
                move_count += 1
            else:
                break

    winner = game_state.get_winner()
    return winner, game_state.score_x, game_state.score_o, move_count


def tournament(difficulty1: int, difficulty2: int, num_games: int = 10):
    """
    Run a tournament between two difficulties using the API.

    Args:
        difficulty1: Difficulty for Player X
        difficulty2: Difficulty for Player O
        num_games: Number of games to play
    """
    print(f"\n{'='*60}")
    print(f"Tournament: Difficulty {difficulty1} (X) vs Difficulty {difficulty2} (O)")
    print(f"Playing {num_games} games via API...")
    print(f"{'='*60}")

    wins_x = 0
    wins_o = 0
    ties = 0
    total_moves = 0

    for game_num in range(num_games):
        print(f"\nGame {game_num + 1}...", end=" ", flush=True)
        winner, score_x, score_o, moves = play_game_with_difficulties(difficulty1, difficulty2)
        total_moves += moves

        if winner == Player.X:
            wins_x += 1
            result = "X wins"
        elif winner == Player.O:
            wins_o += 1
            result = "O wins"
        else:
            ties += 1
            result = "Tie"

        print(f"{result} (Score: {score_x}-{score_o}, Moves: {moves})")

    print(f"\n{'='*60}")
    print(f"Results:")
    print(f"  Difficulty {difficulty1} (X): {wins_x} wins ({wins_x/num_games*100:.1f}%)")
    print(f"  Difficulty {difficulty2} (O): {wins_o} wins ({wins_o/num_games*100:.1f}%)")
    print(f"  Ties: {ties} ({ties/num_games*100:.1f}%)")
    print(f"  Average moves per game: {total_moves/num_games:.1f}")
    print(f"{'='*60}")


if __name__ == "__main__":
    print("MCTS Difficulty Benchmark (via API)")
    print("="*60)

    # Check if API is running
    print("\nChecking API health...")
    if not check_api_health():
        print(f"ERROR: API is not running at {API_URL}")
        print("Please start the API first with:")
        print("  python src/api.py")
        print("or")
        print("  docker-compose up")
        exit(1)

    print(f"✓ API is healthy at {API_URL}")

    # Benchmark each difficulty
    print("\n\nPart 1: Timing Benchmarks")
    print("="*60)

    for difficulty in [1, 2, 3]:
        benchmark_difficulty(difficulty, num_tests=3)

    # Run tournaments
    print("\n\nPart 2: Skill Validation Tournaments")
    print("="*60)

    # Test Easy vs Medium
    tournament(1, 2, num_games=5)

    # Test Easy vs Hard
    tournament(1, 3, num_games=5)

    # Test Medium vs Hard
    tournament(2, 3, num_games=5)

    print("\n\nBenchmark complete!")
