# Connect Four AI Service

MCTS AI microservice for Connect Four.

## Setup

Integrated in main project. Start from Game_Backend directory:

```bash
cd "/Users/seppverbuyst/Documents/KdG 25-26/Integration/Game_Backend/game-backend"
docker-compose up -d
```

AI service runs on port 5001 (external) or `http://ai:5000` (internal).

## API

### Health Check
```
GET /health
```

### Get Move
```
POST /get-move

{
  "board": [["X", ".", ...], ...],
  "current_player": "X",
  "column_positions": [2, 0, ...],
  "score_x": 0,
  "score_o": 0
}

Returns: {"column": 3, "confidence": 0.85, ...}
```

### Analyze
```
POST /analyze

Same input format as /get-move
Returns detailed analysis of all moves
```

**Notes:**
- Case-insensitive (accepts 'x' or 'X')
- Handles '0' as 'O'
- Extra fields ignored
