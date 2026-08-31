from enum import Enum
from typing import Optional, List, Tuple
from dataclasses import dataclass, field
from src.utils.utils import ScoreCalculator

class Player(Enum):
    NONE = '.'
    X = 'X'
    O = 'O'


@dataclass
class GameConfig:
    rows: int = 6
    cols: int = 7
    high_score_limit: int = 5
    connect_length: int = 4

    def __post_init__(self):
        if self.rows < 4 or self.cols < 4:
            raise ValueError("Board dimensions must be at least 4x4")
        if self.high_score_limit < 1:
            raise ValueError("High score limit must be positive")


@dataclass
class GameState:
    """Represents the current state of a game."""
    board: List[List[Player]]
    score_x: int = 0
    score_o: int = 0
    current_turn: int = 0
    current_player: Player = Player.X
    column_positions: List[int] = field(default_factory=list)
    move_history: List[int] = field(default_factory=list)


class Board:
    """Manages the game board and piece placement."""

    def __init__(self, rows: int, cols: int):
        """
        Initialize the game board.
        """
        self.rows = rows
        self.cols = cols
        self.grid = [[Player.NONE for _ in range(cols)] for _ in range(rows)]
        self.column_positions = [rows - 1 for _ in range(cols)]

    def is_valid_column(self, col: int) -> bool:
        """
        Check if a column is valid for placing a piece.

        Args:
            col: Column index (0-based)

        Returns:
            True if the column is valid and not full
        """
        return 0 <= col < self.cols and self.column_positions[col] >= 0

    def place_piece(self, col: int, player: Player) -> Optional[Tuple[int, int]]:
        """
        Place a piece in the specified column.

        Args:
            col: Column index (0-based)
            player: Player making the move

        Returns:
            Tuple of (row, col) where piece was placed, or None if invalid
        """
        if not self.is_valid_column(col):
            return None

        row = self.column_positions[col]
        self.grid[row][col] = player
        self.column_positions[col] -= 1
        return (row, col)

    def get_piece(self, row: int, col: int) -> Player:
        """
        Get the piece at a specific position.

        Args:
            row: Row index
            col: Column index

        Returns:
            Player at the position
        """
        if 0 <= row < self.rows and 0 <= col < self.cols:
            return self.grid[row][col]
        return Player.NONE

    def is_full(self) -> bool:
        """Check if the board is completely full."""
        return all(pos < 0 for pos in self.column_positions)

    def __str__(self) -> str:
        """Return a string representation of the board."""
        lines = []
        for row in self.grid:
            lines.append("| " + " | ".join(p.value for p in row) + " |")
            lines.append("-" * (4 * self.cols + 1))
        
        # Add column numbers
        col_numbers = "  " + "   ".join(str(i + 1) for i in range(self.cols))
        lines.append(col_numbers)
        
        return "\n".join(lines)


class GameEngine:
    """Main game engine managing game flow and rules."""

    def __init__(self, config: GameConfig, logger=None, game_id: Optional[int] = None):
        """
        Initialize the game engine.

        Args:
            config: Game configuration
            logger: Optional GameplayLogger instance for logging moves
            game_id: Optional game ID for database logging
        """
        self.config = config
        self.board = Board(config.rows, config.cols)
        self.score_x = 0
        self.score_o = 0
        self.move_history: List[int] = []
        self.current_turn = 0
        self.current_player = Player.X
        self.logger = logger
        self.game_id = game_id
        self.player_x_id: Optional[int] = None
        self.player_o_id: Optional[int] = None

    def get_current_player(self) -> Player:
        """Get the current player based on turn number."""
        return Player.X if self.current_turn % 2 == 0 else Player.O

    def make_move(self, col: int) -> bool:
        """
        Make a move in the specified column.

        Args:
            col: Column index (0-based)

        Returns:
            True if move was successful, False otherwise
        """
        player = self.get_current_player()

        # Capture state BEFORE move for logging
        if self.logger and self.game_id:
            board_before = [row[:] for row in self.board.grid]
            score_x_before = self.score_x
            score_o_before = self.score_o

            # Get legal moves
            legal_moves = [c for c in range(self.board.cols) if self.board.is_valid_column(c)]

        pos = self.board.place_piece(col, player)

        if pos is None:
            return False

        row_placed = pos[0]

        # Update score
        calculator = ScoreCalculator(self.board, self.config.connect_length)
        score_x_inc, score_o_inc = calculator.calculate_score_for_move(row_placed, col)
        self.score_x += score_x_inc
        self.score_o += score_o_inc

        # Capture state AFTER move
        if self.logger and self.game_id:
            board_after = [row[:] for row in self.board.grid]

            # Determine player ID
            current_player_id = self.player_x_id if player == Player.X else self.player_o_id

            if current_player_id:
                # Import here to avoid circular dependency
                from src.logs.gameplay_logger import MoveLog, board_to_serializable

                # Create move log
                move_log = MoveLog(
                    game_id=self.game_id,
                    player_id=current_player_id,
                    move_number=self.current_turn,
                    column_played=col,
                    row_placed=row_placed,
                    board_state_before=board_to_serializable(board_before),
                    board_state_after=board_to_serializable(board_after),
                    score_x_before=score_x_before,
                    score_o_before=score_o_before,
                    score_x_after=self.score_x,
                    score_o_after=self.score_o,
                    heuristic_value=None,
                    heuristic_value_x=None,
                    heuristic_value_o=None,
                    legal_moves=legal_moves
                )

                # Log the move
                try:
                    self.logger.log_move(move_log)
                except Exception as e:
                    print(f"Warning: Failed to log move: {e}")

        # Update game state
        self.move_history.append(col)
        self.current_turn += 1
        self.current_player = self.get_current_player()

        return True

    def is_game_over(self) -> bool:
        """
        Check if the game is over.

        Game ends when:
        - Any player has scored (made four in a row), OR
        - The board is completely full
        """
        return self.score_x > 0 or self.score_o > 0 or self.board.is_full()

    def get_winner(self) -> Optional[str]:
        """
        Determine the winner based on scores.

        Returns:
            "X", "O", or "Tie"
        """
        if self.score_x > self.score_o:
            return "X"
        elif self.score_o > self.score_x:
            return "O"
        else:
            return "Tie"

    def get_state(self) -> GameState:
        """Get the current game state."""
        return GameState(
            board=[row[:] for row in self.board.grid],
            score_x=self.score_x,
            score_o=self.score_o,
            current_turn=self.current_turn,
            current_player=self.current_player,
            column_positions=self.board.column_positions[:],
            move_history=self.move_history[:]
        )

    def load_state(self, state: GameState):
        """Load a game state."""
        self.board.grid = [row[:] for row in state.board]
        self.board.column_positions = state.column_positions[:]
        self.score_x = state.score_x
        self.score_o = state.score_o
        self.current_turn = state.current_turn
        self.current_player = state.current_player
        self.move_history = state.move_history[:]
