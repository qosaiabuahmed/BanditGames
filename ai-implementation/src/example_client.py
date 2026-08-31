"""
Example Client for Multi-Game AI API

Demonstrates how to call the AI container from the game backend for both
Connect Four and Tic-Tac-Toe games.
"""

import requests


def example_health_check():
    """Example: Check if AI service is running and see supported games."""
    print("\n" + "=" * 60)
    print("EXAMPLE 1: Health Check")
    print("=" * 60)

    try:
        response = requests.get("http://localhost:5000/health", timeout=2)

        if response.status_code == 200:
            data = response.json()
            print("[OK] AI Service is healthy")
            print(f"  Service: {data['service']}")
            print(f"  Supported games: {', '.join(data['supported_games'])}")
        else:
            print(f"[ERROR] Unexpected status: {response.status_code}")

    except requests.exceptions.ConnectionError:
        print("[ERROR] AI Service is not running")
        print("  Start with: python -m flask --app src.api run")


def example_connect4_mid_game():
    """Example: Connect4 mid-game position."""
    print("\n" + "=" * 60)
    print("EXAMPLE 2: Connect4 Mid-Game Position")
    print("=" * 60)

    game_state = {
        "game_type": "connect4",
        "board": [
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", "O", ".", ".", "."],
            ["X", "O", ".", "X", "O", ".", "."],
            ["X", "O", "X", "O", "X", ".", "."],
            ["X", "O", "X", "X", "O", "O", "."]
        ],
        "score_x": 1,
        "score_o": 1,
        "current_player": "O",
        "column_positions": [4, 3, 2, 1, 2, 4, 5],
        "difficulty": 2
    }

    print("Current board state:")
    for row in game_state["board"]:
        print("  " + " ".join(row))
    print(f"\nScores - X: {game_state['score_x']}, O: {game_state['score_o']}")
    print(f"Current player: {game_state['current_player']}")

    try:
        response = requests.post(
            "http://localhost:5000/get-move",
            json=game_state,
            timeout=10
        )

        if response.status_code == 200:
            result = response.json()
            print(f"\n[OK] AI recommends: Column {result['move']} (0-indexed)")
            print(f"  Confidence: {result['confidence']:.2%}")
            print(f"  Nodes explored: {result['nodes_explored']}")
            print(f"  Time: {result['computation_time_ms']}ms")

    except requests.exceptions.ConnectionError:
        print("\n[ERROR] Could not connect to AI service")


def example_tictactoe_mid_game():
    """Example: TicTacToe mid-game position."""
    print("\n" + "=" * 60)
    print("EXAMPLE 3: TicTacToe Mid-Game Position")
    print("=" * 60)

    game_state = {
        "game_type": "tictactoe",
        "board": [
            ["X", ".", "O"],
            [".", "X", "."],
            ["O", ".", "."]
        ],
        "current_player": "X",
        "difficulty": 2
    }

    print("Current board state:")
    for i, row in enumerate(game_state["board"]):
        print("  " + " | ".join(row))
        if i < 2:
            print("  " + "-" * 9)

    print(f"\nCurrent player: {game_state['current_player']}")
    print("\nPosition mapping:")
    print("  0 | 1 | 2")
    print("  ---------")
    print("  3 | 4 | 5")
    print("  ---------")
    print("  6 | 7 | 8")

    try:
        response = requests.post(
            "http://localhost:5000/get-move",
            json=game_state,
            timeout=10
        )

        if response.status_code == 200:
            result = response.json()
            print(f"\n[OK] AI recommends: Position {result['move']} (0-indexed)")
            print(f"  Confidence: {result['confidence']:.2%}")
            print(f"  Nodes explored: {result['nodes_explored']}")
            print(f"  Time: {result['computation_time_ms']}ms")

    except requests.exceptions.ConnectionError:
        print("\n[ERROR] Could not connect to AI service")


def example_connect4_analyze():
    """Example: Get detailed analysis of all Connect4 moves."""
    print("\n" + "=" * 60)
    print("EXAMPLE 4: Connect4 Position Analysis")
    print("=" * 60)

    game_state = {
        "game_type": "connect4",
        "board": [
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", "X", ".", ".", ".", "."],
            [".", ".", "O", "X", ".", ".", "."]
        ],
        "score_x": 0,
        "score_o": 0,
        "current_player": "O",
        "column_positions": [5, 5, 4, 4, 5, 5, 5],
        "difficulty": 2
    }

    try:
        response = requests.post(
            "http://localhost:5000/analyze",
            json=game_state,
            timeout=10
        )

        if response.status_code == 200:
            result = response.json()

            print(f"\nBest move: Column {result['best_move']} (0-indexed)")
            print(f"\nDetailed analysis of all moves:")
            print("-" * 60)

            # Sort by visits (most explored first)
            moves = sorted(
                result['move_analysis'].items(),
                key=lambda x: x[1]['visits'],
                reverse=True
            )

            for col, stats in moves:
                col_num = int(col)
                visits = stats['visits']
                win_rate = stats['win_rate']
                confidence = stats['confidence']

                bar = "#" * int(win_rate * 20)
                print(f"Column {col_num}: {bar:<20} | "
                      f"Win rate: {win_rate:5.1%} | "
                      f"Visits: {visits:4} | "
                      f"Confidence: {confidence:5.1%}")

    except requests.exceptions.ConnectionError:
        print("[ERROR] Could not connect to AI service")


def example_tictactoe_tactical():
    """Example: TicTacToe tactical move detection (difficulty 3)."""
    print("\n" + "=" * 60)
    print("EXAMPLE 5: TicTacToe Tactical Play (Difficulty 3)")
    print("=" * 60)

    # Position where X can win
    game_state = {
        "game_type": "tictactoe",
        "board": [
            ["X", "X", "."],
            ["O", "O", "."],
            [".", ".", "."]
        ],
        "current_player": "X",
        "difficulty": 3  # Hard difficulty uses tactical detection
    }

    print("Current board state:")
    for i, row in enumerate(game_state["board"]):
        print("  " + " | ".join(row))
        if i < 2:
            print("  " + "-" * 9)

    try:
        response = requests.post(
            "http://localhost:5000/get-move",
            json=game_state,
            timeout=10
        )

        if response.status_code == 200:
            result = response.json()
            print(f"\n[OK] AI finds winning move: Position {result['move']} (0-indexed)")
            print(f"  Confidence: {result['confidence']:.2%}")
            print(f"  Nodes explored: {result['nodes_explored']} (tactical detection)")
            print(f"  Time: {result['computation_time_ms']}ms")

    except requests.exceptions.ConnectionError:
        print("\n[ERROR] Could not connect to AI service")


if __name__ == "__main__":
    print("\n")
    print("=" * 60)
    print(" " * 10 + "MULTI-GAME AI - CLIENT EXAMPLES")
    print("=" * 60)

    # Run all examples
    example_health_check()

    print("\n" + "-" * 60)
    print("CONNECT FOUR EXAMPLES")
    print("-" * 60)
    example_connect4_mid_game()
    example_connect4_analyze()

    print("\n" + "-" * 60)
    print("TIC-TAC-TOE EXAMPLES")
    print("-" * 60)
    example_tictactoe_mid_game()
    example_tictactoe_tactical()

    print("\n" + "=" * 60)
    print("Examples complete!")
    print("=" * 60)
    print("\nIntegration tips:")
    print("1. Always include 'game_type' field in requests")
    print("2. Check health endpoint to see supported games")
    print("3. Handle connection errors gracefully")
    print("4. Set appropriate timeouts (5-10 seconds)")
    print("5. For Connect4: use 'column_positions' and 'score_x/o'")
    print("6. For TicTacToe: board is 3x3, moves are positions 0-8")
    print("7. Response 'move' field is always 0-indexed")
    print()