# Self-Play Data Collection

Generate AI vs AI games to collect training data for machine learning.

## Quick Start

```bash
# 1. Start Docker containers
docker-compose up -d

# 2. Run self-play (generates 5 games)
docker-compose exec app python /app/src/self_play.py -n 5

# 3. Export to parquet
curl -X POST http://localhost:8000/api/export/training-data \
  -H "Content-Type: application/json" \
  -d '{}'
```

## Commands

### Command Line
```bash
# Basic usage
docker-compose exec app python /app/src/self_play.py -n <num_games>

# With difficulty settings
docker-compose exec app python /app/src/self_play.py -n 10 --difficulty-x 3 --difficulty-o 1

# Quiet mode (less output)
docker-compose exec app python /app/src/self_play.py -n 100 -q
```

**Options:**
| Flag | Description | Default |
|------|-------------|---------|
| `-n` | Number of games | 10 |
| `--difficulty-x` | AI X difficulty (1-3) | 2 |
| `--difficulty-o` | AI O difficulty (1-3) | 2 |
| `-q` | Quiet mode | false |
| `--timeout` | Seconds per AI move | 60 |

### API Endpoints

**Generate games:**
```bash
curl -X POST http://localhost:8000/api/self-play \
  -H "Content-Type: application/json" \
  -d '{"num_games": 10, "difficulty_x": 2, "difficulty_o": 2}'
```

**Export to parquet:**
```bash
curl -X POST http://localhost:8000/api/export/training-data \
  -H "Content-Type: application/json" \
  -d '{"game_mode": "ai_vs_ai", "limit": 100}'
```

## Check Database

```bash
# Count all moves
docker exec connect_four_db psql -U postgres -d postgres -c "SELECT COUNT(*) FROM moves;"

# View recent moves
docker exec connect_four_db psql -U postgres -d postgres -c "SELECT * FROM moves ORDER BY move_id DESC LIMIT 10;"

# View AI games
docker exec connect_four_db psql -U postgres -d postgres -c "SELECT * FROM games WHERE game_mode = 'ai_vs_ai';"
```

## Data Logged

Each move stores:
- `board_state_before` / `board_state_after` - Full board as JSON
- `column_played` / `row_placed` - Move position
- `score_x_before/after` / `score_o_before/after` - Scores
- `legal_moves` - Valid columns at that state
- `move_timestamp` - When move was made

Each game stores:
- `winner` - X, O, or Tie
- `total_moves` - Number of moves played
- `game_mode` - "ai_vs_ai" for self-play
- `final_board_state` - End state as JSON

## Output

Parquet files are saved inside the Docker container at `/app/data/`.

**Copy to local machine:**
```bash
# Copy all parquet files
docker cp connect_four_app:/app/data/. ./data/

# Or copy a specific file
docker cp connect_four_app:/app/data/training_data_YYYYMMDD_HHMMSS.parquet ./data/
```

**Check files in container:**
```bash
docker-compose exec app ls -la /app/data/
```

## DVC Versioning (Optional)

```bash
# Install DVC
pip install dvc

# Initialize
dvc init

# Track parquet files
dvc add data/training_data_*.parquet

# Commit
git add data/*.dvc
git commit -m "Add training data"
```
