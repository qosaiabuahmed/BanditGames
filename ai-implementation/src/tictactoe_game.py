"""
Tic-Tac-Toe Game Simulator for AI Container

Tic-Tac-Toe specific game logic for MCTS simulations.
"""

from typing import List, Optional
import copy
from src.base_game import BaseGameSimulator, Player


class TicTacToeSimulator(BaseGameSimulator):
    """
    Simulates Tic-Tac-Toe games for MCTS.

    Board positions (0-8):
    0 | 1 | 2
    ---------
    3 | 4 | 5
    ---------
    6 | 7 | 8
    """

    # Winning line patterns (all possible 3-in-a-rows)
    WINNING_LINES = [
        # Rows
        [0, 1, 2],
        [3, 4, 5],
        [6, 7, 8],
        # Columns
        [0, 3, 6],
        [1, 4, 7],
        [2, 5, 8],
        # Diagonals
        [0, 4, 8],
        [2, 4, 6],
    ]

    def __init__(self, board: Optional[List[Player]] = None, current_player: Player = Player.X):
        """
        Initialize Tic-Tac-Toe simulator.

        Args:
            board: List of 9 Player values (positions 0-8), or None for empty board
            current_player: Player whose turn it is
        """
        if board is None:
            self.board = [Player.NONE] * 9
        else:
            self.board = board.copy()

        self.current_player = current_player
        self.winner = None  # Cache the winner once determined

    def copy(self) -> 'TicTacToeSimulator':
        """Create a deep copy of the simulator."""
        new_sim = TicTacToeSimulator(
            board=self.board.copy(),
            current_player=self.current_player
        )
        new_sim.winner = self.winner
        return new_sim

    def make_move(self, position: int) -> bool:
        """
        Place a piece at the specified position.

        Args:
            position: Position index (0-8)

        Returns:
            True if move was valid, False otherwise
        """
        # Validate move
        if position < 0 or position >= 9:
            return False

        if self.board[position] != Player.NONE:
            return False

        # Place piece
        self.board[position] = self.current_player

        # Check for win
        if self._check_win():
            self.winner = self.current_player

        # Switch player
        self.current_player = self.current_player.opponent()

        return True

    def _check_win(self) -> bool:
        """
        Check if current player has won.

        Returns:
            True if current player has 3-in-a-row, False otherwise
        """
        for line in self.WINNING_LINES:
            if all(self.board[pos] == self.current_player for pos in line):
                return True
        return False

    def is_game_over(self) -> bool:
        """Check if game is over (win or tie)."""
        # Someone won
        if self.winner is not None:
            return True

        # Board is full (tie)
        if all(cell != Player.NONE for cell in self.board):
            return True

        return False

    def get_winner(self) -> Optional[Player]:
        """
        Get the winner.

        Returns:
            Player.X or Player.O if there's a winner, None for tie/ongoing
        """
        return self.winner

    def get_valid_moves(self) -> List[int]:
        """Get list of valid position indices."""
        return [i for i in range(9) if self.board[i] == Player.NONE]

    def get_game_type(self) -> str:
        """Get game type identifier."""
        return "tictactoe"

    def get_preferred_move(self, valid_moves: List[int]) -> Optional[int]:
        """
        Get preferred move for simulations (center/corners preference).

        Strategy for random playouts:
        1. Prefer center (position 4)
        2. Prefer corners (0, 2, 6, 8)
        3. Otherwise random

        Args:
            valid_moves: List of valid position indices

        Returns:
            Preferred move if available, None otherwise
        """
        # Prefer center
        if 4 in valid_moves:
            return 4

        # Prefer corners
        corners = [0, 2, 6, 8]
        available_corners = [c for c in corners if c in valid_moves]
        if available_corners:
            return available_corners[0]

        return None

    def find_winning_move(self) -> Optional[int]:
        """
        Find a move that wins the game immediately.

        Returns:
            Position index if winning move exists, None otherwise
        """
        for position in self.get_valid_moves():
            # Try the move
            sim = self.copy()
            sim.board[position] = self.current_player

            # Check if it wins
            if sim._check_win():
                return position

        return None

    def find_blocking_move(self) -> Optional[int]:
        """
        Find a move that blocks opponent from winning next turn.

        Returns:
            Position index if blocking move needed, None otherwise
        """
        opponent = self.current_player.opponent()

        for position in self.get_valid_moves():
            # Simulate opponent playing this move
            sim = self.copy()
            sim.board[position] = opponent
            sim.current_player = opponent  # Temporarily set for win check

            # Check if opponent would win
            if sim._check_win():
                return position

        return None

    @staticmethod
    def from_json(state_dict: dict) -> 'TicTacToeSimulator':
        """
        Create simulator from JSON game state.

        Args:
            state_dict: Dictionary with game state from API
                       Expected fields: board (3x3 grid), current_player

        Example:
        {
            "game_type": "tictactoe",
            "board": [["X", ".", "O"],
                      [".", "X", "."],
                      [".", ".", "."]],
            "current_player": "O"
        }

        Returns:
            TicTacToeSimulator instance
        """
        # Parse 3x3 grid into 1D array (0-8)
        board_2d = state_dict['board']
        board_1d = []

        for row in board_2d:
            for cell in row:
                board_1d.append(Player.from_string(cell))

        # Get current player
        current_player = Player.from_string(state_dict['current_player'])

        return TicTacToeSimulator(
            board=board_1d,
            current_player=current_player
        )

    def to_json(self) -> dict:
        """
        Convert game state to JSON format (useful for debugging/testing).

        Returns:
            Dictionary with game state in API format
        """
        # Convert 1D array to 3x3 grid
        board_2d = [
            [self.board[0].value, self.board[1].value, self.board[2].value],
            [self.board[3].value, self.board[4].value, self.board[5].value],
            [self.board[6].value, self.board[7].value, self.board[8].value],
        ]

        return {
            "game_type": "tictactoe",
            "board": board_2d,
            "current_player": self.current_player.value
        }