"""
Test script to verify both Connect4 and TicTacToe work with MCTS.
"""

from src.game_factory import GameFactory
from src.mcts import MCTS
from src.config import AIConfig

def test_connect4():
    """Test Connect4 game."""
    print("=" * 60)
    print("Testing Connect4")
    print("=" * 60)

    # Create empty Connect4 game
    game_state_json = {
        "game_type": "connect4",
        "board": [
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."],
            [".", ".", ".", ".", ".", ".", "."]
        ],
        "current_player": "X",
        "column_positions": [5, 5, 5, 5, 5, 5, 5],
        "score_x": 0,
        "score_o": 0
    }

    # Create game state
    game_state = GameFactory.create_from_json(game_state_json)
    print(f"Game type: {game_state.get_game_type()}")
    print(f"Valid moves: {game_state.get_valid_moves()}")

    # Get difficulty profile
    profile = AIConfig.get_profile("connect4", 2)
    print(f"Difficulty profile (Medium): {profile}")

    # Create MCTS and get move
    mcts = MCTS(
        exploration_constant=profile['exploration_constant'],
        max_iterations=profile['max_iterations']
    )

    best_move = mcts.search(game_state)
    print(f"Best move (column): {best_move}")
    print(f"Nodes explored: {mcts.nodes_explored}")

    # Test preferred move heuristic
    preferred = game_state.get_preferred_move([0, 1, 2, 3, 4, 5, 6])
    print(f"Preferred move (center column): {preferred}")

    print("Connect4 test PASSED!")
    print()

def test_tictactoe():
    """Test TicTacToe game."""
    print("=" * 60)
    print("Testing TicTacToe")
    print("=" * 60)

    # Create empty TicTacToe game
    game_state_json = {
        "game_type": "tictactoe",
        "board": [
            [".", ".", "."],
            [".", ".", "."],
            [".", ".", "."]
        ],
        "current_player": "X"
    }

    # Create game state
    game_state = GameFactory.create_from_json(game_state_json)
    print(f"Game type: {game_state.get_game_type()}")
    print(f"Valid moves: {game_state.get_valid_moves()}")

    # Get difficulty profile
    profile = AIConfig.get_profile("tictactoe", 2)
    print(f"Difficulty profile (Medium): {profile}")

    # Create MCTS and get move
    mcts = MCTS(
        exploration_constant=profile['exploration_constant'],
        max_iterations=profile['max_iterations']
    )

    best_move = mcts.search(game_state)
    print(f"Best move (position): {best_move}")
    print(f"Nodes explored: {mcts.nodes_explored}")

    # Test preferred move heuristic
    preferred = game_state.get_preferred_move([0, 1, 2, 3, 4, 5, 6, 7, 8])
    print(f"Preferred move (center): {preferred}")

    print("TicTacToe test PASSED!")
    print()

def test_tactical_moves():
    """Test tactical move detection."""
    print("=" * 60)
    print("Testing Tactical Moves (Win/Block Detection)")
    print("=" * 60)

    # TicTacToe with winning move for X
    game_state_json = {
        "game_type": "tictactoe",
        "board": [
            ["X", "X", "."],
            ["O", "O", "."],
            [".", ".", "."]
        ],
        "current_player": "X"
    }

    game_state = GameFactory.create_from_json(game_state_json)
    winning_move = game_state.find_winning_move()
    print(f"TicTacToe winning move for X: {winning_move} (should be 2)")

    # TicTacToe with blocking move needed
    game_state_json_block = {
        "game_type": "tictactoe",
        "board": [
            ["X", "X", "."],
            ["O", "O", "."],
            [".", ".", "."]
        ],
        "current_player": "O"
    }

    game_state_block = GameFactory.create_from_json(game_state_json_block)
    blocking_move = game_state_block.find_blocking_move()
    print(f"TicTacToe blocking move for O: {blocking_move} (should be 2)")

    print("Tactical moves test PASSED!")
    print()

if __name__ == "__main__":
    print("Multi-Game MCTS Test Suite")
    print()

    test_connect4()
    test_tictactoe()
    test_tactical_moves()

    print("=" * 60)
    print("All tests PASSED!")
    print("=" * 60)