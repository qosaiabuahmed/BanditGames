"""
Base Game Simulator for AI Container

Abstract base class and shared enums for all game implementations.
"""

from abc import ABC, abstractmethod
from enum import Enum
from typing import List, Optional


class Player(Enum):
    """Player representation (shared across all games)."""
    NONE = '.'
    X = 'X'
    O = 'O'

    @staticmethod
    def from_string(s: str) -> 'Player':
        """Convert string to Player enum (case-insensitive, handles '0' as 'O')."""
        s_upper = str(s).upper()
        # Handle common typo: '0' (zero) instead of 'O' (letter)
        if s_upper == 'X':
            return Player.X
        elif s_upper == 'O' or s == '0':
            return Player.O
        else:
            return Player.NONE

    def opponent(self) -> 'Player':
        """Get the opponent player."""
        if self == Player.X:
            return Player.O
        elif self == Player.O:
            return Player.X
        else:
            return Player.NONE


class BaseGameSimulator(ABC):
    """
    Abstract base class for game simulators.

    All game implementations must inherit from this class and implement
    the required methods. This ensures MCTS can work with any game.
    """

    @abstractmethod
    def copy(self) -> 'BaseGameSimulator':
        """
        Create a deep copy of the game state.

        Returns:
            A new instance with identical game state
        """
        pass

    @abstractmethod
    def make_move(self, move: int) -> bool:
        """
        Execute a move and update game state.

        Args:
            move: Move to execute (game-specific representation)
                  - Connect4: column index (0-6)
                  - TicTacToe: position index (0-8)

        Returns:
            True if move was valid and executed, False otherwise
        """
        pass

    @abstractmethod
    def is_game_over(self) -> bool:
        """
        Check if the game has ended (win or tie).

        Returns:
            True if game is over, False otherwise
        """
        pass

    @abstractmethod
    def get_winner(self) -> Optional[Player]:
        """
        Get the winner of the game.

        Returns:
            Player.X or Player.O if there's a winner
            None if game is tied or still ongoing
        """
        pass

    @abstractmethod
    def get_valid_moves(self) -> List[int]:
        """
        Get list of all valid moves in current position.

        Returns:
            List of valid move indices (game-specific representation)
        """
        pass

    @abstractmethod
    def get_game_type(self) -> str:
        """
        Get the game type identifier.

        Returns:
            Game type string ('connect4', 'tictactoe', etc.)
        """
        pass

    @staticmethod
    @abstractmethod
    def from_json(state_dict: dict) -> 'BaseGameSimulator':
        """
        Create game simulator from JSON state.

        Args:
            state_dict: Dictionary with game state from API

        Returns:
            Game simulator instance
        """
        pass

    # Optional methods with default implementations

    def get_preferred_move(self, valid_moves: List[int]) -> Optional[int]:
        """
        Get preferred move for simulation heuristic (optional).

        This is used during MCTS random playouts to add domain knowledge.
        Default implementation returns None (pure random playouts).

        Args:
            valid_moves: List of valid moves to choose from

        Returns:
            Preferred move index, or None for pure random selection
        """
        return None

    def find_winning_move(self) -> Optional[int]:
        """
        Find a move that wins the game immediately (optional).

        Used for tactical play at higher difficulties.
        Default implementation returns None (not implemented).

        Returns:
            Move that wins immediately, or None if no such move exists
        """
        return None

    def find_blocking_move(self) -> Optional[int]:
        """
        Find a move that blocks opponent from winning next turn (optional).

        Used for tactical play at higher difficulties.
        Default implementation returns None (not implemented).

        Returns:
            Move that blocks opponent threat, or None if no threat exists
        """
        return None