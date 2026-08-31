"""
Feature Engineering for Connect Four ML Models

Converts board states to ML-ready features.
"""

import numpy as np
from typing import Tuple, List


class FeatureExtractor:
    """
    Extracts and transforms features from Connect Four board states.

    Board encoding:
    - Empty (E) -> 0
    - X -> 1
    - O -> -1
    """

    def __init__(self, rows: int = 6, cols: int = 7):
        """
        Initialize feature extractor.

        Args:
            rows: Number of rows in board
            cols: Number of columns in board
        """
        self.rows = rows
        self.cols = cols
        self.board_size = rows * cols

    def board_to_features(self, board: np.ndarray) -> np.ndarray:
        """
        Convert board state to feature vector.

        Args:
            board: Board state array of shape (rows, cols)
                   Values: 0 (empty), 1 (X), -1 (O)

        Returns:
            Feature vector of shape (features,)
        """
        # For now, just flatten the board
        # Future: can add derived features (column heights, threats, etc.)
        return board.flatten()

    def boards_to_features(self, boards: np.ndarray) -> np.ndarray:
        """
        Convert multiple board states to feature vectors.

        Args:
            boards: Array of board states, shape (N, rows, cols)

        Returns:
            Feature matrix of shape (N, features)
        """
        return boards.reshape(len(boards), -1)

    def normalize_features(self, X: np.ndarray) -> np.ndarray:
        """
        Normalize features to [-1, 1] range.

        Since board values are already in {-1, 0, 1}, this is a no-op.
        Included for completeness and future extensions.

        Args:
            X: Feature matrix

        Returns:
            Normalized features
        """
        return X

    def add_derived_features(self, boards: np.ndarray) -> np.ndarray:
        """
        Add derived features to board representations.

        Derived features (optional enhancements):
        - Column heights (7 features)
        - Piece counts for X and O (2 features)
        - Threat counts (pieces with 2 or 3 in a row) (2 features)

        Args:
            boards: Array of board states, shape (N, rows, cols)

        Returns:
            Enhanced feature matrix with derived features appended
        """
        N = len(boards)
        base_features = self.boards_to_features(boards)

        # Column heights (how many pieces in each column)
        column_heights = np.array([
            self._get_column_heights(board)
            for board in boards
        ])  # Shape: (N, cols)

        # Piece counts
        piece_counts = np.array([
            [np.sum(board == 1), np.sum(board == -1)]  # X count, O count
            for board in boards
        ])  # Shape: (N, 2)

        # Concatenate all features
        enhanced = np.concatenate([
            base_features,
            column_heights,
            piece_counts
        ], axis=1)

        return enhanced

    def _get_column_heights(self, board: np.ndarray) -> np.ndarray:
        """
        Get the height (number of pieces) in each column.

        Args:
            board: Single board state (rows, cols)

        Returns:
            Array of column heights (cols,)
        """
        heights = []
        for col in range(self.cols):
            column = board[:, col]
            # Count non-zero (non-empty) cells
            height = np.sum(column != 0)
            heights.append(height)
        return np.array(heights)

    def prepare_for_cnn(self, boards: np.ndarray) -> np.ndarray:
        """
        Prepare board states for CNN models.

        Adds channel dimension for convolutional layers.

        Args:
            boards: Array of board states, shape (N, rows, cols)

        Returns:
            Board states with channel dimension, shape (N, rows, cols, 1)
        """
        return boards[..., np.newaxis]

    def prepare_for_cnn_multiplane(self, boards: np.ndarray) -> np.ndarray:
        """
        Prepare board states for CNN with separate planes for X and O.

        Creates 2-channel representation:
        - Channel 0: X pieces (1 where X, 0 elsewhere)
        - Channel 1: O pieces (1 where O, 0 elsewhere)

        Args:
            boards: Array of board states, shape (N, rows, cols)
                   Values: 0 (empty), 1 (X), -1 (O)

        Returns:
            Multi-plane board states, shape (N, rows, cols, 2)
        """
        N = len(boards)
        multiplane = np.zeros((N, self.rows, self.cols, 2))

        # Channel 0: X pieces
        multiplane[:, :, :, 0] = (boards == 1).astype(float)

        # Channel 1: O pieces
        multiplane[:, :, :, 1] = (boards == -1).astype(float)

        return multiplane


def example_usage():
    """Example of using the feature extractor."""
    # Create sample board
    board = np.array([
        [0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 1, 0, 0, 0],
        [0, 0, -1, 1, 0, 0, 0],
        [-1, 1, -1, 1, 1, -1, 0]
    ])

    extractor = FeatureExtractor(rows=6, cols=7)

    # Basic feature extraction (flatten)
    features = extractor.board_to_features(board)
    print(f"Basic features shape: {features.shape}")
    print(f"Features: {features}")

    # Batch processing
    boards = np.array([board, board, board])  # 3 boards
    batch_features = extractor.boards_to_features(boards)
    print(f"\nBatch features shape: {batch_features.shape}")

    # With derived features
    enhanced = extractor.add_derived_features(boards)
    print(f"Enhanced features shape: {enhanced.shape}")

    # For CNN
    cnn_input = extractor.prepare_for_cnn(boards)
    print(f"CNN input shape: {cnn_input.shape}")

    # Multi-plane CNN
    multiplane = extractor.prepare_for_cnn_multiplane(boards)
    print(f"Multi-plane CNN input shape: {multiplane.shape}")


if __name__ == "__main__":
    example_usage()
