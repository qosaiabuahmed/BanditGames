# Docker Setup Guide

Complete guide for running Connect Four game backend in Docker containers.

---

## Quick Start

### 1. Start containers
```bash
docker-compose up -d --build
```

### 2. Play the game
```bash
docker-compose exec app python /app/src/ui.py
```

Play normally! When done, just quit the game. Containers keep running.

### 3. Stop containers
```bash
docker-compose down
```

---

## How to Test

### Full Test Workflow

**1. Start containers:**
```bash
docker-compose up -d --build
```

**2. Verify containers running:**
```bash
docker-compose ps
# Both should show "Up"
```

**3. Check database tables:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "\dt"
# Should show: games, game_configurations, moves, players
```

**4. Play a test game:**
```bash
docker-compose exec app python /app/src/ui.py
# Start new game
# Make 5-10 moves
# Quit game
```

**5. Verify moves saved:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "SELECT COUNT(*) FROM moves;"
# Should show number of moves you made
```

**6. View the moves:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "
SELECT game_id, move_number, column_played, score_x_after, score_o_after
FROM moves
ORDER BY game_id, move_number;
"
```

**7. Clean up:**
```bash
docker-compose down
```

---

## Daily Usage

```bash
# Start
docker-compose up -d

# Play
docker-compose exec app python /app/src/ui.py

# Stop
docker-compose down

# Fresh start (wipe data)
docker-compose down -v
docker-compose up -d --build
```

---

## Architecture

### Multi-Layer Dockerfile

**Layer 1: Base**
- Python 3.11-slim
- PostgreSQL client + system dependencies

**Layer 2: Dependencies (cached)**
- `pip install -r requirements.txt`
- Only rebuilds when requirements.txt changes

**Layer 3: Application**
- Copy source code from `src/`
- Copy database files from `database/`
- Set PYTHONPATH for imports

**Benefits:**
- Fast rebuilds (dependencies cached)
- Smaller final image
- Efficient layer caching

### Services

**PostgreSQL Database (`db`):**
- Image: postgres:15-alpine
- Port: 5432 (exposed to host)
- Container: connect_four_db
- Volume: postgres_data (persistent)
- Auto-initializes schema.sql on first run
- Health check: Waits until ready before starting app

**Python Application (`app`):**
- Built from Dockerfile
- Container: connect_four_app
- Depends on: db (waits for health check)
- Volume mounts:
  - `./src:/app/src` (hot reload)
  - `./database:/app/database`
- Command: `tail -f /dev/null` (keeps container alive)

**Network:**
- Name: connect_four_network
- Type: Bridge
- Containers communicate using service names

---

## Why docker-entrypoint.sh?

The shell script is **critical** for startup:

### 1. Wait for PostgreSQL to be REALLY ready
Docker health check isn't enough. The script waits for actual query capability.

**Without it:** App crashes because PostgreSQL isn't ready yet.

### 2. Auto-create tables if missing
Checks if tables exist, runs schema.sql if needed.

**Without it:** App crashes with "relation does not exist" errors.

### 3. Auto-apply migrations
Automatically runs all `migration_*.sql` files.

**Without it:** Database schema is outdated.

---

## Environment Variables

Set in `docker-compose.yml`:

```yaml
DB_HOST: db           # Container name (not localhost!)
DB_PORT: 5432
DB_NAME: postgres
DB_USER: postgres
DB_PASSWORD: Student_1234
```

These override your local `.env` file when running in Docker.

---

## Useful Commands

### Database Queries

**Check tables:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "\dt"
```

**Count moves:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "SELECT COUNT(*) FROM moves;"
```

**View recent games:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "
SELECT game_id, game_mode, game_status, started_at
FROM games
ORDER BY game_id DESC
LIMIT 5;
"
```

**Check specific game:**
```bash
docker-compose exec db psql -U postgres -d postgres -c "
SELECT move_number, column_played, score_x_after, score_o_after
FROM moves
WHERE game_id = 1
ORDER BY move_number;
"
```

### Container Operations

**View logs:**
```bash
docker-compose logs app
docker-compose logs db
docker-compose logs -f    # Follow mode
```

**Container status:**
```bash
docker-compose ps
```

**Access container shell:**
```bash
docker-compose exec app bash
```

**Rebuild from scratch:**
```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## Alternative Ways to Play

### Option 1: One-off session (auto-cleanup)
```bash
docker-compose run --rm app python /app/src/ui.py
```
Creates container, runs game, removes container when done.

### Option 2: Attached mode (see startup logs)
```bash
docker-compose up
# In another terminal:
docker-compose exec app python /app/src/ui.py
```

### Option 3: Interactive shell first
```bash
docker-compose exec app bash
python /app/src/ui.py
```

---

## Troubleshooting

### Port 5432 already in use

**Problem:** Local PostgreSQL is running.

**Solution 1:** Stop local PostgreSQL:
```bash
brew services stop postgresql
```

**Solution 2:** Change port in docker-compose.yml:
```yaml
ports:
  - "5433:5432"  # Use 5433 on host
```

### "relation does not exist" errors

**Problem:** Tables weren't created.

**Solution:**
```bash
docker-compose down -v
docker-compose up -d --build
```

### Code changes not reflected

Python files in `src/` are volume-mounted, changes are instant.

If you added new files or changed requirements.txt:
```bash
docker-compose down
docker-compose up -d --build
```

### Database is empty after restart

**Problem:** Volume was deleted.

**Solution:** Don't use `-v` flag unless you want to wipe data:
```bash
docker-compose down      # Keeps data
docker-compose down -v   # WIPES data
```

---

## What Gets Saved?

**Every move:**
- ✅ Board state (before and after)
- ✅ Scores (before and after)
- ✅ Game state snapshot (move history, redo stack, column positions)
- ✅ Heuristic evaluations (for X, O, current player)
- ✅ Legal moves
- ✅ Timestamp

**When game ends:**
- ✅ Winner
- ✅ Final scores
- ✅ Final board state
- ✅ Player statistics (wins/losses/draws)

---

## Development Workflow

### First Time
```bash
git clone <repo>
cd game-backend
docker-compose up -d --build
docker-compose exec app python /app/src/ui.py
```

### Daily Development
```bash
# Start
docker-compose up -d

# Edit code in src/ (changes apply instantly)

# Test
docker-compose exec app python /app/src/ui.py

# Stop
docker-compose down
```

### After Changing requirements.txt
```bash
docker-compose build
docker-compose up -d
```

---

## File Structure

```
game-backend/
├── src/                    # Python source (volume mounted)
│   ├── ui.py              # Main entry point
│   ├── connect_four.py    # Game engine
│   ├── gameplay_logger.py # Database logging
│   └── file_io.py         # File operations
├── database/              # Database files (volume mounted)
│   ├── schema.sql         # Table definitions
│   └── migration_*.sql    # Database migrations
├── docker-entrypoint.sh   # Container startup script
├── Dockerfile             # Multi-layer image build
├── docker-compose.yml     # Service orchestration
└── .dockerignore          # Files excluded from image
```

---

## Production Notes

For production:
1. Remove volume mounts (bake code into image)
2. Use secrets for passwords
3. Add restart policies
4. Use production-grade database
5. Add monitoring/logging

---

## Summary

- **Start:** `docker-compose up -d --build`
- **Play:** `docker-compose exec app python /app/src/ui.py`
- **Stop:** `docker-compose down`
- **Reset:** `docker-compose down -v && docker-compose up -d --build`

Everything is isolated, reproducible, and version controlled! 🎯
