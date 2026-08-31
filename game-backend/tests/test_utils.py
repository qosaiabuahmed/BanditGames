"""Tests for utility functions."""
import pytest
import sys
from pathlib import Path

# Add src to path
sys.path.insert(0, str(Path(__file__).parent.parent / 'src'))

from src.utils.utils import calculate_difficulty


class TestCalculateDifficulty:
    """Tests for calculate_difficulty function."""

    def test_low_rating_returns_easy(self):
        """Test that rating < 1000 returns difficulty 1 (Easy)."""
        assert calculate_difficulty(900, 0, 0) == 1
        assert calculate_difficulty(500, 0, 0) == 1
        assert calculate_difficulty(999, 0, 0) == 1

    def test_medium_rating_returns_medium(self):
        """Test that rating 1000-1050 returns difficulty 2 (Medium)."""
        assert calculate_difficulty(1000, 0, 0) == 2
        assert calculate_difficulty(1025, 0, 0) == 2
        assert calculate_difficulty(1050, 0, 0) == 2

    def test_high_rating_returns_hard(self):
        """Test that rating > 1050 returns difficulty 3 (Hard)."""
        assert calculate_difficulty(1051, 0, 0) == 3
        assert calculate_difficulty(1100, 0, 0) == 3
        assert calculate_difficulty(2000, 0, 0) == 3

    def test_player_winning_increases_difficulty(self):
        """Test that player winning by 2+ increases difficulty."""
        # Medium rating (2) + winning by 3 = Hard (3)
        assert calculate_difficulty(1000, 3, 0) == 3

        # Easy rating (1) + winning by 2 = Medium (2)
        assert calculate_difficulty(900, 2, 0) == 2

    def test_player_losing_decreases_difficulty(self):
        """Test that player losing by 2+ decreases difficulty."""
        # Medium rating (2) + losing by 3 = Easy (1)
        assert calculate_difficulty(1000, 0, 3) == 1

        # Hard rating (3) + losing by 2 = Medium (2)
        assert calculate_difficulty(1100, 0, 2) == 2

    def test_difficulty_clamped_at_boundaries(self):
        """Test that difficulty is clamped between 1 and 3."""
        # Can't go below 1
        assert calculate_difficulty(500, 0, 5) == 1

        # Can't go above 3
        assert calculate_difficulty(2000, 5, 0) == 3

    def test_small_score_difference_no_adjustment(self):
        """Test that score difference < 2 doesn't adjust difficulty."""
        assert calculate_difficulty(1000, 1, 0) == 2  # +1 diff, no change
        assert calculate_difficulty(1000, 0, 1) == 2  # -1 diff, no change
        assert calculate_difficulty(1000, 0, 0) == 2  # 0 diff, no change

    def test_edge_cases(self):
        """Test edge cases."""
        # Zero rating
        assert calculate_difficulty(0, 0, 0) == 1

        # Negative rating (shouldn't happen but should still work)
        assert calculate_difficulty(-100, 0, 0) == 1

        # Very high scores
        assert calculate_difficulty(1000, 100, 0) == 3
        assert calculate_difficulty(1000, 0, 100) == 1
