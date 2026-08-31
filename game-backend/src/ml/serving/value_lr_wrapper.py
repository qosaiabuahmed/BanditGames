"""
Value Model LR Wrapper for Connect Four

Loads linear regression-based value models and handles predictions.
"""

import numpy as np
import joblib
from typing import Optional


class ValueModelLR:
    """
    Wrapper for LR-based value model.

    The LR model expects flattened board input (rows*cols,).
    """

    def __init__(self, rows: int = 6, cols: int = 7):
        """
        Initialize LR value model wrapper.

        Args:
            rows: Board rows
            cols: Board columns
        """
        self.rows = rows
        self.cols = cols
        self.model = None

    def load(self, filepath: str):
        """Load LR model from file."""
        self.model = joblib.load(filepath)
        print(f"LR model loaded from {filepath}")

    def predict_single(self, board: np.ndarray) -> float:
        """
        Predict win probability for a single board state.

        Args:
            board: Single board state (rows, cols) or (input_size,)

        Returns:
            Win probability in [-1, 1]
        """
        if self.model is None:
            raise ValueError("Model not loaded")

        # Ensure 1D input
        if board.ndim == 2:
            board = board.flatten()

        # Add batch dimension
        board = board.reshape(1, -1)

        # Get prediction from sklearn and clip to [-1, 1]
        prob = self.model.predict(board)
        prob = np.clip(prob[0], -1, 1)

        return float(prob)

    def evaluate_position(self, board: np.ndarray) -> dict:
        """
        Evaluate a board position with interpretation.

        Args:
            board: Board state

        Returns:
            Dictionary with evaluation details
        """
        value = self.predict_single(board)

        # Interpret the value
        if value > 0.5:
            evaluation = "Strong advantage"
        elif value > 0.2:
            evaluation = "Slight advantage"
        elif value > -0.2:
            evaluation = "Balanced position"
        elif value > -0.5:
            evaluation = "Slight disadvantage"
        else:
            evaluation = "Strong disadvantage"

        # Convert to win percentage
        win_percentage = (value + 1) / 2 * 100  # Map [-1,1] to [0,100]

        return {
            "value": float(value),
            "win_percentage": float(win_percentage),
            "evaluation": evaluation
        }
