"""
Training Script for Linear Regression Value Model (Baseline)

Simple baseline model for win probability prediction using linear regression.
Much faster to train than neural networks, serves as performance comparison.
Uses MLFlow for experiment tracking.
"""

import os
import sys
import argparse
from datetime import datetime
from pathlib import Path

import numpy as np
import joblib
import mlflow
import mlflow.sklearn
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_absolute_error, mean_squared_error

from src.ml.utils.data_loader import ConnectFourDataLoader
from src.ml.utils.features import FeatureExtractor


def train_linear_regression(
    data_dir: str = "/app/data",
    dataset_file: str = "connect_four_v3.parquet",
    alpha: float = 1.0,
    last_n_moves: int = 8,
    experiment_name: str = "connect_four_value",
    run_name: str = None
):
    """
    Train a linear regression baseline model with MLFlow tracking.

    Args:
        data_dir: Directory containing parquet files
        alpha: Regularization strength (Ridge regression)
        experiment_name: MLFlow experiment name
        run_name: MLFlow run name (optional)
    """
    # Set up MLFlow
    mlflow.set_experiment(experiment_name)

    if run_name is None:
        run_name = f"value_lr_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

    print("="*60)
    print("Training Linear Regression Baseline (Win Probability)")
    print("="*60)

    with mlflow.start_run(run_name=run_name):
        # Log hyperparameters
        mlflow.log_param("model_type", "linear_regression")
        mlflow.log_param("alpha", alpha)
        mlflow.log_param("last_n_moves", last_n_moves)
        mlflow.log_param("parameters", "~43 (42 features + 1 intercept)")

        # Load data
        print(f"\n1. Loading training data from {dataset_file}...")
        loader = ConnectFourDataLoader(data_dir=data_dir)
        df = loader.load_data(file_pattern=dataset_file, game_mode="ai_vs_ai")

        # Split data by game (avoid data leakage)
        print("\n2. Splitting data...")
        train_df, val_df, test_df = loader.split_data(by_game=True)

        # Log dataset stats
        stats = loader.get_stats()
        mlflow.log_metric("total_moves", stats.total_moves)
        mlflow.log_metric("total_games", stats.total_games)
        mlflow.log_metric("train_moves", stats.train_moves)
        mlflow.log_metric("val_moves", stats.val_moves)
        mlflow.log_metric("test_moves", stats.test_moves)

        # Prepare features
        print("\n3. Preparing features...")
        X_train, y_train = loader.prepare_value_data_temporal(train_df, last_n_moves)
        X_test, y_test = loader.prepare_value_data_temporal(test_df, last_n_moves)

        print(f"  Train: X={X_train.shape}, y={y_train.shape}")
        print(f"  Test:  X={X_test.shape}, y={y_test.shape}")

        # Print outcome distribution
        print(f"\n  Outcome distribution (train):")
        print(f"    Wins (+1):  {np.sum(y_train == 1.0)} ({100*np.mean(y_train == 1.0):.1f}%)")
        print(f"    Ties (0):   {np.sum(y_train == 0.0)} ({100*np.mean(y_train == 0.0):.1f}%)")
        print(f"    Losses (-1): {np.sum(y_train == -1.0)} ({100*np.mean(y_train == -1.0):.1f}%)")

        # Flatten board to 42 features (6 rows × 7 cols)
        extractor = FeatureExtractor()
        X_train_flat = extractor.boards_to_features(X_train)
        X_test_flat = extractor.boards_to_features(X_test)

        print(f"  Flattened: {X_train_flat.shape[1]} features per sample")

        # Build Ridge regression model (linear regression with L2 regularization)
        print("\n4. Building linear regression model...")
        model = Ridge(alpha=alpha, random_state=42)

        # Train model
        print("\n5. Training model...")
        print("  (This should take 1-2 seconds)")

        import time
        start_time = time.time()
        model.fit(X_train_flat, y_train)
        training_time = time.time() - start_time

        print(f"  Training completed in {training_time:.2f} seconds")
        mlflow.log_metric("training_time_seconds", training_time)

        # Evaluate on training set (for comparison)
        print("\n6. Evaluating on training set...")
        train_pred = model.predict(X_train_flat)
        # Clip predictions to [-1, 1] range
        train_pred = np.clip(train_pred, -1, 1)

        train_mae = mean_absolute_error(y_train, train_pred)
        train_mse = mean_squared_error(y_train, train_pred)
        print(f"  Train MAE: {train_mae:.4f}")
        print(f"  Train MSE: {train_mse:.4f}")
        mlflow.log_metric("train_mae", train_mae)
        mlflow.log_metric("train_mse", train_mse)

        # Evaluate on test set
        print("\n7. Evaluating on test set...")
        test_pred = model.predict(X_test_flat)
        # Clip predictions to [-1, 1] range
        test_pred = np.clip(test_pred, -1, 1)

        test_mae = mean_absolute_error(y_test, test_pred)
        test_mse = mean_squared_error(y_test, test_pred)
        print(f"  Test MAE: {test_mae:.4f}")
        print(f"  Test MSE: {test_mse:.4f}")
        mlflow.log_metric("test_mae", test_mae)
        mlflow.log_metric("test_mse", test_mse)

        # Additional evaluation: sign accuracy
        correct_sign = np.sum(np.sign(test_pred) == np.sign(y_test))
        sign_accuracy = correct_sign / len(y_test)
        print(f"  Sign Accuracy (correct outcome direction): {sign_accuracy:.4f}")
        mlflow.log_metric("test_sign_accuracy", sign_accuracy)

        # Save model
        print("\n8. Saving model...")
        model_dir = Path("/app/data/models")
        model_dir.mkdir(parents=True, exist_ok=True)

        model_path = model_dir / "value_lr_model.pkl"
        joblib.dump(model, str(model_path))

        # Log model to MLFlow
        mlflow.sklearn.log_model(model, "model")

        print("\n" + "="*60)
        print("Training Complete!")
        print(f"  Final Test MAE: {test_mae:.4f}")
        print(f"  Final Sign Accuracy: {sign_accuracy:.4f}")
        print(f"  Training Time: {training_time:.2f}s")
        print("\n  Comparison to neural networks:")
        print("    Dense NN:  ~0.36 MAE, ~10 min training")
        print("    CNN:       ~0.30 MAE, ~15 min training")
        print(f"    LinReg:    ~{test_mae:.2f} MAE, {training_time:.0f}s training")
        print("="*60)

        return model


def main():
    """Main entry point for training script."""
    parser = argparse.ArgumentParser(description="Train Connect Four linear regression baseline")
    parser.add_argument("--data-dir", type=str, default="/app/data",
                        help="Directory containing training data")
    parser.add_argument("--dataset-file", type=str, default="connect_four_v3.parquet",
                        help="Specific parquet file to load (e.g., connect_four_v1.parquet)")
    parser.add_argument("--alpha", type=float, default=1.0,
                        help="Regularization strength (Ridge)")
    parser.add_argument("--last-n-moves", type=int, default=8,
                        help="Number of final moves to use per game")
    parser.add_argument("--experiment", type=str, default="connect_four_value",
                        help="MLFlow experiment name")

    args = parser.parse_args()

    try:
        train_linear_regression(
            data_dir=args.data_dir,
            dataset_file=args.dataset_file,
            alpha=args.alpha,
            last_n_moves=args.last_n_moves,
            experiment_name=args.experiment
        )
    except Exception as e:
        print(f"\nError during training: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
