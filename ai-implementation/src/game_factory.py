"""
Game Factory for AI Container

Factory pattern for creating game simulators based on game type.
"""

from typing import Dict, Type
from src.base_game import BaseGameSimulator
from src.connect4_game import Connect4Simulator
from src.tictactoe_game import TicTacToeSimulator


class GameFactory:
    """
    Factory for creating game simulators.

    Uses registry pattern to allow easy addition of new games.
    """

    # Game registry: maps game_type string to simulator class
    _game_registry: Dict[str, Type[BaseGameSimulator]] = {
        'connect4': Connect4Simulator,
        'tictactoe': TicTacToeSimulator,
    }

    @classmethod
    def create_from_json(cls, state_dict: dict) -> BaseGameSimulator:
        """
        Create game simulator from JSON state.

        Args:
            state_dict: Dictionary with game state from API
                       Must include 'game_type' field

        Returns:
            Game simulator instance (Connect4Simulator or TicTacToeSimulator)

        Raises:
            ValueError: If game_type is missing or unsupported
        """
        # Get game type
        game_type = state_dict.get('game_type')

        if not game_type:
            raise ValueError("Missing 'game_type' field in game state")

        # Normalize to lowercase
        game_type = game_type.lower()

        # Get simulator class from registry
        simulator_class = cls._game_registry.get(game_type)

        if simulator_class is None:
            supported_games = ', '.join(cls._game_registry.keys())
            raise ValueError(
                f"Unsupported game_type: '{game_type}'. "
                f"Supported games: {supported_games}"
            )

        # Create and return simulator instance
        return simulator_class.from_json(state_dict)

    @classmethod
    def register_game(cls, game_type: str, simulator_class: Type[BaseGameSimulator]):
        """
        Register a new game type (for extensibility).

        Args:
            game_type: Game type identifier (e.g., 'chess', 'checkers')
            simulator_class: Class that implements BaseGameSimulator

        Example:
            GameFactory.register_game('chess', ChessSimulator)
        """
        cls._game_registry[game_type.lower()] = simulator_class

    @classmethod
    def get_supported_games(cls) -> list:
        """
        Get list of supported game types.

        Returns:
            List of game type strings
        """
        return list(cls._game_registry.keys())
