"""
Policy Model LR Wrapper for Connect Four

Loads logistic regression-based policy models and handles predictions.
"""

import numpy as np
import joblib
from typing import Tuple, Optional


class PolicyModelLR:
    """
    Wrapper for LR-based policy model.

    The LR model expects flattened board input (rows*cols,).
    """

    def __init__(self, rows: int = 6, cols: int = 7):
        """
        Initialize LR policy model wrapper.

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

        # Ensure 1D input
        if board.ndim == 2:
            board = board.flatten()

        # Add batch dimension
        board = board.reshape(1, -1)

        # Get probabilities from sklearn
        probs = self.model.predict_proba(board)
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
