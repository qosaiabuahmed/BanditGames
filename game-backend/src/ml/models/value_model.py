"""
Value Model for Connect Four

Predicts win probability given a board state.
Trained on game outcomes from MCTS gameplay data.
"""

import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from typing import Optional


class ValueModel:
    """
    Neural network model for predicting game outcome probability.

    Architecture:
    - Input: Flattened board state (42 features for 6x7 board)
    - Hidden layers: Fully connected with ReLU activation
    - Output: Single value in [-1, 1] representing win probability
              -1 = certain loss, 0 = tie/neutral, +1 = certain win
    """

    def __init__(
        self,
        rows: int = 6,
        cols: int = 7,
        hidden_layers: list = [128, 64, 32],
        dropout_rate: float = 0.2,
        # === NEW: CNN architecture parameters ===
        # Default values are the ORIGINAL architecture used for v1/v2
        # For v3 with larger dataset, use larger values (e.g., [128, 128, 64])
        cnn_filters: list = [64, 64, 32],  # Number of filters in each conv layer
        cnn_dense_units: list = [128, 64]  # Dense layer sizes after conv layers
        # =========================================
    ):
        """
        Initialize value model.

        Args:
            rows: Board rows
            cols: Board columns
            hidden_layers: List of hidden layer sizes (for non-CNN models)
            dropout_rate: Dropout rate for regularization
            cnn_filters: List of filter counts for CNN conv layers (default: [64, 64, 32] for v1/v2)
            cnn_dense_units: List of dense layer sizes for CNN (default: [128, 64] for v1/v2)
        """
        self.rows = rows
        self.cols = cols
        self.input_size = rows * cols
        self.hidden_layers = hidden_layers
        self.dropout_rate = dropout_rate

        # === NEW: Store CNN architecture parameters ===
        self.cnn_filters = cnn_filters
        self.cnn_dense_units = cnn_dense_units
        # ==============================================

        self.model: Optional[keras.Model] = None

    def build_model(self) -> keras.Model:
        """
        Build the value network.

        Returns:
            Compiled Keras model
        """
        model = keras.Sequential(name="value_model")

        # Input layer
        model.add(layers.Input(shape=(self.input_size,), name="board_input"))

        # Hidden layers with dropout
        for i, units in enumerate(self.hidden_layers):
            model.add(layers.Dense(
                units,
                activation='relu',
                name=f'hidden_{i+1}'
            ))
            model.add(layers.Dropout(self.dropout_rate, name=f'dropout_{i+1}'))

        # Output layer: single value with tanh activation
        # tanh outputs in range [-1, 1]
        model.add(layers.Dense(
            1,
            activation='tanh',
            name='win_probability'
        ))

        # Compile with mean squared error loss
        model.compile(
            optimizer='adam',
            loss='mse',
            metrics=['mae']
        )

        self.model = model
        return model

    def build_cnn_model(self) -> keras.Model:
        """
        Build a CNN-based value network (alternative architecture).

        CNNs can better capture spatial patterns in the board.

        Returns:
            Compiled Keras model
        """
        model = keras.Sequential(name="value_cnn_model")

        # Input: reshaped board
        model.add(layers.Input(shape=(self.rows, self.cols, 1), name="board_input"))

        # === MODIFIED: Convolutional layers now use configurable filter counts ===
        # OLD (v1/v2): Conv2D(64), Conv2D(64), Conv2D(32) - hardcoded
        # NEW: Uses self.cnn_filters (default [64, 64, 32] for v1/v2, [128, 128, 64] for v3)
        for i, filters in enumerate(self.cnn_filters):
            model.add(layers.Conv2D(
                filters, (3, 3),
                activation='relu',
                padding='same',
                name=f'conv{i+1}'
            ))
        # ==========================================================================

        # Flatten for dense layers
        model.add(layers.Flatten(name='flatten'))

        # === MODIFIED: Dense layers now use configurable units ===
        # OLD (v1/v2): Dense(128), Dense(64) - hardcoded
        # NEW: Uses self.cnn_dense_units (default [128, 64] for v1/v2, [256, 128] for v3)
        for i, units in enumerate(self.cnn_dense_units):
            model.add(layers.Dense(units, activation='relu', name=f'dense{i+1}'))
            model.add(layers.Dropout(self.dropout_rate, name=f'dropout{i+1}'))
        # ==========================================================

        # Output layer
        model.add(layers.Dense(
            1,
            activation='tanh',
            name='win_probability'
        ))

        # Compile
        model.compile(
            optimizer='adam',
            loss='mse',
            metrics=['mae']
        )

        self.model = model
        return model

    def train(
        self,
        X_train: np.ndarray,
        y_train: np.ndarray,
        X_val: np.ndarray,
        y_val: np.ndarray,
        epochs: int = 50,
        batch_size: int = 32,
        callbacks: Optional[list] = None
    ) -> keras.callbacks.History:
        """
        Train the value model.

        Args:
            X_train: Training features (N, input_size)
            y_train: Training labels (N,) - game outcomes in [-1, 0, 1]
            X_val: Validation features
            y_val: Validation labels
            epochs: Number of training epochs
            batch_size: Batch size
            callbacks: Optional Keras callbacks

        Returns:
            Training history
        """
        if self.model is None:
            raise ValueError("Must call build_model() first")

        if callbacks is None:
            callbacks = [
                keras.callbacks.EarlyStopping(
                    monitor='val_loss',
                    patience=10,
                    restore_best_weights=True
                ),
                keras.callbacks.ReduceLROnPlateau(
                    monitor='val_loss',
                    factor=0.5,
                    patience=5,
                    min_lr=1e-6
                )
            ]

        history = self.model.fit(
            X_train, y_train,
            validation_data=(X_val, y_val),
            epochs=epochs,
            batch_size=batch_size,
            callbacks=callbacks,
            verbose=1
        )

        return history

    def predict(self, X: np.ndarray) -> np.ndarray:
        """
        Predict win probabilities for board states.

        Args:
            X: Board states (N, input_size)

        Returns:
            Win probabilities (N,) in range [-1, 1]
        """
        if self.model is None:
            raise ValueError("Model not built or loaded")

        predictions = self.model.predict(X, verbose=0)
        return predictions.flatten()

    def predict_single(self, board: np.ndarray) -> float:
        """
        Predict win probability for a single board state.

        Args:
            board: Single board state (rows, cols) or (input_size,)

        Returns:
            Win probability in [-1, 1]
        """
        # Ensure 1D input
        if board.ndim == 2:
            board = board.flatten()

        # Add batch dimension
        board = board.reshape(1, -1)

        prob = self.predict(board)
        return float(prob[0])

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

    def save(self, filepath: str):
        """Save model to file."""
        if self.model is None:
            raise ValueError("No model to save")
        self.model.save(filepath)
        print(f"Model saved to {filepath}")

    def load(self, filepath: str):
        """Load model from file."""
        self.model = keras.models.load_model(filepath)
        print(f"Model loaded from {filepath}")


def example_usage():
    """Example of creating and using the value model."""
    # Create model
    value_model = ValueModel(rows=6, cols=7, hidden_layers=[128, 64, 32])
    model = value_model.build_model()

    print("Value Model Summary:")
    model.summary()

    # Example prediction
    sample_board = np.random.randint(-1, 2, size=(6, 7))
    evaluation = value_model.evaluate_position(sample_board)

    print(f"\nExample position evaluation:")
    print(f"  Value: {evaluation['value']:.3f}")
    print(f"  Win percentage: {evaluation['win_percentage']:.1f}%")
    print(f"  Evaluation: {evaluation['evaluation']}")


if __name__ == "__main__":
    example_usage()
