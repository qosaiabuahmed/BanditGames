"""
Value Model CNN Wrapper for Connect Four

Loads CNN-based value models and handles input reshaping.
"""

import numpy as np
from tensorflow import keras
from typing import Optional


class ValueModelCNN:
    """
    Wrapper for CNN-based value model that handles 2D board input.

    The CNN model expects input shape (batch, rows, cols, 1) instead of
    the flattened (batch, rows*cols) used by dense models.
    """

    def __init__(self, rows: int = 6, cols: int = 7):
        """
        Initialize CNN value model wrapper.

        Args:
            rows: Board rows
            cols: Board columns
        """
        self.rows = rows
        self.cols = cols
        self.model: Optional[keras.Model] = None

    def load(self, filepath: str):
        """Load CNN model from file."""
        self.model = keras.models.load_model(filepath)
        print(f"CNN model loaded from {filepath}")

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

        # Ensure 2D shape for CNN
        if board.ndim == 1:
            board = board.reshape(self.rows, self.cols)

        # Add batch and channel dimensions: (1, rows, cols, 1)
        board = board.reshape(1, self.rows, self.cols, 1)

        prob = self.model.predict(board, verbose=0)
        return float(prob[0][0])

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
