import pytest
from src.tic_tac_toe import TicTacToe, Move, parse_move, render_board, EMPTY_CELL


class TestTicTacToeInitialization:
    """Tests for board initialization and reset."""

    def test_default_board_size(self):
        game = TicTacToe()
        assert game.size == 3
        assert len(game.board) == 3
        assert all(len(row) == 3 for row in game.board)

    def test_custom_board_size(self):
        game = TicTacToe(size=5)
        assert game.size == 5
        assert len(game.board) == 5
        assert all(len(row) == 5 for row in game.board)

    def test_initial_board_is_empty(self):
        game = TicTacToe()
        for row in game.board:
            for cell in row:
                assert cell == EMPTY_CELL

    def test_initial_player_is_x(self):
        game = TicTacToe()
        assert game.current_player == "x"

    def test_invalid_board_size_raises_error(self):
        with pytest.raises(ValueError, match="Board size must be positive"):
            TicTacToe(size=0)

        with pytest.raises(ValueError, match="Board size must be positive"):
            TicTacToe(size=-1)

    def test_reset_clears_board(self):
        game = TicTacToe()
        game.make_move(Move(0, 0))
        game.make_move(Move(1, 1))
        game.current_player = "o"

        game.reset()

        assert all(cell == EMPTY_CELL for row in game.board for cell in row)
        assert game.current_player == "x"


class TestCellOperations:
    """Tests for cell-related operations."""

    def test_get_cell_returns_correct_value(self):
        game = TicTacToe()
        assert game.get_cell(0, 0) == EMPTY_CELL

        game.board[1][2] = "x"
        assert game.get_cell(1, 2) == "x"

    def test_get_cell_out_of_bounds_raises_error(self):
        game = TicTacToe()

        with pytest.raises(IndexError, match="out of bounds"):
            game.get_cell(3, 0)

        with pytest.raises(IndexError, match="out of bounds"):
            game.get_cell(0, 3)

        with pytest.raises(IndexError, match="out of bounds"):
            game.get_cell(-1, 0)

    def test_is_cell_empty_returns_true_for_empty_cell(self):
        game = TicTacToe()
        assert game.is_cell_empty(0, 0) is True

    def test_is_cell_empty_returns_false_for_occupied_cell(self):
        game = TicTacToe()
        game.board[0][0] = "x"
        assert game.is_cell_empty(0, 0) is False

    def test_is_cell_empty_out_of_bounds_raises_error(self):
        game = TicTacToe()

        with pytest.raises(IndexError, match="out of bounds"):
            game.is_cell_empty(3, 0)


class TestMakingMoves:
    """Tests for making moves on the board."""

    def test_make_move_on_empty_cell_succeeds(self):
        game = TicTacToe()
        result = game.make_move(Move(0, 0))

        assert result is True
        assert game.board[0][0] == "x"

    def test_make_move_on_occupied_cell_fails(self):
        game = TicTacToe()
        game.make_move(Move(0, 0))
        result = game.make_move(Move(0, 0))

        assert result is False

    def test_make_move_out_of_bounds_fails(self):
        game = TicTacToe()

        assert game.make_move(Move(3, 0)) is False
        assert game.make_move(Move(0, 3)) is False
        assert game.make_move(Move(-1, 0)) is False

    def test_make_move_with_explicit_player(self):
        game = TicTacToe()
        game.make_move(Move(0, 0), player="o")

        assert game.board[0][0] == "o"

    def test_make_move_uses_current_player_by_default(self):
        game = TicTacToe()
        game.current_player = "o"
        game.make_move(Move(0, 0))

        assert game.board[0][0] == "o"


class TestPlayerSwitching:
    """Tests for switching between players."""

    def test_switch_player_from_x_to_o(self):
        game = TicTacToe()
        assert game.current_player == "x"

        game.switch_player()
        assert game.current_player == "o"

    def test_switch_player_from_o_to_x(self):
        game = TicTacToe()
        game.current_player = "o"

        game.switch_player()
        assert game.current_player == "x"

    def test_switch_player_multiple_times(self):
        game = TicTacToe()

        game.switch_player()
        assert game.current_player == "o"

        game.switch_player()
        assert game.current_player == "x"

        game.switch_player()
        assert game.current_player == "o"


class TestWinnerDetection:
    """Tests for detecting winning conditions."""

    def test_winning_row(self):
        game = TicTacToe()
        # Fill top row with x
        game.board[0][0] = "x"
        game.board[0][1] = "x"
        game.board[0][2] = "x"

        assert game.has_winner("x") is True
        assert game.has_winner("o") is False

    def test_winning_column(self):
        game = TicTacToe()
        # Fill left column with o
        game.board[0][0] = "o"
        game.board[1][0] = "o"
        game.board[2][0] = "o"

        assert game.has_winner("o") is True
        assert game.has_winner("x") is False

    def test_winning_main_diagonal(self):
        game = TicTacToe()
        # Fill main diagonal with x
        game.board[0][0] = "x"
        game.board[1][1] = "x"
        game.board[2][2] = "x"

        assert game.has_winner("x") is True
        assert game.has_winner("o") is False

    def test_winning_anti_diagonal(self):
        game = TicTacToe()
        # Fill anti-diagonal with o
        game.board[0][2] = "o"
        game.board[1][1] = "o"
        game.board[2][0] = "o"

        assert game.has_winner("o") is True
        assert game.has_winner("x") is False

    def test_no_winner_on_empty_board(self):
        game = TicTacToe()

        assert game.has_winner("x") is False
        assert game.has_winner("o") is False

    def test_no_winner_with_incomplete_lines(self):
        game = TicTacToe()
        game.board[0][0] = "x"
        game.board[0][1] = "x"
        game.board[1][0] = "o"

        assert game.has_winner("x") is False
        assert game.has_winner("o") is False

    def test_winning_row_on_larger_board(self):
        game = TicTacToe(size=5)
        # Fill middle row with x
        for col in range(5):
            game.board[2][col] = "x"

        assert game.has_winner("x") is True

    def test_winning_column_on_larger_board(self):
        game = TicTacToe(size=4)
        # Fill last column with o
        for row in range(4):
            game.board[row][3] = "o"

        assert game.has_winner("o") is True


class TestBoardFullness:
    """Tests for detecting when the board is full."""

    def test_empty_board_is_not_full(self):
        game = TicTacToe()
        assert game.is_full() is False

    def test_partially_filled_board_is_not_full(self):
        game = TicTacToe()
        game.board[0][0] = "x"
        game.board[1][1] = "o"

        assert game.is_full() is False

    def test_completely_filled_board_is_full(self):
        game = TicTacToe()
        # Fill entire board
        for row in range(3):
            for col in range(3):
                game.board[row][col] = "x" if (row + col) % 2 == 0 else "o"

        assert game.is_full() is True

    def test_board_with_one_empty_cell_is_not_full(self):
        game = TicTacToe()
        # Fill all cells except one
        for row in range(3):
            for col in range(3):
                if row != 2 or col != 2:
                    game.board[row][col] = "x"

        assert game.is_full() is False


class TestParseMove:
    """Tests for the parse_move function."""

    def test_parse_valid_move(self):
        move = parse_move("11")
        assert move is not None
        assert move.row == 0
        assert move.col == 0

    def test_parse_different_positions(self):
        move = parse_move("23")
        assert move is not None
        assert move.row == 1
        assert move.col == 2

        move = parse_move("33")
        assert move is not None
        assert move.row == 2
        assert move.col == 2

    def test_parse_with_whitespace(self):
        move = parse_move("  12  ")
        assert move is not None
        assert move.row == 0
        assert move.col == 1

    def test_parse_invalid_length_returns_none(self):
        assert parse_move("1") is None
        assert parse_move("123") is None
        assert parse_move("") is None

    def test_parse_non_digit_returns_none(self):
        assert parse_move("ab") is None
        assert parse_move("1a") is None
        assert parse_move("a1") is None

    def test_parse_zero_position_returns_none(self):
        # 0-based indexing should reject 0 input (since input is 1-based)
        move = parse_move("01")
        # This actually creates row=-1, which is invalid for game logic
        # but parse_move itself doesn't validate board bounds
        assert move is None

        move = parse_move("10")
        assert move is None


class TestRenderBoard:
    """Tests for the render_board function."""

    def test_render_empty_board(self):
        board = [[EMPTY_CELL for _ in range(3)] for _ in range(3)]
        result = render_board(board)

        assert "# 1 2 3" in result
        assert "1 - - -" in result
        assert "2 - - -" in result
        assert "3 - - -" in result

    def test_render_board_with_moves(self):
        board = [[EMPTY_CELL for _ in range(3)] for _ in range(3)]
        board[0][0] = "x"
        board[1][1] = "o"

        result = render_board(board)

        assert "1 x - -" in result
        assert "2 - o -" in result

    def test_render_larger_board(self):
        board = [[EMPTY_CELL for _ in range(5)] for _ in range(5)]
        result = render_board(board)

        assert "# 1 2 3 4 5" in result
        assert "5 - - - - -" in result


class TestGameScenarios:
    """Tests for complete game scenarios."""

    def test_complete_game_x_wins(self):
        game = TicTacToe()

        # X wins on top row
        game.make_move(Move(0, 0), player="x")
        game.make_move(Move(1, 0), player="o")
        game.make_move(Move(0, 1), player="x")
        game.make_move(Move(1, 1), player="o")
        game.make_move(Move(0, 2), player="x")

        assert game.has_winner("x") is True
        assert game.has_winner("o") is False

    def test_complete_game_draw(self):
        game = TicTacToe()

        # Create a draw scenario
        # x x o
        # o o x
        # x o x
        moves = [
            (Move(0, 0), "x"),
            (Move(0, 1), "x"),
            (Move(0, 2), "o"),
            (Move(1, 0), "o"),
            (Move(1, 1), "o"),
            (Move(1, 2), "x"),
            (Move(2, 0), "x"),
            (Move(2, 1), "o"),
            (Move(2, 2), "x"),
        ]

        for move, player in moves:
            game.make_move(move, player=player)

        assert game.is_full() is True
        assert game.has_winner("x") is False
        assert game.has_winner("o") is False

    def test_game_flow_with_player_switching(self):
        game = TicTacToe()

        assert game.current_player == "x"
        game.make_move(Move(0, 0))
        game.switch_player()

        assert game.current_player == "o"
        game.make_move(Move(1, 1))
        game.switch_player()

        assert game.current_player == "x"
        assert game.board[0][0] == "x"
        assert game.board[1][1] == "o"
