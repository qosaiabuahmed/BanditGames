# MCTS AI Integration Guide

## Overview

This document explains how to integrate the Monte Carlo Tree Search (MCTS) AI service into your Connect Four game backend. The AI runs as a separate service that receives game state via HTTP and returns optimal moves.

## Architecture

```
┌─────────────────┐         HTTP POST          ┌─────────────────┐
│                 │    /get-move endpoint      │                 │
│  Game Backend   │ ─────────────────────────> │   AI Service    │
│  (Your Code)    │                            │   (Flask API)   │
│                 │ <───────────────────────── │                 │
└─────────────────┘      Best Move + Stats     └─────────────────┘
                                                        │
                                                        │ uses
                                                        ▼
                                                ┌──────────────┐
                                                │ MCTS Engine  │
                                                │ (mcts.py)    │
                                                └──────────────┘
                                                        │
                                                        │ uses
                                                        ▼
                                                ┌──────────────┐
                                                │ Game Rules   │
                                                │ (simulator)  │
                                                └──────────────┘
```

## System Components

### AI Service Files

1. **api.py** - Flask REST API (run this as the service)
2. **mcts.py** - Monte Carlo Tree Search algorithm
3. **game_simulator.py** - Lightweight game rules for simulations
4. **config.py** - AI configuration (iterations, exploration constant)

### How MCTS Works

The AI uses Monte Carlo Tree Search with 2000 iterations per move:
1. **Selection**: Traverse the game tree using UCB1 formula
2. **Expansion**: Add a new node to the tree
3. **Simulation**: Play random moves until game ends
4. **Backpropagation**: Update win/visit statistics up the tree

After all iterations, the move with the most visits is selected.

## API Endpoints

### 1. Health Check

**Endpoint**: `GET /health`

**Purpose**: Verify the AI service is running

**Response**:
```json
{
  "status": "healthy",
  "service": "connect-four-ai"
}
```

**When to use**: On game server startup, before each game starts

---

### 2. Get Best Move (Primary Endpoint)

**Endpoint**: `POST /get-move`

**Purpose**: Get the AI's best move for the current game state

**Request Format**:
```json
{
  "board": [
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", "X", ".", ".", "."],
    [".", ".", "O", "X", ".", ".", "."],
    [".", "X", "O", "O", ".", ".", "."]
  ],
  "score_x": 0,
  "score_o": 0,
  "current_player": "X",
  "column_positions": [5, 4, 3, 2, 5, 5, 5],
  "rows": 6,
  "cols": 7,
  "connect_length": 4
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `board` | 2D array | Yes | 6x7 grid, "X"/"O"/"." for pieces |
| `current_player` | string | Yes | "X" or "O" - whose turn it is |
| `column_positions` | array[int] | Yes | Next available row per column (0-5, or -1 if full) |
| `score_x` | int | No | Current score for player X (default: 0) |
| `score_o` | int | No | Current score for player O (default: 0) |
| `rows` | int | No | Board height (default: 6) |
| `cols` | int | No | Board width (default: 7) |
| `connect_length` | int | No | Win condition length (default: 4) |

**Response Format**:
```json
{
  "column": 3,
  "confidence": 0.72,
  "nodes_explored": 2000,
  "computation_time_ms": 856
}
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `column` | int | Best column to play (0-6, zero-indexed) |
| `confidence` | float | Win rate for this move (0.0-1.0) |
| `nodes_explored` | int | Number of MCTS iterations performed |
| `computation_time_ms` | int | Time taken in milliseconds |

---

### 3. Analyze Position (Advanced)

**Endpoint**: `POST /analyze`

**Purpose**: Get detailed statistics for ALL possible moves

**Request Format**: Same as `/get-move`

**Response Format**:
```json
{
  "best_move": 3,
  "move_analysis": {
    "0": {
      "visits": 150,
      "wins": 67.5,
      "win_rate": 0.45,
      "confidence": 0.075
    },
    "1": {
      "visits": 200,
      "wins": 104,
      "win_rate": 0.52,
      "confidence": 0.10
    },
    "3": {
      "visits": 800,
      "wins": 576,
      "win_rate": 0.72,
      "confidence": 0.40
    }
  },
  "total_simulations": 2000
}
```

**When to use**: For debugging, analysis, or showing move probabilities to users

---

## Integration Steps

### Step 1: Start the AI Service

```bash
# Navigate to AI directory
cd C:\School\ai-implementation\src

# Start the Flask API
python api.py
```

The service runs on `http://0.0.0.0:5000` (accessible via localhost:5000)

### Step 2: Check Service Health

Before your game starts, verify the AI is running:

```python
import requests

def check_ai_health():
    try:
        response = requests.get("http://localhost:5000/health", timeout=2)
        return response.status_code == 200
    except:
        return False
```

### Step 3: Convert Game State to JSON

Your game needs to provide these data structures:

```python
def game_state_to_json(game):
    """Convert your game state to AI-compatible JSON."""
    return {
        "board": game.get_board_as_2d_array(),  # 6x7 array
        "current_player": game.current_player,   # "X" or "O"
        "column_positions": game.get_column_positions(),  # Array of next available row per column
        "score_x": game.score_x,
        "score_o": game.score_o
    }
```

**Important Board Format**:
- Row 0 is TOP of the board
- Row 5 is BOTTOM of the board
- Use "X", "O", or "." (period for empty)
- Case-insensitive ("x"/"X" both work)

**Column Positions Array**:
- Index = column number (0-6)
- Value = next available row in that column
- Value -1 means column is full
- Example: `[5, 4, 3, 2, 5, 5, 5]` means:
  - Column 0: next piece goes in row 5 (1 piece placed)
  - Column 1: next piece goes in row 4 (2 pieces placed)
  - Column 3: next piece goes in row 2 (4 pieces placed)

### Step 4: Request AI Move

```python
def get_ai_move(game_state_json):
    """Request best move from AI service."""
    try:
        response = requests.post(
            "http://localhost:5000/get-move",
            json=game_state_json,
            timeout=10  # Allow up to 10 seconds for computation
        )

        if response.status_code == 200:
            result = response.json()
            return {
                'success': True,
                'column': result['column'],  # 0-indexed column
                'confidence': result['confidence'],
                'time_ms': result['computation_time_ms']
            }
        else:
            return {
                'success': False,
                'error': response.json().get('error', 'Unknown error')
            }

    except requests.exceptions.Timeout:
        return {'success': False, 'error': 'AI timeout'}
    except requests.exceptions.ConnectionError:
        return {'success': False, 'error': 'AI service not running'}
    except Exception as e:
        return {'success': False, 'error': str(e)}
```

### Step 5: Apply the Move

```python
def ai_turn(game):
    """Execute AI's turn in the game."""
    # Convert game state
    game_state = game_state_to_json(game)

    # Get AI move
    ai_response = get_ai_move(game_state)

    if ai_response['success']:
        column = ai_response['column']
        confidence = ai_response['confidence']

        # Apply move to your game
        game.place_piece(column)

        # Optional: Log AI metadata
        print(f"AI played column {column + 1} (confidence: {confidence:.1%})")

        return True
    else:
        print(f"AI Error: {ai_response['error']}")
        # Fallback: random valid move
        return False
```

## Complete Integration Example

```python
import requests
import random

class ConnectFourAIClient:
    """Client for integrating MCTS AI into your game."""

    def __init__(self, ai_url="http://localhost:5000"):
        self.ai_url = ai_url
        self.service_healthy = False

    def check_health(self):
        """Verify AI service is running."""
        try:
            response = requests.get(f"{self.ai_url}/health", timeout=2)
            self.service_healthy = response.status_code == 200
            return self.service_healthy
        except:
            self.service_healthy = False
            return False

    def get_move(self, board, current_player, column_positions,
                 score_x=0, score_o=0):
        """
        Get AI's best move.

        Args:
            board: 6x7 2D list with "X", "O", or "."
            current_player: "X" or "O"
            column_positions: List[int] of next available row per column
            score_x: Current score for X
            score_o: Current score for O

        Returns:
            dict with 'column', 'confidence', or 'error'
        """
        if not self.service_healthy:
            return {'error': 'AI service not healthy'}

        payload = {
            "board": board,
            "current_player": current_player,
            "column_positions": column_positions,
            "score_x": score_x,
            "score_o": score_o
        }

        try:
            response = requests.post(
                f"{self.ai_url}/get-move",
                json=payload,
                timeout=10
            )

            if response.status_code == 200:
                data = response.json()
                return {
                    'column': data['column'],
                    'confidence': data['confidence'],
                    'nodes_explored': data['nodes_explored'],
                    'time_ms': data['computation_time_ms']
                }
            else:
                return {'error': response.json().get('error', 'API error')}

        except requests.exceptions.Timeout:
            return {'error': 'Request timeout (>10s)'}
        except requests.exceptions.ConnectionError:
            return {'error': 'Cannot connect to AI service'}
        except Exception as e:
            return {'error': f'Unexpected error: {str(e)}'}

    def get_analysis(self, board, current_player, column_positions,
                     score_x=0, score_o=0):
        """Get detailed analysis of all moves."""
        payload = {
            "board": board,
            "current_player": current_player,
            "column_positions": column_positions,
            "score_x": score_x,
            "score_o": score_o
        }

        try:
            response = requests.post(
                f"{self.ai_url}/analyze",
                json=payload,
                timeout=10
            )

            if response.status_code == 200:
                return response.json()
            else:
                return None

        except:
            return None


# Usage in your game:
def example_usage():
    # Initialize AI client
    ai_client = ConnectFourAIClient()

    # Check health on startup
    if not ai_client.check_health():
        print("WARNING: AI service not available!")
        return

    # Example game state (mid-game)
    board = [
        [".", ".", ".", ".", ".", ".", "."],
        [".", ".", ".", ".", ".", ".", "."],
        [".", ".", ".", ".", ".", ".", "."],
        [".", ".", ".", "X", ".", ".", "."],
        [".", ".", "O", "X", ".", ".", "."],
        [".", "X", "O", "O", ".", ".", "."]
    ]
    current_player = "X"
    column_positions = [5, 4, 3, 2, 5, 5, 5]

    # Get AI move
    result = ai_client.get_move(
        board=board,
        current_player=current_player,
        column_positions=column_positions,
        score_x=0,
        score_o=0
    )

    if 'error' in result:
        print(f"Error: {result['error']}")
        # Fallback to random or other strategy
    else:
        column = result['column']
        confidence = result['confidence']
        print(f"AI plays column {column} (1-indexed: {column + 1})")
        print(f"Confidence: {confidence:.1%}")
        print(f"Computation time: {result['time_ms']}ms")
```

## Error Handling

### Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| Connection refused | AI service not running | Start with `python api.py` |
| Timeout | Position too complex | Increase timeout or reduce MAX_ITERATIONS |
| 400 Bad Request | Invalid JSON format | Check required fields: board, current_player, column_positions |
| 500 Internal Error | AI logic error | Check logs, verify board state is valid |

### Robust Error Handling Pattern

```python
def safe_ai_move(game_state, fallback_strategy=None):
    """Get AI move with fallback."""
    try:
        result = ai_client.get_move(**game_state)

        if 'error' in result:
            # AI failed, use fallback
            if fallback_strategy:
                return fallback_strategy(game_state)
            else:
                # Random valid move
                valid_cols = [i for i, pos in enumerate(game_state['column_positions']) if pos >= 0]
                return random.choice(valid_cols) if valid_cols else 0

        return result['column']

    except Exception as e:
        print(f"AI error: {e}")
        # Use fallback
        if fallback_strategy:
            return fallback_strategy(game_state)
        return 0
```

## Configuration

### AI Difficulty Tuning

Edit `config.py` to adjust AI strength:

```python
# config.py
class AIConfig:
    MAX_ITERATIONS = 2000  # More = stronger but slower
    EXPLORATION_CONSTANT = 1.41  # Higher = more exploratory
```

**Performance Guide**:
- 500 iterations: ~200ms, beginner level
- 1000 iterations: ~400ms, intermediate
- 2000 iterations: ~800ms, advanced (current default)
- 5000 iterations: ~2000ms, expert

### Server Configuration

```python
# config.py
class AIConfig:
    SERVER_HOST = '0.0.0.0'  # Listen on all interfaces
    SERVER_PORT = 5000
    DEBUG_MODE = True  # Set False in production
```

## Testing Your Integration

### 1. Test with example_client.py

```bash
# In one terminal: Start AI service
python src/api.py

# In another terminal: Run test client
python src/example_client.py
```

### 2. Manual API Test

```bash
curl -X POST http://localhost:5000/get-move \
  -H "Content-Type: application/json" \
  -d '{
    "board": [[".",".",".",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."],[".",".",".",".",".",".","."]],
    "current_player": "X",
    "column_positions": [5,5,5,5,5,5,5]
  }'
```

Expected response:
```json
{
  "column": 3,
  "confidence": 0.5,
  "nodes_explored": 2000,
  "computation_time_ms": 750
}
```

## Best Practices

1. **Always check health on startup** - Don't assume the service is running
2. **Set appropriate timeouts** - 5-10 seconds is reasonable
3. **Log AI moves with metadata** - Store confidence/time for analysis
4. **Have a fallback strategy** - Random move if AI fails
5. **Don't block the UI** - Make async requests if possible
6. **Validate AI responses** - Check column is valid before applying
7. **Monitor performance** - Track computation times to tune iterations

## Deployment Considerations

### Running as a Service (Production)

Use a production WSGI server instead of Flask's development server:

```bash
# Install gunicorn
pip install gunicorn

# Run with 4 workers
gunicorn -w 4 -b 0.0.0.0:5000 api:app
```

### Docker Deployment

The AI service can run in a Docker container (see Dockerfile in project).

```bash
# Build container
docker build -t connect-four-ai .

# Run container
docker run -p 5000:5000 connect-four-ai
```

Then connect from your game using `http://localhost:5000`.

## Troubleshooting

### AI Returns Invalid Moves

**Check**: Column positions array is correct
- Must match actual board state
- -1 for full columns
- Valid range: -1 to 5

### Slow Response Times

**Solutions**:
- Reduce MAX_ITERATIONS in config.py
- Use faster hardware
- Profile with `/analyze` endpoint to see statistics

### Inconsistent Move Quality

**Possible causes**:
- MCTS is probabilistic, slight variations are normal
- Very early game has many equivalent moves
- Increase MAX_ITERATIONS for more consistent play

## Summary

To integrate the MCTS AI:

1. **Start the service**: `python api.py`
2. **Check health**: `GET /health`
3. **Send game state**: `POST /get-move` with board, player, positions
4. **Receive column**: 0-indexed column number
5. **Apply move**: Use column in your game logic
6. **Handle errors**: Fallback to random/other strategy

The AI will return the best move based on 2000 MCTS simulations, typically in under 1 second.