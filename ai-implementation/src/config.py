"""
Configuration for MCTS AI Agent

Game-specific AI configurations for different difficulty levels.
"""

from enum import IntEnum


class Difficulty(IntEnum):
    """Difficulty levels for AI."""
    EASY = 1
    MEDIUM = 2
    HARD = 3


class AIConfig:
    """Configuration for AI agent."""

    # Game-specific difficulty profiles
    DIFFICULTY_PROFILES = {
        'connect4': {
            Difficulty.EASY: {
                "max_iterations": 50,
                "exploration_constant": 1.41,
                "mistake_probability": 0.2,  # 20% chance of making a random move
            },
            Difficulty.MEDIUM: {
                "max_iterations": 125,
                "exploration_constant": 1.41,
                "mistake_probability": 0.01,  # 1% mistakes
            },
            Difficulty.HARD: {
                "max_iterations": 750,
                "exploration_constant": 1.41,
                "mistake_probability": 0.0,  # No intentional mistakes
            }
        },
        'tictactoe': {
            Difficulty.EASY: {
                "max_iterations": 20,
                "exploration_constant": 1.41,
                "mistake_probability": 0.3,  # 30% chance of random move
            },
            Difficulty.MEDIUM: {
                "max_iterations": 100,
                "exploration_constant": 1.41,
                "mistake_probability": 0.05,  # 5% mistakes
            },
            Difficulty.HARD: {
                "max_iterations": 500,
                "exploration_constant": 1.41,
                "mistake_probability": 0.0,  # No intentional mistakes
            }
        }
    }

    # Server configuration
    SERVER_HOST = '0.0.0.0'
    SERVER_PORT = 5000
    DEBUG_MODE = True

    @classmethod
    def get_profile(cls, game_type: str, difficulty: int) -> dict:
        """
        Get difficulty profile for specific game and difficulty level.

        Args:
            game_type: Game type ('connect4', 'tictactoe')
            difficulty: Difficulty level (1=Easy, 2=Medium, 3=Hard)

        Returns:
            Difficulty profile dictionary

        Raises:
            ValueError: If game_type or difficulty is invalid
        """
        game_type = game_type.lower()

        if game_type not in cls.DIFFICULTY_PROFILES:
            supported = ', '.join(cls.DIFFICULTY_PROFILES.keys())
            raise ValueError(
                f"Unsupported game_type: '{game_type}'. "
                f"Supported games: {supported}"
            )

        if difficulty not in [Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD]:
            raise ValueError(
                f"Invalid difficulty: {difficulty}. "
                f"Must be {Difficulty.EASY} (Easy), {Difficulty.MEDIUM} (Medium), or {Difficulty.HARD} (Hard)"
            )

        return cls.DIFFICULTY_PROFILES[game_type][difficulty]


