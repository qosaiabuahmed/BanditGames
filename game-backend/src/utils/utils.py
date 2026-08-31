"""
Utility functions shared across the Connect Four application.
"""
from typing import List, Tuple, Optional


class ScoreCalculator:
    """Calculates scores based on Connect Four patterns."""

    def __init__(self, board, connect_length: int = 4):
        """
        Initialize the score calculator.

        Args:
            board: Game board to analyze
            connect_length: Number of pieces to connect (default 4)
        """
        self.board = board
        self.connect_length = connect_length

    def check_four_in_line(
        self,
        positions: List[Tuple[int, int]]
    ) -> Optional:
        """
        Check if four positions contain the same player's pieces.

        Args:
            positions: List of (row, col) tuples to check

        Returns:
            Player if all four positions match, None otherwise
        """
        from src.gameLogic.connect_four import Player

        pieces = [self.board.get_piece(r, c) for r, c in positions]

        if (len(pieces) == self.connect_length and
            all(p == pieces[0] and p != Player.NONE for p in pieces)):
            return pieces[0]

        return None

    def calculate_score_for_move(
        self,
        row: int,
        col: int
    ) -> Tuple[int, int]:
        """
        Calculate score changes from a move at position (row, col).

        Args:
            row: Row of the move
            col: Column of the move

        Returns:
            Tuple of (score_x_increase, score_o_increase)
        """
        from src.gameLogic.connect_four import Player

        score_x_increase = 0
        score_o_increase = 0

        # Define all directions to check
        directions = [
            (0, 1),   # Horizontal
            (1, 0),   # Vertical
            (1, 1),   # Diagonal down-right
            (1, -1),  # Diagonal down-left
        ]

        for dr, dc in directions:
            # Check all possible 4-in-a-row patterns involving this position
            for offset in range(-3, 1):
                positions = [
                    (row + dr * (offset + i), col + dc * (offset + i))
                    for i in range(self.connect_length)
                ]

                # Check if all positions are within bounds
                if all(
                    0 <= r < self.board.rows and 0 <= c < self.board.cols
                    for r, c in positions
                ):
                    result = self.check_four_in_line(positions)
                    if result == Player.X:
                        score_x_increase += 1
                    elif result == Player.O:
                        score_o_increase += 1

        return score_x_increase, score_o_increase


def calculate_difficulty(player_rating: int, score_x: int, score_o: int) -> int:
    """
    Calculate AI difficulty (1-3) based on player rating and in-game score.

    Starting difficulty is based on player rating:
    - Rating < 1000: Start at 1 (Easy)
    - Rating 1000-1050: Start at 2 (Medium)
    - Rating > 1050: Start at 3 (Hard)

    Then adjusts based on in-game score difference:
    - If player is losing by 2+: decrease difficulty by 1
    - If player is winning by 2+: increase difficulty by 1

    Args:
        player_rating: Player's database rating score (starts at 1000)
        score_x: Current in-game score for X
        score_o: Current in-game score for O

    Returns:
        Difficulty level (1, 2, or 3)

    Examples:
        >>> calculate_difficulty(900, 0, 0)
        1
        >>> calculate_difficulty(1025, 0, 0)
        2
        >>> calculate_difficulty(1100, 0, 0)
        3
        >>> calculate_difficulty(1000, 3, 0)  # X winning by 3
        3
        >>> calculate_difficulty(1000, 0, 3)  # X losing by 3
        1
    """
    # Base difficulty from player rating
    if player_rating < 1000:
        base_difficulty = 1  # Easy
    elif player_rating <= 1050:
        base_difficulty = 2  # Medium
    else:
        base_difficulty = 3  # Hard

    # Adjust based on in-game score difference
    score_diff = score_x - score_o  # Positive = X winning, Negative = O winning

    if abs(score_diff) >= 2:
        if score_diff > 0:
            # X is winning - make it harder
            base_difficulty = min(3, base_difficulty + 1)
        else:
            # O is winning - make it easier
            base_difficulty = max(1, base_difficulty - 1)

    return base_difficulty
