"""
Training Script for CNN Policy Model

Trains the CNN policy network for move prediction.
Uses MLFlow for experiment tracking.
"""

import os
import sys
import argparse
from datetime import datetime
from pathlib import Path

import numpy as np
import mlflow
import mlflow.tensorflow

from src.ml.utils.data_loader import ConnectFourDataLoader
from src.ml.utils.features import FeatureExtractor
from src.ml.models.policy_model import PolicyModel


def train_policy_cnn(
    data_dir: str = "/app/data",
    dataset_file: str = "connect_four_v3.parquet",
    epochs: int = 50,
    batch_size: int = 32,
    dropout_rate: float = 0.2,
    experiment_name: str = "connect_four_policy",
    run_name: str = None
):
    """
    Train CNN policy model with MLFlow tracking.

    Args:
        data_dir: Directory containing parquet files
        epochs: Number of training epochs
        batch_size: Batch size
        dropout_rate: Dropout rate
        experiment_name: MLFlow experiment name
        run_name: MLFlow run name (optional)
    """
    # Set up MLFlow
    mlflow.set_experiment(experiment_name)

    if run_name is None:
        run_name = f"policy_cnn_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

    print("="*60)
    print("Training CNN Policy Model (Move Prediction)")
    print("="*60)

    with mlflow.start_run(run_name=run_name):
        # Log hyperparameters
        mlflow.log_param("model_type", "cnn")
        mlflow.log_param("epochs", epochs)
        mlflow.log_param("batch_size", batch_size)
        mlflow.log_param("dropout_rate", dropout_rate)

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
        X_val, y_val = loader.prepare_policy_data(val_df)
        X_test, y_test = loader.prepare_policy_data(test_df)

        print(f"  Train: X={X_train.shape}, y={y_train.shape}")
        print(f"  Val:   X={X_val.shape}, y={y_val.shape}")
        print(f"  Test:  X={X_test.shape}, y={y_test.shape}")

        # Prepare for CNN
        extractor = FeatureExtractor()
        X_train_cnn = extractor.prepare_for_cnn(X_train)
        X_val_cnn = extractor.prepare_for_cnn(X_val)
        X_test_cnn = extractor.prepare_for_cnn(X_test)

        # Build model
        print("\n4. Building CNN model...")

        # === MODIFIED: Use larger architecture for v3 dataset ===
        # v1/v2 use default: Conv[64,64,32] + Dense[128,64] (~237k params)
        # v3 uses larger: Conv[128,128,64] + Dense[256,128] (~900k params)
        if 'v3' in dataset_file:
            print("  Detected v3 dataset - using LARGER architecture for better capacity")
            policy = PolicyModel(
                rows=6,
                cols=7,
                dropout_rate=dropout_rate,
                cnn_filters=[128, 128, 64],      # Larger filters for v3
                cnn_dense_units=[256, 128]       # Larger dense layers for v3
            )
        else:
            print("  Using standard architecture for v1/v2")
            policy = PolicyModel(
                rows=6,
                cols=7,
                dropout_rate=dropout_rate
                # cnn_filters=[64, 64, 32] (default)
                # cnn_dense_units=[128, 64] (default)
            )
        # =========================================================

        model = policy.build_cnn_model()
        model.summary()

        # Train model
        print("\n5. Training model...")
        history = policy.train(
            X_train_cnn, y_train,
            X_val_cnn, y_val,
            epochs=epochs,
            batch_size=batch_size
        )

        # Log training metrics
        for epoch in range(len(history.history['loss'])):
            mlflow.log_metric("train_loss", history.history['loss'][epoch], step=epoch)
            mlflow.log_metric("train_accuracy", history.history['accuracy'][epoch], step=epoch)
            mlflow.log_metric("val_loss", history.history['val_loss'][epoch], step=epoch)
            mlflow.log_metric("val_accuracy", history.history['val_accuracy'][epoch], step=epoch)

        # Evaluate on test set
        print("\n6. Evaluating on test set...")
        test_loss, test_accuracy = model.evaluate(X_test_cnn, y_test, verbose=0)
        print(f"  Test Loss: {test_loss:.4f}")
        print(f"  Test Accuracy: {test_accuracy:.4f}")

        mlflow.log_metric("test_loss", test_loss)
        mlflow.log_metric("test_accuracy", test_accuracy)

        # Save model
        print("\n7. Saving model...")
        model_dir = Path("/app/data/models")
        model_dir.mkdir(parents=True, exist_ok=True)

        model_path = model_dir / "policy_cnn_model.keras"
        policy.save(str(model_path))

        # Log model to MLFlow
        mlflow.tensorflow.log_model(model, "model")

        print("\n" + "="*60)
        print("Training Complete!")
        print(f"  Final Test Accuracy: {test_accuracy:.4f}")
        print(f"  Model saved to: {model_path}")
        print("="*60)

        return policy, history


def main():
    """Main entry point for training script."""
    parser = argparse.ArgumentParser(description="Train Connect Four CNN policy model")
    parser.add_argument("--data-dir", type=str, default="/app/data",
                        help="Directory containing training data")
    parser.add_argument("--dataset-file", type=str, default="connect_four_v3.parquet",
                        help="Specific parquet file to load (e.g., connect_four_v1.parquet)")
    parser.add_argument("--epochs", type=int, default=50,
                        help="Number of training epochs")
    parser.add_argument("--batch-size", type=int, default=32,
                        help="Batch size")
    parser.add_argument("--dropout", type=float, default=0.2,
                        help="Dropout rate")
    parser.add_argument("--experiment", type=str, default="connect_four_policy",
                        help="MLFlow experiment name")

    args = parser.parse_args()

    try:
        train_policy_cnn(
            data_dir=args.data_dir,
            dataset_file=args.dataset_file,
            epochs=args.epochs,
            batch_size=args.batch_size,
            dropout_rate=args.dropout,
            experiment_name=args.experiment
        )
    except Exception as e:
        print(f"\nError during training: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
