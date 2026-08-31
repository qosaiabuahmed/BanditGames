-- =====================================================
-- Connect Four Gameplay Logging Database Schema
-- =====================================================
-- This schema supports comprehensive gameplay logging
-- for AI training, analytics, and player insights
-- =====================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS moves CASCADE;
DROP TABLE IF EXISTS games CASCADE;
DROP TABLE IF EXISTS players CASCADE;
DROP TABLE IF EXISTS game_configurations CASCADE;

-- =====================================================
-- PLAYERS TABLE
-- =====================================================
-- Stores information about all players (human and AI)
CREATE TABLE players (
    player_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    player_type VARCHAR(20) NOT NULL CHECK (player_type IN ('human', 'ai_easy', 'ai_hard', 'ai_mcts', 'ml_model')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- AI-specific metadata
    ai_algorithm VARCHAR(50),           -- e.g., 'MCTS', 'minimax', 'random'
    ai_difficulty INTEGER,              -- 1-10 difficulty level
    ai_config JSONB,                    -- AI configuration parameters

    -- Player statistics (updated via triggers or application)
    total_games INTEGER DEFAULT 0,
    total_wins INTEGER DEFAULT 0,
    total_losses INTEGER DEFAULT 0,
    total_draws INTEGER DEFAULT 0,

    -- Player scoring system
    score INTEGER DEFAULT 1000  -- Starting score: 1000 points
);

-- Indexes for common queries
CREATE INDEX idx_players_username ON players(username);
CREATE INDEX idx_players_type ON players(player_type);

-- =====================================================
-- GAME_CONFIGURATIONS TABLE
-- =====================================================
-- Stores different game board configurations
CREATE TABLE game_configurations (
    config_id SERIAL PRIMARY KEY,
    rows INTEGER NOT NULL CHECK (rows >= 4),
    cols INTEGER NOT NULL CHECK (cols >= 4),
    connect_length INTEGER DEFAULT 4,
    high_score_limit INTEGER DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Unique constraint on configuration
    UNIQUE(rows, cols, connect_length)
);

-- Insert default configuration
INSERT INTO game_configurations (rows, cols, connect_length, high_score_limit)
VALUES (6, 7, 4, 5);

-- =====================================================
-- GAMES TABLE
-- =====================================================
-- Stores metadata for each game session
CREATE TABLE games (
    game_id SERIAL PRIMARY KEY,

    -- Player information
    player_x_id INTEGER NOT NULL REFERENCES players(player_id),
    player_o_id INTEGER NOT NULL REFERENCES players(player_id),

    -- Game configuration
    config_id INTEGER NOT NULL REFERENCES game_configurations(config_id),

    -- Game metadata
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,

    -- Game outcome
    winner VARCHAR(10) CHECK (winner IN ('X', 'O', 'Tie', NULL)),
    final_score_x INTEGER DEFAULT 0,
    final_score_o INTEGER DEFAULT 0,
    total_moves INTEGER DEFAULT 0,

    -- Game state
    game_status VARCHAR(20) DEFAULT 'in_progress' CHECK (game_status IN ('in_progress', 'completed', 'abandoned')),

    -- Final board state (JSON for easy querying)
    final_board_state JSONB,

    -- Additional metadata
    game_mode VARCHAR(50),              -- e.g., 'pvp', 'pve_easy', 'pve_hard', 'ai_vs_ai'
    metadata JSONB                      -- Extensible field for additional data
);

-- Indexes for common queries
CREATE INDEX idx_games_player_x ON games(player_x_id);
CREATE INDEX idx_games_player_o ON games(player_o_id);
CREATE INDEX idx_games_status ON games(game_status);
CREATE INDEX idx_games_started_at ON games(started_at);
CREATE INDEX idx_games_winner ON games(winner);

-- =====================================================
-- MOVES TABLE
-- =====================================================
-- Logs every move made during gameplay
-- This is the core table for ML training and analytics
CREATE TABLE moves (
    move_id SERIAL PRIMARY KEY,

    -- Game and player context
    game_id INTEGER NOT NULL REFERENCES games(game_id) ON DELETE CASCADE,
    player_id INTEGER NOT NULL REFERENCES players(player_id),

    -- Move information
    move_number INTEGER NOT NULL,       -- Turn number (0-indexed)
    column_played INTEGER NOT NULL,     -- Column where piece was placed (0-indexed)
    row_placed INTEGER NOT NULL,        -- Row where piece landed (0-indexed)

    -- Timestamp
    move_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Game state BEFORE the move
    board_state_before JSONB NOT NULL,  -- Full board state before move
    score_x_before INTEGER NOT NULL,
    score_o_before INTEGER NOT NULL,

    -- Game state AFTER the move
    board_state_after JSONB NOT NULL,   -- Full board state after move
    score_x_after INTEGER NOT NULL,
    score_o_after INTEGER NOT NULL,

    -- Heuristic evaluation
    heuristic_value FLOAT,              -- Position evaluation from player's perspective
    heuristic_value_x FLOAT,            -- Evaluation from X's perspective
    heuristic_value_o FLOAT,            -- Evaluation from O's perspective

    -- Move metadata
    legal_moves JSONB,                  -- List of all legal moves at this state

    -- Complete game state snapshot (for restore/replay)
    game_state_snapshot JSONB,          -- Full GameState including move_history, redo_stack, column_positions

    -- Additional extensible data
    metadata JSONB,

    -- Constraints
    UNIQUE(game_id, move_number),
    CHECK (move_number >= 0),
    CHECK (column_played >= 0),
    CHECK (row_placed >= 0)
);

-- Indexes for analytics and ML queries
CREATE INDEX idx_moves_game_id ON moves(game_id);
CREATE INDEX idx_moves_player_id ON moves(player_id);
CREATE INDEX idx_moves_move_number ON moves(move_number);
CREATE INDEX idx_moves_timestamp ON moves(move_timestamp);
CREATE INDEX idx_moves_heuristic ON moves(heuristic_value);

-- GIN index for JSONB columns (for efficient querying)
CREATE INDEX idx_moves_board_state_before ON moves USING GIN(board_state_before);
CREATE INDEX idx_moves_board_state_after ON moves USING GIN(board_state_after);
CREATE INDEX idx_moves_game_state_snapshot ON moves USING GIN(game_state_snapshot);

-- =====================================================
-- VIEWS FOR ANALYTICS
-- =====================================================

-- View: Game statistics with player information
CREATE VIEW v_game_stats AS
SELECT
    g.game_id,
    g.started_at,
    g.ended_at,
    EXTRACT(EPOCH FROM (g.ended_at - g.started_at)) as duration_seconds,
    px.username as player_x_name,
    px.player_type as player_x_type,
    po.username as player_o_name,
    po.player_type as player_o_type,
    g.winner,
    g.final_score_x,
    g.final_score_o,
    g.total_moves,
    g.game_mode,
    gc.rows,
    gc.cols
FROM games g
JOIN players px ON g.player_x_id = px.player_id
JOIN players po ON g.player_o_id = po.player_id
JOIN game_configurations gc ON g.config_id = gc.config_id;

-- View: Move analysis with game context
CREATE VIEW v_move_analysis AS
SELECT
    m.move_id,
    m.game_id,
    m.move_number,
    m.column_played,
    m.row_placed,
    p.username as player_name,
    p.player_type,
    m.heuristic_value,
    m.heuristic_value_x,
    m.heuristic_value_o,
    m.score_x_after - m.score_x_before as score_x_change,
    m.score_o_after - m.score_o_before as score_o_change,
    m.move_timestamp
FROM moves m
JOIN players p ON m.player_id = p.player_id;

-- =====================================================
-- FUNCTIONS AND TRIGGERS
-- =====================================================

-- Function to update game statistics
CREATE OR REPLACE FUNCTION update_game_stats()
RETURNS TRIGGER AS $$
BEGIN
    -- Update total moves count
    UPDATE games
    SET total_moves = (
        SELECT COUNT(*)
        FROM moves
        WHERE game_id = NEW.game_id
    )
    WHERE game_id = NEW.game_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update game stats on move insert
CREATE TRIGGER trg_update_game_stats
AFTER INSERT ON moves
FOR EACH ROW
EXECUTE FUNCTION update_game_stats();

-- =====================================================
-- SAMPLE QUERIES
-- =====================================================

-- Query 1: Get all moves for a specific game
-- SELECT * FROM v_move_analysis WHERE game_id = 1 ORDER BY move_number;

-- Query 2: Player win rates
-- SELECT
--     username,
--     player_type,
--     total_games,
--     total_wins,
--     ROUND(100.0 * total_wins / NULLIF(total_games, 0), 2) as win_rate
-- FROM players
-- WHERE total_games > 0
-- ORDER BY win_rate DESC;

-- Query 3: Average game duration by game mode
-- SELECT
--     game_mode,
--     COUNT(*) as games_played,
--     AVG(duration_seconds) as avg_duration,
--     AVG(total_moves) as avg_moves
-- FROM v_game_stats
-- WHERE ended_at IS NOT NULL
-- GROUP BY game_mode;

-- Query 4: Most popular move positions
-- SELECT
--     column_played,
--     COUNT(*) as times_played,
--     AVG(heuristic_value) as avg_heuristic
-- FROM moves
-- GROUP BY column_played
-- ORDER BY times_played DESC;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE players IS 'Stores player information including human and AI players';
COMMENT ON TABLE games IS 'Stores game session metadata and outcomes';
COMMENT ON TABLE moves IS 'Logs every move with complete state and evaluation data';
COMMENT ON TABLE game_configurations IS 'Stores different board configurations';

COMMENT ON COLUMN moves.board_state_before IS 'JSON representation of board before move';
COMMENT ON COLUMN moves.board_state_after IS 'JSON representation of board after move';
COMMENT ON COLUMN moves.heuristic_value IS 'Position evaluation from current player perspective';
COMMENT ON COLUMN moves.game_state_snapshot IS 'Complete GameState including move_history, redo_stack, and column_positions for full game restoration';

-- =====================================================
-- ML PREDICTIONS TABLE (Part 2b Requirement)
-- =====================================================
-- Logs inference predictions made by deployed ML models
-- Used for admin monitoring and analysis of model usage
CREATE TABLE ml_predictions (
    prediction_id SERIAL PRIMARY KEY,

    -- Timestamp
    predicted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Prediction type and model
    prediction_type VARCHAR(20) NOT NULL CHECK (prediction_type IN ('policy', 'value')),
    model_type VARCHAR(10) NOT NULL CHECK (model_type IN ('cnn', 'lr')),

    -- Input data
    board_state JSONB NOT NULL,
    legal_moves INTEGER[],

    -- Policy model outputs (move prediction)
    predicted_move INTEGER,
    move_probabilities JSONB,

    -- Value model outputs (win probability)
    win_probability FLOAT,

    -- Performance metadata
    response_time_ms INTEGER,

    -- Optional request metadata
    request_ip VARCHAR(45),
    user_agent TEXT
);

-- Indexes for common queries
CREATE INDEX idx_ml_predictions_timestamp ON ml_predictions(predicted_at);
CREATE INDEX idx_ml_predictions_type_model ON ml_predictions(prediction_type, model_type);
CREATE INDEX idx_ml_predictions_date ON ml_predictions(DATE(predicted_at));

-- GIN index for board state queries
CREATE INDEX idx_ml_predictions_board ON ml_predictions USING GIN(board_state);

-- Comments
COMMENT ON TABLE ml_predictions IS 'Logs all ML model inference predictions for monitoring and analysis';
COMMENT ON COLUMN ml_predictions.prediction_type IS 'Type of prediction: policy (move) or value (win probability)';
COMMENT ON COLUMN ml_predictions.model_type IS 'Model used: cnn or lr';
COMMENT ON COLUMN ml_predictions.board_state IS 'Board state at time of prediction';
COMMENT ON COLUMN ml_predictions.move_probabilities IS 'Probability distribution over all possible moves (policy models only)';
COMMENT ON COLUMN ml_predictions.win_probability IS 'Predicted win probability from -1 to +1 (value models only)';
