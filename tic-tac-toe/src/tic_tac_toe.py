from __future__ import annotations

from dataclasses import dataclass
from typing import List, Optional


DEFAULT_SIZE = 3
EMPTY_CELL = "-"


@dataclass
class Move:
    row: int
    col: int


class TicTacToe:
    """Simple tic-tac-toe style game logic for a square grid."""

    def __init__(self, size: int = DEFAULT_SIZE) -> None:
        if size <= 0:
            raise ValueError("Board size must be positive.")
        self.size: int = size
        self.board: List[List[str]] = self._create_empty_board()
        self.current_player: str = "x"

    def _create_empty_board(self) -> List[List[str]]:
        return [[EMPTY_CELL for _ in range(self.size)] for _ in range(self.size)]

    def reset(self) -> None:
        """Reset the board and current player."""
        self.board = self._create_empty_board()
        self.current_player = "x"

    def get_cell(self, row: int, col: int) -> str:
        self._validate_coordinates(row, col)
        return self.board[row][col]

    def is_cell_empty(self, row: int, col: int) -> bool:
        self._validate_coordinates(row, col)
        return self.board[row][col] == EMPTY_CELL

    def make_move(self, move: Move, player: Optional[str] = None) -> bool:
        """
        Attempt to place a move for the given player.

        Returns True if the move was applied, False otherwise.
        """
        if player is None:
            player = self.current_player

        if not self._is_valid_move(move):
            return False

        self.board[move.row][move.col] = player
        return True

    def _is_valid_move(self, move: Move) -> bool:
        if not (0 <= move.row < self.size and 0 <= move.col < self.size):
            return False
        return self.is_cell_empty(move.row, move.col)

    def _validate_coordinates(self, row: int, col: int) -> None:
        if not (0 <= row < self.size and 0 <= col < self.size):
            raise IndexError("Cell coordinates are out of bounds.")

    def has_winner(self, player: str) -> bool:
        """Return True if the given player has a winning line."""
        return any(
            (
                self._has_winning_row(player),
                self._has_winning_column(player),
                self._has_winning_diagonal(player),
            )
        )

    def _has_winning_row(self, player: str) -> bool:
        for row in self.board:
            if all(cell == player for cell in row):
                return True
        return False

    def _has_winning_column(self, player: str) -> bool:
        for col in range(self.size):
            if all(self.board[row][col] == player for row in range(self.size)):
                return True
        return False

    def _has_winning_diagonal(self, player: str) -> bool:
        main_diag = all(self.board[i][i] == player for i in range(self.size))
        anti_diag = all(
            self.board[i][self.size - 1 - i] == player for i in range(self.size)
        )
        return main_diag or anti_diag

    def is_full(self) -> bool:
        """Return True if the board has no empty cells."""
        return all(
            cell != EMPTY_CELL for row in self.board for cell in row
        )

    def switch_player(self) -> None:
        self.current_player = "o" if self.current_player == "x" else "x"


def parse_move(raw: str) -> Optional[Move]:
    """
    Parse a move in 'rc' format (row and column, 1-based).

    Returns a Move on success, or None if the format is invalid.
    """
    raw = raw.strip()
    if len(raw) != 2 or not raw.isdigit():
        return None

    row = int(raw[0]) - 1
    col = int(raw[1]) - 1

    if row < 0 or col < 0:
        return None

    return Move(row=row, col=col)


def render_board(board: List[List[str]]) -> str:
    """
    Return a string representation of the board with row/column labels.
    Suitable for printing and unit testing.
    """
    size = len(board)
    header = "# " + " ".join(str(i + 1) for i in range(size))
    lines = [header]

    for row_index, row in enumerate(board, start=1):
        line = f"{row_index} " + " ".join(row)
        lines.append(line)

    return "\n".join(lines)


def run_cli_game() -> None:
    """
    Run a CLI game loop.

    This function contains all I/O, keeping game logic pure for testing.
    """
    game = TicTacToe(size=DEFAULT_SIZE)

    while True:
        print("\033c", end="")  # Clear screen (ANSI escape), portable alternative to system()
        print(render_board(game.board))
        print()

        raw_input_str = input(
            f"Player {game.current_player}, pick your position (rowcol, e.g., 12): "
        )
        move = parse_move(raw_input_str)

        if move is None or not game.make_move(move):
            print("Invalid move. Try again.")
            input("Press Enter to continue...")
            continue

        if game.has_winner(game.current_player):
            print("\033c", end="")
            print(render_board(game.board))
            print("-----------------------------")
            print(f"Player {game.current_player} wins!!!")
            print("-----------------------------")
            break

        if game.is_full():
            print("\033c", end="")
            print(render_board(game.board))
            print("-----------------------------")
            print("It's a draw!")
            print("-----------------------------")
            break

        game.switch_player()


if __name__ == "__main__":
    run_cli_game()
