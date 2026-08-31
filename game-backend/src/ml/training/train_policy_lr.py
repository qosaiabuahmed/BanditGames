"""
Training Script for Logistic Regression Policy Model (Baseline)

Simple baseline model for move prediction using logistic regression.
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
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report

from src.ml.utils.data_loader import ConnectFourDataLoader
from src.ml.utils.features import FeatureExtractor


def train_logistic_regression(
    data_dir: str = "/app/data",
    dataset_file: str = "connect_four_v3.parquet",
    max_iter: int = 1000,
    C: float = 1.0,
    experiment_name: str = "connect_four_policy",
    run_name: str = None
):
    """
    Train a logistic regression baseline model with MLFlow tracking.

    Args:
        data_dir: Directory containing parquet files
        max_iter: Maximum iterations for solver
        C: Inverse of regularization strength (smaller = stronger regularization)
        experiment_name: MLFlow experiment name
        run_name: MLFlow run name (optional)
    """
    # Set up MLFlow
    mlflow.set_experiment(experiment_name)

    if run_name is None:
        run_name = f"policy_lr_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

    print("="*60)
    print("Training Logistic Regression Baseline (Move Prediction)")
    print("="*60)

    with mlflow.start_run(run_name=run_name):
        # Log hyperparameters
        mlflow.log_param("model_type", "logistic_regression")
        mlflow.log_param("max_iter", max_iter)
        mlflow.log_param("C", C)
        mlflow.log_param("solver", "lbfgs")
        mlflow.log_param("parameters", "~301 (42 features × 7 classes)")

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
        X_train, y_train = loader.prepare_policy_data(train_df)
        X_test, y_test = loader.prepare_policy_data(test_df)

        print(f"  Train: X={X_train.shape}, y={y_train.shape}")
        print(f"  Test:  X={X_test.shape}, y={y_test.shape}")

        # Flatten board to 42 features (6 rows × 7 cols)
        extractor = FeatureExtractor()
        X_train_flat = extractor.boards_to_features(X_train)
        X_test_flat = extractor.boards_to_features(X_test)

        print(f"  Flattened: {X_train_flat.shape[1]} features per sample")

        # Build logistic regression model
        print("\n4. Building logistic regression model...")
        model = LogisticRegression(
            max_iter=max_iter,
            C=C,
            solver='lbfgs',
            multi_class='multinomial',
            random_state=42,
            verbose=1
        )

        # Train model
        print("\n5. Training model...")
        print("  (This should take 5-10 seconds)")

        import time
        start_time = time.time()
        model.fit(X_train_flat, y_train)
        training_time = time.time() - start_time

        print(f"  Training completed in {training_time:.2f} seconds")
        mlflow.log_metric("training_time_seconds", training_time)

        # Evaluate on training set (for comparison)
        print("\n6. Evaluating on training set...")
        train_pred = model.predict(X_train_flat)
        train_accuracy = accuracy_score(y_train, train_pred)
        print(f"  Train Accuracy: {train_accuracy:.4f}")
        mlflow.log_metric("train_accuracy", train_accuracy)

        # Evaluate on test set
        print("\n7. Evaluating on test set...")
        test_pred = model.predict(X_test_flat)
        test_accuracy = accuracy_score(y_test, test_pred)
        print(f"  Test Accuracy: {test_accuracy:.4f}")
        mlflow.log_metric("test_accuracy", test_accuracy)

        # Detailed classification report
        print("\n  Per-column accuracy:")
        report = classification_report(y_test, test_pred, target_names=[f"Col {i}" for i in range(7)], output_dict=True)
        for col in range(7):
            col_f1 = report[f"Col {col}"]["f1-score"]
            print(f"    Column {col}: F1={col_f1:.3f}")

        # Save model
        print("\n8. Saving model...")
        model_dir = Path("/app/data/models")
        model_dir.mkdir(parents=True, exist_ok=True)

        model_path = model_dir / "policy_lr_model.pkl"
        joblib.dump(model, str(model_path))

        # Log model to MLFlow
        mlflow.sklearn.log_model(model, "model")

        print("\n" + "="*60)
        print("Training Complete!")
        print(f"  Final Test Accuracy: {test_accuracy:.4f}")
        print(f"  Training Time: {training_time:.2f}s")
        print(f"  Model saved to: {model_path}")
        print("\n  Comparison to neural networks:")
        print("    Dense NN:  ~50% accuracy, ~10 min training")
        print("    CNN:       ~60% accuracy, ~15 min training")
        print(f"    LogReg:    ~{test_accuracy*100:.0f}% accuracy, {training_time:.0f}s training")
        print("="*60)

        return model


def main():
    """Main entry point for training script."""
    parser = argparse.ArgumentParser(description="Train Connect Four logistic regression baseline")
    parser.add_argument("--data-dir", type=str, default="/app/data",
                        help="Directory containing training data")
    parser.add_argument("--dataset-file", type=str, default="connect_four_v3.parquet",
                        help="Specific parquet file to load (e.g., connect_four_v1.parquet)")
    parser.add_argument("--max-iter", type=int, default=1000,
                        help="Maximum iterations for solver")
    parser.add_argument("--C", type=float, default=1.0,
                        help="Inverse regularization strength")
    parser.add_argument("--experiment", type=str, default="connect_four_policy",
                        help="MLFlow experiment name")

    args = parser.parse_args()

    try:
        train_logistic_regression(
            data_dir=args.data_dir,
            dataset_file=args.dataset_file,
            max_iter=args.max_iter,
            C=args.C,
            experiment_name=args.experiment
        )
    except Exception as e:
        print(f"\nError during training: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
