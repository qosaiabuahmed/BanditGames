"""
Connect Four Game Simulator for AI Container

Connect Four specific game logic for MCTS simulations.
This is a stripped-down version without logging, database, or UI concerns.
"""

from typing import List, Tuple, Optional
import copy
from src.base_game import BaseGameSimulator, Player


class Connect4Board:
    """Lightweight board for Connect Four simulations."""

    def __init__(self, rows: int = 6, cols: int = 7):
        """Initialize empty board."""
        self.rows = rows
        self.cols = cols
        self.grid = [[Player.NONE for _ in range(cols)] for _ in range(rows)]
        self.column_positions = [rows - 1 for _ in range(cols)]

    def copy(self) -> 'Connect4Board':
        """Create a deep copy of the board."""
        new_board = Connect4Board(self.rows, self.cols)
        new_board.grid = copy.deepcopy(self.grid)
        new_board.column_positions = self.column_positions.copy()
        return new_board

    def is_valid_column(self, col: int) -> bool:
        """Check if a column can accept a piece."""
        return 0 <= col < self.cols and self.column_positions[col] >= 0

    def get_valid_columns(self) -> List[int]:
        """Get list of all valid columns."""
        return [col for col in range(self.cols) if self.is_valid_column(col)]

    def place_piece(self, col: int, player: Player) -> Optional[Tuple[int, int]]:
        """
        Place a piece in the specified column.

        Returns:
            (row, col) where piece was placed, or None if invalid
        """
        if not self.is_valid_column(col):
            return None

        row = self.column_positions[col]
        self.grid[row][col] = player
        self.column_positions[col] -= 1
        return (row, col)

    def is_full(self) -> bool:
        """Check if board is completely full."""
        return all(pos < 0 for pos in self.column_positions)

    def get_piece(self, row: int, col: int) -> Player:
        """Get piece at position."""
        if 0 <= row < self.rows and 0 <= col < self.cols:
            return self.grid[row][col]
        return Player.NONE

    def check_win_at_position(self, row: int, col: int, connect_length: int = 4) -> bool:
        """
        Check if the piece at (row, col) creates a 4-in-a-row.
        This is much faster than checking the entire board.

        Args:
            row: Row of the last placed piece
            col: Column of the last placed piece
            connect_length: Number of pieces needed in a row (default 4)

        Returns:
            True if this piece creates a win, False otherwise
        """
        player = self.get_piece(row, col)
        if player == Player.NONE:
            return False

        directions = [
            (0, 1),   # Horizontal
            (1, 0),   # Vertical
            (1, 1),   # Diagonal down-right
            (1, -1),  # Diagonal down-left
        ]

        for dr, dc in directions:
            count = 1  # Count the piece we just placed

            # Check in positive direction
            for i in range(1, connect_length):
                r, c = row + dr * i, col + dc * i
                if 0 <= r < self.rows and 0 <= c < self.cols and self.get_piece(r, c) == player:
                    count += 1
                else:
                    break

            # Check in negative direction
            for i in range(1, connect_length):
                r, c = row - dr * i, col - dc * i
                if 0 <= r < self.rows and 0 <= c < self.cols and self.get_piece(r, c) == player:
                    count += 1
                else:
                    break

            if count >= connect_length:
                return True

        return False


class Connect4ScoreCalculator:
    """Calculate scores for Connect Four positions."""

    def __init__(self, board: Connect4Board, connect_length: int = 4):
        """Initialize calculator."""
        self.board = board
        self.connect_length = connect_length

    def check_line(self, positions: List[Tuple[int, int]]) -> Optional[Player]:
        """
        Check if positions form a winning line.

        Returns:
            Player if all positions match and form a line, None otherwise
        """
        pieces = [self.board.get_piece(r, c) for r, c in positions]

        if (len(pieces) == self.connect_length and
                all(p == pieces[0] and p != Player.NONE for p in pieces)):
            return pieces[0]

        return None

    def calculate_score_for_position(
            self,
            row: int,
            col: int
    ) -> Tuple[int, int]:
        """
        Calculate how many 4-in-a-rows pass through this position.

        Returns:
            (score_x_increase, score_o_increase)
        """
        score_x = 0
        score_o = 0

        # Directions: horizontal, vertical, diagonal-right, diagonal-left
        directions = [
            (0, 1),  # Horizontal
            (1, 0),  # Vertical
            (1, 1),  # Diagonal down-right
            (1, -1),  # Diagonal down-left
        ]

        for dr, dc in directions:
            # Check all possible 4-in-a-row patterns through this position
            for offset in range(-3, 1):
                positions = [
                    (row + dr * (offset + i), col + dc * (offset + i))
                    for i in range(self.connect_length)
                ]

                # Verify all positions are in bounds
                if all(
                        0 <= r < self.board.rows and 0 <= c < self.board.cols
                        for r, c in positions
                ):
                    result = self.check_line(positions)
                    if result == Player.X:
                        score_x += 1
                    elif result == Player.O:
                        score_o += 1

        return score_x, score_o

    def calculate_total_score(self) -> Tuple[int, int]:
        """
        Calculate total score for entire board.

        Returns:
            (total_score_x, total_score_o)
        """
        score_x = 0
        score_o = 0

        # Check all possible 4-in-a-rows on the board
        directions = [(0, 1), (1, 0), (1, 1), (1, -1)]

        checked_lines = set()

        for row in range(self.board.rows):
            for col in range(self.board.cols):
                for dr, dc in directions:
                    positions = [
                        (row + dr * i, col + dc * i)
                        for i in range(self.connect_length)
                    ]

                    # Check bounds
                    if all(
                            0 <= r < self.board.rows and 0 <= c < self.board.cols
                            for r, c in positions
                    ):
                        # Create a unique key for this line
                        line_key = tuple(sorted(positions))

                        if line_key not in checked_lines:
                            checked_lines.add(line_key)
                            result = self.check_line(positions)
                            if result == Player.X:
                                score_x += 1
                            elif result == Player.O:
                                score_o += 1

        return score_x, score_o


class Connect4Simulator(BaseGameSimulator):
    """
    Simulates Connect Four games for MCTS.

    This is a lightweight simulator focused on speed for simulations.
    """

    def __init__(
            self,
            board: Connect4Board,
            current_player: Player,
            score_x: int = 0,
            score_o: int = 0,
            connect_length: int = 4
    ):
        """Initialize simulator with game state."""
        self.board = board.copy()
        self.current_player = current_player
        self.score_x = score_x
        self.score_o = score_o
        self.connect_length = connect_length
        self.winner = None  # Cache the winner once determined

    def copy(self) -> 'Connect4Simulator':
        """Create a deep copy of the simulator."""
        new_sim = Connect4Simulator(
            board=self.board.copy(),
            current_player=self.current_player,
            score_x=self.score_x,
            score_o=self.score_o,
            connect_length=self.connect_length
        )
        new_sim.winner = self.winner  # Preserve winner state
        return new_sim

    def make_move(self, col: int) -> bool:
        """
        Make a move and update scores.

        Returns:
            True if move was valid, False otherwise
        """
        pos = self.board.place_piece(col, self.current_player)

        if pos is None:
            return False

        row_placed = pos[0]

        # Update score
        calculator = Connect4ScoreCalculator(self.board, self.connect_length)
        score_x_inc, score_o_inc = calculator.calculate_score_for_position(
            row_placed, col
        )

        self.score_x += score_x_inc
        self.score_o += score_o_inc

        # Check for a winner at the position we just placed (fast check)
        if self.board.check_win_at_position(row_placed, col, self.connect_length):
            self.winner = self.current_player

        # Switch player
        self.current_player = self.current_player.opponent()

        return True

    def is_game_over(self) -> bool:
        """Check if game is over (someone won or board is full)."""
        return self.winner is not None or self.board.is_full()

    def get_winner(self) -> Optional[Player]:
        """
        Get the winner.

        Returns:
            Player.X or Player.O if there's a winner (4-in-a-row), None for tie/no winner
        """
        # Return the winner if someone got 4-in-a-row
        if self.winner is not None:
            return self.winner

        # Fallback to score-based system if board is full (shouldn't happen in normal play)
        if self.board.is_full():
            if self.score_x > self.score_o:
                return Player.X
            elif self.score_o > self.score_x:
                return Player.O

        return None  # No winner yet or tie

    def get_valid_moves(self) -> List[int]:
        """Get list of valid column indices."""
        return self.board.get_valid_columns()

    def get_game_type(self) -> str:
        """Get game type identifier."""
        return "connect4"

    def get_preferred_move(self, valid_moves: List[int]) -> Optional[int]:
        """
        Get preferred move for simulations (center column preference).

        Args:
            valid_moves: List of valid column indices

        Returns:
            Center column if valid, None otherwise
        """
        center_col = self.board.cols // 2
        if center_col in valid_moves:
            return center_col
        return None

    def find_winning_move(self) -> Optional[int]:
        """
        Find a move that wins the game immediately.

        Returns:
            Column index if winning move exists, None otherwise
        """
        for col in self.get_valid_moves():
            sim = self.copy()
            pos = sim.board.place_piece(col, self.current_player)
            if pos is None:
                continue

            # Check if this move wins
            if sim.board.check_win_at_position(pos[0], col, self.connect_length):
                return col

        return None

    def find_blocking_move(self) -> Optional[int]:
        """
        Find a move that blocks opponent from winning next turn.

        Returns:
            Column index if blocking move needed, None otherwise
        """
        opponent = self.current_player.opponent()
        blocking_moves = []

        for col in self.get_valid_moves():
            sim = self.copy()
            # Simulate opponent playing this move
            pos = sim.board.place_piece(col, opponent)
            if pos is None:
                continue

            # Check if opponent would win
            if sim.board.check_win_at_position(pos[0], col, self.connect_length):
                blocking_moves.append(col)

        # Return first blocking move (if multiple threats exist, we can only block one)
        return blocking_moves[0] if blocking_moves else None

    def find_all_blocking_moves(self) -> List[int]:
        """
        Find ALL moves that block opponent from winning next turn.

        Returns:
            List of column indices that block opponent threats (empty if no threats)
        """
        opponent = self.current_player.opponent()
        blocking_moves = []

        for col in self.get_valid_moves():
            sim = self.copy()
            # Simulate opponent playing this move
            pos = sim.board.place_piece(col, opponent)
            if pos is None:
                continue

            # Check if opponent would win
            if sim.board.check_win_at_position(pos[0], col, self.connect_length):
                blocking_moves.append(col)

        return blocking_moves

    def find_best_immediate_move(self) -> Optional[Tuple[int, int]]:
        """
        Find move that immediately scores the most points.

        Returns:
            (column, score_increase) if a scoring move exists, None otherwise
        """
        best_col = None
        best_score = 0

        for col in self.get_valid_moves():
            # Simulate the move
            sim = self.copy()
            pos = sim.board.place_piece(col, self.current_player)

            if pos is None:
                continue

            # Calculate score increase
            row_placed = pos[0]
            calculator = Connect4ScoreCalculator(sim.board, self.connect_length)
            score_x_inc, score_o_inc = calculator.calculate_score_for_position(row_placed, col)

            # Get score for current player
            if self.current_player == Player.X:
                score_inc = score_x_inc
            else:
                score_inc = score_o_inc

            if score_inc > best_score:
                best_score = score_inc
                best_col = col

        if best_col is not None and best_score > 0:
            return (best_col, best_score)
        return None

    @staticmethod
    def from_json(state_dict: dict) -> 'Connect4Simulator':
        """
        Create simulator from JSON game state.

        Args:
            state_dict: Dictionary with game state from API
                       Expected fields: board, current_player, column_positions, score_x, score_o

        Returns:
            Connect4Simulator instance
        """
        # Standard Connect Four dimensions
        rows = 6
        cols = 7
        connect_length = 4

        # Create board
        board = Connect4Board(rows, cols)

        # Fill board from JSON
        board_data = state_dict['board']
        for row_idx, row in enumerate(board_data):
            for col_idx, cell in enumerate(row):
                board.grid[row_idx][col_idx] = Player.from_string(cell)

        # Set column positions
        board.column_positions = state_dict['column_positions'].copy()

        # Get current player
        current_player = Player.from_string(state_dict['current_player'])

        # Get scores
        score_x = state_dict.get('score_x', 0)
        score_o = state_dict.get('score_o', 0)

        return Connect4Simulator(
            board=board,
            current_player=current_player,
            score_x=score_x,
            score_o=score_o,
            connect_length=connect_length
        )
