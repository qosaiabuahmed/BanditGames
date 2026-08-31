"""
Policy Model CNN Wrapper for Connect Four

Loads CNN-based policy models and handles input reshaping.
"""

import numpy as np
from tensorflow import keras
from typing import Tuple, Optional


class PolicyModelCNN:
    """
    Wrapper for CNN-based policy model that handles 2D board input.

    The CNN model expects input shape (batch, rows, cols, 1) instead of
    the flattened (batch, rows*cols) used by dense models.
    """

    def __init__(self, rows: int = 6, cols: int = 7):
        """
        Initialize CNN policy model wrapper.

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

    def predict_single(self, board: np.ndarray) -> np.ndarray:
        """
        Predict move probabilities for a single board state.

        Args:
            board: Single board state (rows, cols) or (input_size,)

        Returns:
            Move probabilities (cols,)
        """
        if self.model is None:
            raise ValueError("Model not loaded")

        # Ensure 2D shape for CNN
        if board.ndim == 1:
            board = board.reshape(self.rows, self.cols)

        # Add batch and channel dimensions: (1, rows, cols, 1)
        board = board.reshape(1, self.rows, self.cols, 1)

        probs = self.model.predict(board, verbose=0)
        return probs[0]

    def get_best_move(
        self,
        board: np.ndarray,
        legal_moves: Optional[list] = None
    ) -> Tuple[int, float]:
        """
        Get the best move for a board state.

        Args:
            board: Board state
            legal_moves: List of legal column indices (optional)

        Returns:
            Tuple of (best_column, probability)
        """
        probs = self.predict_single(board)

        # Mask illegal moves if specified
        if legal_moves is not None:
            masked_probs = np.zeros_like(probs)
            masked_probs[legal_moves] = probs[legal_moves]
            # Renormalize
            if masked_probs.sum() > 0:
                masked_probs /= masked_probs.sum()
            probs = masked_probs

        best_col = np.argmax(probs)
        best_prob = probs[best_col]

        return int(best_col), float(best_prob)
