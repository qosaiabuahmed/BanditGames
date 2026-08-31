# MCTS AI Integration with Connect Four UI

This guide explains how the Connect Four UI integrates with the MCTS AI service to display move suggestions during gameplay.

## Overview

The UI (`src/ui.py`) automatically connects to your MCTS AI service (running in Docker on port 5001) and requests move suggestions for the current player before each turn.

## Architecture

```
┌─────────────────┐          ┌─────────────────┐
│   UI (ui.py)    │          │  MCTS Service   │
│   Port: N/A     │          │   Port: 5001    │
│   (Terminal)    │          │   (Docker)      │
└────────┬────────┘          └────────┬────────┘
         │                            │
         │  1. POST /get-move         │
         │  (current game state)      │
         │───────────────────────────>│
         │                            │
         │                     MCTS   │
         │                  Algorithm │
         │                   Running  │
         │                            │
         │  2. Response               │
         │  (best move + stats)       │
         │<───────────────────────────│
         │                            │
         │  3. Display to player      │
         │                            │
```

## How It Works

1. **Startup**: When you run `python src/ui.py`, it checks if the MCTS service is running on port 5001
2. **Each Turn**: Before asking for player input, the UI:
   - Gets the current board state
   - Sends it to the MCTS service at `http://localhost:5001/get-move`
   - Receives the best move with confidence, nodes explored, and computation time
   - Displays the suggestion to the player
3. **Player Choice**: The player can follow the AI suggestion or make their own move

## Setup

### 1. Start the MCTS AI Service (Docker)

```bash
cd C:\School\game-backend
docker-compose up -d ai
```

This starts the MCTS service in the background on port 5001.

### 2. Verify MCTS Service is Running

```bash
curl http://localhost:5001/health
```

You should see:
```json
{
  "service": "connect-four-ai",
  "status": "healthy"
}
```

### 3. Run the UI

```bash
cd C:\School\game-backend
python src/ui.py
```

When the UI starts, you'll see:
```
✓ MCTS AI service connected at http://localhost:5001
```

If the MCTS service is not running, you'll see:
```
⚠ MCTS AI service not available (run: docker-compose up -d ai)
```

The game will still work, but won't show AI suggestions.

## Example Gameplay

```
============================================================
                       CONNECT FOUR
============================================================

Game Score Player 1 (X): 0
Game Score Player 2 (O): 0

  1   2   3   4   5   6   7
┌───┬───┬───┬───┬───┬───┬───┐
│ . │ . │ . │ . │ . │ . │ . │
├───┼───┼───┼───┼───┼───┼───┤
│ . │ . │ . │ . │ . │ . │ . │
├───┼───┼───┼───┼───┼───┼───┤
│ . │ . │ . │ . │ . │ . │ . │
├───┼───┼───┼───┼───┼───┼───┤
│ . │ . │ . │ . │ . │ . │ . │
├───┼───┼───┼───┼───┼───┼───┤
│ . │ . │ . │ . │ . │ . │ . │
├───┼───┼───┼───┼───┼───┼───┤
│ . │ . │ . │ . │ . │ . │ . │
└───┴───┴───┴───┴───┴───┴───┘

Player 1's turn (X)

────────────────────────────────────────────────────────────
  🤖 AI SUGGESTION:
  Column: 4 (1-indexed)
  Confidence: 68.50%
  Nodes explored: 2,000
  Computation time: 856ms
────────────────────────────────────────────────────────────

Commands: [column number] | save | menu

Enter command:
```

## MCTS Service API

### Endpoint: POST `/get-move`

**Request Format:**
```json
{
  "board": [
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."]
  ],
  "score_x": 0,
  "score_o": 0,
  "current_player": "X",
  "column_positions": [0, 0, 0, 0, 0, 0, 0],
  "rows": 6,
  "cols": 7,
  "connect_length": 4
}
```

**Response Format:**
```json
{
  "column": 3,
  "confidence": 0.685,
  "nodes_explored": 2000,
  "computation_time_ms": 856
}
```

## Configuration

### Change MCTS Service URL

Edit `src/ui.py` line 14:

```python
# MCTS AI Service URL (running in Docker)
MCTS_SERVICE_URL = "http://localhost:5001"
```

### Adjust MCTS Parameters

Edit `docker-compose.yml` to change MCTS algorithm parameters:

```yaml
ai:
  environment:
    MAX_ITERATIONS: 2000      # Number of MCTS simulations
    EXPLORATION_CONSTANT: 1.41 # UCB exploration constant
```

Then restart the service:
```bash
docker-compose restart ai
```

## Troubleshooting

### Problem: "MCTS AI service not available"

**Cause**: Docker container is not running

**Solution**:
```bash
docker-compose up -d ai
docker-compose ps  # Verify it's running
docker logs connect_four_ai  # Check for errors
```

### Problem: AI suggestions are slow

**Cause**: MAX_ITERATIONS is set too high

**Solution**: Lower MAX_ITERATIONS in `docker-compose.yml`:
```yaml
MAX_ITERATIONS: 1000  # Faster, less accurate
```

### Problem: Port 5001 already in use

**Cause**: Another service is using port 5001

**Solution**: Change the port mapping in `docker-compose.yml`:
```yaml
ai:
  ports:
    - "5002:5000"  # Map to port 5002 instead
```

Then update `src/ui.py`:
```python
MCTS_SERVICE_URL = "http://localhost:5002"
```

### Problem: Suggestions seem random

**Cause**: Board might be empty (all moves are equally good at start)

**Solution**: This is normal. MCTS confidence improves as the game progresses.

## Technical Details

### Request Timeout

The UI waits up to 10 seconds for the MCTS service to respond. If it takes longer, the turn continues without a suggestion.

See `src/ui.py` line 110:
```python
response = requests.post(
    f"{MCTS_SERVICE_URL}/get-move",
    json=request_data,
    timeout=10
)
```

### Error Handling

The UI gracefully handles MCTS service failures:
- Service unavailable: Game continues without suggestions
- Request timeout: Player turn proceeds normally
- Invalid response: Silently ignored

### Performance

- MCTS calculation happens during player's turn (player waits)
- Typical calculation time: 200-1000ms depending on MAX_ITERATIONS
- No caching between turns (each suggestion is fresh)

## Optional: Running Without Docker

If you want to run the MCTS service without Docker:

```bash
cd C:\School\ai-implementation
pip install -r requirements.txt
python src/api.py
```

The service will start on port 5000 by default. Update `ui.py`:
```python
MCTS_SERVICE_URL = "http://localhost:5000"
```