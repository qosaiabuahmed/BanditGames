# ML Training - Connect Four

## Quick Start for Collaborators

**Checking out this project:**

1. **Clone the repository and checkout the branch**
2. **Start Docker containers:**
   ```bash
   docker-compose up -d
   ```
3. **Pull datasets with DVC (RECOMMENDED):**
   ```bash
   dvc pull  # Pulls all versioned datasets
   ```

   **Alternative if DVC remote not configured:** Datasets and models are gitignored. You'll need to either:
   - Ask the project owner for DVC remote access, OR
   - Regenerate datasets: `dvc repro` (generates datasets from scratch), OR
   - Retrain models from scratch (see Training section below)

4. **Verify setup:**
   ```bash
   docker-compose exec app ls -lh /app/data/*.parquet  # Check datasets
   docker-compose exec app ls -lh /app/data/models/    # Check models
   ```

**What's Gitignored:**
- `data/*.parquet` - Datasets (tracked by DVC instead)
- `data/models/` - Trained models (large files, can be regenerated)
- `mlruns/` - MLFlow tracking data
- See `.gitignore` for complete list

## Datasets

Three datasets from AI self-play games are used for training:

| Dataset | Games | Moves | Source |
|---------|-------|-------|--------|
| v1      | 1,401 | 44k   | ai_vs_ai |
| v2      | 5,002 | 156k  | ai_vs_ai |
| v3      | 10,401 | 508k | ai_vs_ai |

Location: `data/connect_four_v*.parquet`

**DVC Integration:**
- Dataset generation is managed via DVC pipeline (`dvc.yaml`)
- Datasets are tracked with DVC: `data/connect_four_v*.parquet.dvc`
- This ensures dataset versioning and reproducibility
- Pipeline includes self-play and parquet export stages
- Remote storage configured in `.dvc/config`

## Models

**4 model types trained on each dataset (12 total models):**

1.  **Policy CNN** - Move prediction using convolutional neural network
2.  **Policy LR** - Move prediction using logistic regression
3.  **Value CNN** - Win probability using convolutional neural network
4.  **Value LR** - Win probability using logistic regression

**Architecture (Standard for v1/v2, also used for v3 in evaluation):**

*   **Policy CNN / Value CNN:**
    *   Input: 6x7 board
    *   Conv2D(64 filters, 3x3) + ReLU + Padding
    *   Conv2D(64 filters, 3x3) + ReLU + Padding
    *   Conv2D(32 filters, 3x3) + ReLU + Padding
    *   Flatten
    *   Dense(128) + Dropout(0.2)
    *   Dense(64) + Dropout(0.2)
    *   Output: Dense(7) for policy, Dense(1) for value
    *   Parameters: ~237k
*   **Policy LR:** Logistic regression baseline (~300 parameters)
*   **Value LR:** Linear regression baseline (~50 parameters)

## Training

**Scripts:** `src/ml/training/`
*   `train_policy_cnn.py`
*   `train_policy_lr.py`
*   `train_value_cnn.py`
*   `train_value_lr.py`

**Training Configuration:**
*   **Optimizer:** Adam
*   **Learning Rate:** 0.001
*   **Batch Size:** 32
*   **Epochs:** 50 (with early stopping)
*   **Train/Val/Test Split:** 70% / 15% / 15%
*   **Early Stopping:** Patience = 10 epochs

**Example Training Commands:**
```bash
# Policy CNN
docker-compose exec app python src/ml/training/train_policy_cnn.py \
  --experiment "v2_dataset" --data-dir /app/data --epochs 50

# Policy LR
docker-compose exec app python src/ml/training/train_policy_lr.py \
  --experiment "v2_dataset" --data-dir /app/data

# Value CNN
docker-compose exec app python src/ml/training/train_value_cnn.py \
  --experiment "v2_dataset" --data-dir /app/data --epochs 50

# Value LR
docker-compose exec app python src/ml/training/train_value_lr.py \
  --experiment "v2_dataset" --data-dir /app/data
```

**Training all 12 models (v1, v2, v3 datasets x 4 model types):**
Repeat above commands for each dataset by changing `--experiment` flag to `v1_dataset`, `v2_dataset`, or `v3_dataset`.

## Model Versioning & Deployment

**Currently Deployed Models:**
The models in `data/models/` are the production models currently served by the API:
- `policy_cnn_model.keras` (11MB) - Best policy model (from v2 dataset)
- `policy_lr_model.pkl` (3.2KB) - Baseline policy model
- `value_cnn_model.keras` (2.8MB) - Best value model (from v2 dataset)
- `value_lr_model.pkl` (867B) - Baseline value model

**Model Storage & Versioning:**
- Trained models are gitignored (see `.gitignore` lines 13-16)
- Models ARE tracked with DVC via `data/models.dvc`
- All training runs are logged to MLFlow with model artifacts
- Production models are stored in `data/models/` and synced via DVC

**For Collaborators:**
Models are version-controlled with DVC. After cloning the repo, simply run:
```bash
dvc pull
```
This will download all datasets AND production models to `data/models/`.

**Alternative:** If DVC remote is not accessible, models can be regenerated from MLFlow artifacts at `http://localhost:5002`.

## Evaluation & Results

**Evaluation Script:** `src/ml/evaluation/evaluate_models.py`

**Generate Evaluation Report (Comprehensive overview of all 12 models):**
```bash
docker-compose exec app python /app/reports/generate_report.py
```
Output: `reports/evaluation_report.md` with 4 visualization charts (Policy Accuracy, Value MAE, CNN Improvement, Dataset Size Impact).

**Best Models (from evaluation):**
*   **Policy Model:** CNN on v2 dataset - **64.11% accuracy**
*   **Value Model:** CNN on v2 dataset - **MAE 0.2429**

**All Results (Summary):**

| Dataset | Policy CNN Acc. | Policy LR Acc. | Value CNN MAE | Value LR MAE |
|---------|-----------------|----------------|---------------|--------------|
| v1      | 56.28%          | 26.03%         | 0.7266        | 0.9304       |
| v2      | 64.11%          | 26.30%         | 0.2429        | 0.9363       |
| v3      | 60.68%          | 23.22%         | 0.3919        | 0.9402       |

**Key Findings from Evaluation:**
*   CNNs significantly outperform Logistic Regression (2-3x better accuracy for policy, much lower MAE for value).
*   v2 dataset (5k games) achieved the best overall performance for both CNN models.
*   v3 dataset (10.4k games) surprisingly showed slightly worse performance than v2 for CNNs, indicating potential data quality issues or a need for further hyperparameter tuning/larger models for this specific dataset.

## MLFlow Tracking

All training runs are logged to MLFlow: `http://localhost:5002`

**Metrics tracked:** Training/validation loss and accuracy, test accuracy (policy models), test MAE (value models), training time, epochs, parameters.

**Experiments:** Organized by dataset (`v1_dataset`, `v2_dataset`, `v3_dataset`).

## API Deployment

Trained models are deployed as REST API endpoints using FastAPI.

**API Endpoints:**
*   `/api/models/info`: Get information about loaded ML models.
*   `/api/predict/move`: Predict the best move using a policy model (`cnn` or `lr`).
*   `/api/predict/win-probability`: Predict win probability using a value model (`cnn` or `lr`).

## Prediction Logging & Monitoring (Part 3)

All API predictions are logged to the database (`ml_predictions` table) for monitoring.

**Admin Endpoints for Monitoring:**
*   `/api/admin/predictions/stats`: Get summary statistics of ML predictions.
*   `/api/admin/predictions/export`: Export prediction history for analysis (JSON/CSV, with filters).

**Comprehensive Player Comparison Report (Weights & Biases):**

A dedicated script (`reports/generate_comparison_report.py`) generates a comprehensive report on player performance, logged to Weights & Biases (W&B) for live monitoring and visualization. This provides graphical comparison of AI players (MCTS), ML models, and human players.

**Live W&B Dashboard:**
https://wandb.ai/sepp-verbuyst-karel-de-grote-hogeschool/connect-four-analysis/runs/mu2n65mc

**Generate/update the W&B report:**
```bash
# Get your W&B API key from: https://wandb.ai/authorize
docker-compose exec -T -e WANDB_API_KEY=<your_key> app python reports/generate_comparison_report.py
```

**Analyses included in W&B dashboard:**
1. **Overall Win Rate Comparison:** Win/loss/draw rates for each player type ('human', 'ai_mcts', 'ml_model')
2. **Move Speed Comparison:** Average time per move for each player type
3. **Opening Move Preferences:** Distribution of first moves by player type (visual comparison of strategies)
4. **Unforced Error Rate:** Frequency of missed winning moves (showcases playing skill differences)

All visualizations are logged as interactive charts with underlying data tables for deeper analysis.

## Project Structure

```
game-backend/
├── data/
│   ├── connect_four_v1.parquet           # Dataset v1 (1.4k games) - DVC tracked
│   ├── connect_four_v2.parquet           # Dataset v2 (5k games) - DVC tracked
│   ├── connect_four_v3.parquet           # Dataset v3 (10.4k games) - DVC tracked
│   ├── connect_four_v*.parquet.dvc       # DVC tracking files (committed to git)
│   └── models/                           # Deployed production models (gitignored)
│       ├── policy_cnn_model.keras        # 11MB - Best policy model (v2)
│       ├── policy_lr_model.pkl           # 3.2KB - Baseline policy
│       ├── value_cnn_model.keras         # 2.8MB - Best value model (v2)
│       └── value_lr_model.pkl            # 867B - Baseline value
├── src/
│   ├── ml/
│   │   ├── training/                     # Training scripts
│   │   │   ├── train_policy_cnn.py
│   │   │   ├── train_policy_lr.py
│   │   │   ├── train_value_cnn.py
│   │   │   └── train_value_lr.py
│   │   ├── evaluation/
│   │   │   └── evaluate_models.py        # Model evaluation script
│   │   └── serving/                      # Model serving wrappers
│   ├── api.py                            # FastAPI endpoints for ML predictions
│   ├── connect_four.py                   # Game logic
│   ├── gameplay_logger.py                # Database logging for games/moves
│   └── self_play.py                      # Self-play engine for dataset generation
├── reports/
│   ├── evaluation/
│   │   ├── evaluation_report.md          # Comprehensive model evaluation
│   │   └── *.png                         # Evaluation charts (4 visualizations)
│   ├── generate_report.py                # Script for evaluation report
│   └── generate_comparison_report.py     # Script for W&B comparative analysis
├── database/
│   └── schema.sql                        # Database schema (includes ml_predictions table)
├── mlruns/                               # MLFlow tracking data (gitignored)
├── dvc.yaml                              # DVC pipeline configuration
├── .dvc/
│   └── config                            # DVC remote configuration
├── .gitignore                            # Git ignore rules (models, datasets, etc.)
└── documentation/
    └── ml_training.md                    # This file
```

**Key Notes:**
- **Gitignored:** `data/*.parquet`, `data/models/`, `mlruns/`
- **DVC tracked:** Datasets via `*.parquet.dvc` files
- **Git tracked:** Code, configuration, DVC files, documentation
- **MLFlow artifacts:** Model checkpoints, training curves, metrics

## Docker

Models run in Docker containers with FastAPI:
*   API server: `http://localhost:8000`
*   MLFlow UI: `http://localhost:5002`

Enable/disable prediction logging: Set `ENABLE_PREDICTION_LOGGING=true` in `.env` or `docker-compose.yml`.