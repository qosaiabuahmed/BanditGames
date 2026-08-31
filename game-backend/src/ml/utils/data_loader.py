"""
Data Loader for Connect Four Training Data

Loads and prepares parquet files for ML training.
Handles train/validation/test splits and batching.
"""

import os
import json
import pandas as pd
import numpy as np
from pathlib import Path
from typing import Tuple, Optional, List, Dict, Any
from dataclasses import dataclass


@dataclass
class DatasetStats:
    """Statistics about loaded dataset."""
    total_moves: int
    total_games: int
    train_moves: int
    val_moves: int
    test_moves: int
    game_modes: Dict[str, int]
    winner_distribution: Dict[str, int]


class ConnectFourDataLoader:
    """
    Loads and manages Connect Four training data from parquet files.

    Provides train/validation/test splits and handles board state parsing.
    """

    def __init__(
        self,
        data_dir: str = "/app/data",
        train_split: float = 0.7,
        val_split: float = 0.15,
        test_split: float = 0.15,
        random_seed: int = 42
    ):
        """
        Initialize data loader.

        Args:
            data_dir: Directory containing parquet files
            train_split: Fraction of data for training
            val_split: Fraction of data for validation
            test_split: Fraction of data for testing
            random_seed: Random seed for reproducibility
        """
        self.data_dir = Path(data_dir)
        self.train_split = train_split
        self.val_split = val_split
        self.test_split = test_split
        self.random_seed = random_seed

        if not np.isclose(train_split + val_split + test_split, 1.0):
            raise ValueError("Splits must sum to 1.0")

        self.df: Optional[pd.DataFrame] = None
        self.train_df: Optional[pd.DataFrame] = None
        self.val_df: Optional[pd.DataFrame] = None
        self.test_df: Optional[pd.DataFrame] = None

    def load_data(
        self,
        file_pattern: str = "*.parquet",
        game_mode: Optional[str] = None
    ) -> pd.DataFrame:
        """
        Load parquet files from data directory.

        Args:
            file_pattern: Glob pattern for parquet files
            game_mode: Filter by game mode (e.g., 'ai_vs_ai')

        Returns:
            Loaded DataFrame
        """
        parquet_files = list(self.data_dir.glob(file_pattern))

        if not parquet_files:
            raise FileNotFoundError(
                f"No parquet files found matching '{file_pattern}' in {self.data_dir}"
            )

        print(f"Loading {len(parquet_files)} parquet file(s)...")

        # Load all parquet files
        dfs = [pd.read_parquet(f) for f in parquet_files]
        self.df = pd.concat(dfs, ignore_index=True)

        # Filter by game mode if specified
        if game_mode:
            original_len = len(self.df)
            self.df = self.df[self.df['game_mode'] == game_mode]
            print(f"Filtered to {len(self.df)} moves (from {original_len}) with game_mode='{game_mode}'")

        print(f"Loaded {len(self.df)} moves from {self.df['game_id'].nunique()} games")

        return self.df

    def parse_board_state(self, board_json: str) -> np.ndarray:
        """
        Parse board state from JSON string to numpy array.

        Board is stored as JSON array: [["X","O","E",...], [...], ...]
        Converts to numpy array with numeric encoding:
        - "E" (Empty) -> 0
        - "X" -> 1
        - "O" -> -1

        Args:
            board_json: JSON string representing board state

        Returns:
            Numpy array of shape (rows, cols)
        """
        if isinstance(board_json, str):
            board = json.loads(board_json)
        else:
            board = board_json

        # Convert to numeric encoding
        # Handle both "E" and "." for empty cells (both are used in different parts of the codebase)
        encoding = {"E": 0, ".": 0, "X": 1, "O": -1}
        return np.array([[encoding[cell] for cell in row] for row in board])

    def split_data(self, by_game: bool = True) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
        """
        Split data into train/validation/test sets.

        Args:
            by_game: If True, split by games (all moves from same game in same set).
                     If False, split by individual moves (can mix moves from same game).

        Returns:
            Tuple of (train_df, val_df, test_df)
        """
        if self.df is None:
            raise ValueError("Must call load_data() first")

        np.random.seed(self.random_seed)

        if by_game:
            # Split by games (recommended to avoid data leakage)
            unique_games = self.df['game_id'].unique()
            np.random.shuffle(unique_games)

            n_games = len(unique_games)
            train_end = int(n_games * self.train_split)
            val_end = train_end + int(n_games * self.val_split)

            train_games = unique_games[:train_end]
            val_games = unique_games[train_end:val_end]
            test_games = unique_games[val_end:]

            self.train_df = self.df[self.df['game_id'].isin(train_games)].copy()
            self.val_df = self.df[self.df['game_id'].isin(val_games)].copy()
            self.test_df = self.df[self.df['game_id'].isin(test_games)].copy()
        else:
            # Split by moves (not recommended - potential data leakage)
            indices = np.random.permutation(len(self.df))

            n_train = int(len(indices) * self.train_split)
            n_val = int(len(indices) * self.val_split)

            train_idx = indices[:n_train]
            val_idx = indices[n_train:n_train + n_val]
            test_idx = indices[n_train + n_val:]

            self.train_df = self.df.iloc[train_idx].copy()
            self.val_df = self.df.iloc[val_idx].copy()
            self.test_df = self.df.iloc[test_idx].copy()

        print(f"\nData split:")
        print(f"  Train: {len(self.train_df)} moves from {self.train_df['game_id'].nunique()} games")
        print(f"  Val:   {len(self.val_df)} moves from {self.val_df['game_id'].nunique()} games")
        print(f"  Test:  {len(self.test_df)} moves from {self.test_df['game_id'].nunique()} games")

        return self.train_df, self.val_df, self.test_df

    def get_stats(self) -> DatasetStats:
        """
        Get statistics about the loaded dataset.

        Returns:
            DatasetStats object with dataset information
        """
        if self.df is None:
            raise ValueError("Must call load_data() first")

        return DatasetStats(
            total_moves=len(self.df),
            total_games=self.df['game_id'].nunique(),
            train_moves=len(self.train_df) if self.train_df is not None else 0,
            val_moves=len(self.val_df) if self.val_df is not None else 0,
            test_moves=len(self.test_df) if self.test_df is not None else 0,
            game_modes=self.df['game_mode'].value_counts().to_dict(),
            winner_distribution=self.df['winner'].value_counts().to_dict()
        )

    def prepare_policy_data(
        self,
        df: pd.DataFrame
    ) -> Tuple[np.ndarray, np.ndarray]:
        """
        Prepare data for policy model training (move prediction).

        Args:
            df: DataFrame with move data

        Returns:
            Tuple of (X, y) where:
            - X: Board states (N, rows, cols) - state BEFORE move
            - y: Column played (N,) - the action taken
        """
        # Parse all board states
        X = np.array([
            self.parse_board_state(board)
            for board in df['board_state_before']
        ])

        # Get columns played (actions)
        y = df['column_played'].values

        return X, y

    def prepare_value_data(
        self,
        df: pd.DataFrame
    ) -> Tuple[np.ndarray, np.ndarray]:
        """
        Prepare data for value model training (win probability).

        Args:
            df: DataFrame with move data

        Returns:
            Tuple of (X, y) where:
            - X: Board states (N, rows, cols) - state AFTER move
            - y: Game outcome from perspective of player who just moved
                 1.0 if that player won
                 0.0 if tie
                -1.0 if that player lost
        """
        # Parse all board states AFTER move
        X = np.array([
            self.parse_board_state(board)
            for board in df['board_state_after']
        ])

        # Determine outcome from perspective of player who made the move
        # board_state_after has the piece placed, so need to figure out who moved
        # by checking board_state_before and board_state_after
        y = []
        for _, row in df.iterrows():
            winner = row['winner']

            # Determine who made this move by checking which player is in board_state_after
            # but not in board_state_before at the played position
            board_after = self.parse_board_state(row['board_state_after'])
            played_col = row['column_played']
            played_row = row['row_placed']

            player_who_moved = board_after[played_row, played_col]  # 1 for X, -1 for O

            # Assign outcome from that player's perspective
            if winner == 'Tie':
                outcome = 0.0
            elif winner == 'X' and player_who_moved == 1:
                outcome = 1.0  # X moved and X won
            elif winner == 'O' and player_who_moved == -1:
                outcome = 1.0  # O moved and O won
            else:
                outcome = -1.0  # Player moved but lost

            y.append(outcome)

        return X, np.array(y)

    def prepare_value_data_temporal(
        self,
        df: pd.DataFrame,
        last_n_moves: int = 8
    ) -> Tuple[np.ndarray, np.ndarray]:
        """
        Prepare value data using only last N moves of each game.

        Reduces label noise by focusing on endgame positions where
        the outcome is actually determined.

        Args:
            df: DataFrame with move data
            last_n_moves: Number of final moves to keep per game

        Returns:
            Tuple of (X, y) - same format as prepare_value_data()
        """
        # Keep only last N moves of each game
        df_filtered = df.groupby('game_id').tail(last_n_moves).copy()

        print(f"  Temporal filtering: kept {len(df_filtered)} moves from {len(df)} "
              f"(last {last_n_moves} moves per game)")

        # Use existing logic on filtered data
        return self.prepare_value_data(df_filtered)


def test_data_loader():
    """Test the data loader with sample data."""
    loader = ConnectFourDataLoader(data_dir="./data")

    try:
        # Load data
        df = loader.load_data(game_mode="ai_vs_ai")

        # Split data
        train_df, val_df, test_df = loader.split_data(by_game=True)

        # Get stats
        stats = loader.get_stats()
        print(f"\nDataset Statistics:")
        print(f"  Total moves: {stats.total_moves}")
        print(f"  Total games: {stats.total_games}")
        print(f"  Game modes: {stats.game_modes}")
        print(f"  Winner distribution: {stats.winner_distribution}")

        # Prepare policy data
        X_policy, y_policy = loader.prepare_policy_data(train_df)
        print(f"\nPolicy data shapes:")
        print(f"  X: {X_policy.shape}")
        print(f"  y: {y_policy.shape}")

        # Prepare value data
        X_value, y_value = loader.prepare_value_data(train_df)
        print(f"\nValue data shapes:")
        print(f"  X: {X_value.shape}")
        print(f"  y: {y_value.shape}")

    except FileNotFoundError as e:
        print(f"Error: {e}")
        print("Make sure to generate training data first using self-play")


if __name__ == "__main__":
    test_data_loader()
