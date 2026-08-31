-- Tic-Tac-Toe Database Schema
-- PostgreSQL Database for gameplay logging and analytics

-- Create schema
DROP SCHEMA IF EXISTS tictactoe CASCADE;
CREATE SCHEMA tictactoe;

-- Set search path to use the tictactoe schema
SET search_path TO tictactoe, public;

-- =============================================================================
-- PLAYERS TABLE
-- =============================================================================
CREATE TABLE tictactoe.players (
    player_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    player_type VARCHAR(20) NOT NULL DEFAULT 'human',

    -- Statistics
    total_games INTEGER DEFAULT 0,
    total_wins INTEGER DEFAULT 0,
    total_losses INTEGER DEFAULT 0,
    total_draws INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,

    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    last_played_at TIMESTAMP,

    -- Constraints
    CONSTRAINT valid_player_type CHECK (player_type IN ('human', 'ai'))
);

CREATE INDEX idx_players_username ON tictactoe.players(username);
CREATE INDEX idx_players_score ON tictactoe.players(score DESC);

-- =============================================================================
-- GAMES TABLE
-- =============================================================================
CREATE TABLE tictactoe.games (
    game_id SERIAL PRIMARY KEY,

    -- Players
    player_x_id INTEGER NOT NULL REFERENCES tictactoe.players(player_id),
    player_o_id INTEGER NOT NULL REFERENCES tictactoe.players(player_id),

    -- Game configuration
    board_size INTEGER NOT NULL DEFAULT 3,
    game_mode VARCHAR(20) NOT NULL DEFAULT 'pvp',

    -- Game outcome
    winner VARCHAR(10),
    final_board_state JSONB,
    game_status VARCHAR(20) DEFAULT 'active',

    -- Timestamps
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP,

    -- Constraints
    CONSTRAINT valid_game_mode CHECK (game_mode IN ('pvp', 'pve', 'ai_vs_ai')),
    CONSTRAINT valid_winner CHECK (winner IN ('x', 'o', 'draw') OR winner IS NULL),
    CONSTRAINT valid_game_status CHECK (game_status IN ('active', 'completed', 'abandoned')),
    CONSTRAINT different_players CHECK (player_x_id != player_o_id)
);

CREATE INDEX idx_games_player_x ON tictactoe.games(player_x_id);
CREATE INDEX idx_games_player_o ON tictactoe.games(player_o_id);
CREATE INDEX idx_games_started_at ON tictactoe.games(started_at DESC);
CREATE INDEX idx_games_status ON tictactoe.games(game_status);

-- =============================================================================
-- MOVES TABLE
-- =============================================================================
CREATE TABLE tictactoe.moves (
    move_id SERIAL PRIMARY KEY,
    game_id INTEGER NOT NULL REFERENCES tictactoe.games(game_id) ON DELETE CASCADE,
    player_id INTEGER NOT NULL REFERENCES tictactoe.players(player_id),

    -- Move details
    move_number INTEGER NOT NULL,
    row INTEGER NOT NULL,
    col INTEGER NOT NULL,

    -- Board states
    board_state_before JSONB NOT NULL,
    board_state_after JSONB NOT NULL,

    -- Additional data
    legal_moves JSONB,
    metadata JSONB,

    -- Timestamp
    played_at TIMESTAMP DEFAULT NOW(),

    -- Constraints
    CONSTRAINT valid_move_number CHECK (move_number >= 0),
    CONSTRAINT unique_move_per_game UNIQUE (game_id, move_number)
);

CREATE INDEX idx_moves_game_id ON tictactoe.moves(game_id);
CREATE INDEX idx_moves_player_id ON tictactoe.moves(player_id);
CREATE INDEX idx_moves_played_at ON tictactoe.moves(played_at DESC);

-- =============================================================================
-- VIEWS
-- =============================================================================

-- View for detailed game analysis
CREATE OR REPLACE VIEW tictactoe.v_game_analysis AS
SELECT
    g.game_id,
    g.board_size,
    g.game_mode,
    g.winner,
    g.game_status,
    px.username as player_x_username,
    po.username as player_o_username,
    g.started_at,
    g.ended_at,
    EXTRACT(EPOCH FROM (g.ended_at - g.started_at)) as duration_seconds,
    COUNT(m.move_id) as total_moves
FROM tictactoe.games g
JOIN tictactoe.players px ON g.player_x_id = px.player_id
JOIN tictactoe.players po ON g.player_o_id = po.player_id
LEFT JOIN tictactoe.moves m ON g.game_id = m.game_id
GROUP BY g.game_id, px.username, po.username;

-- View for player leaderboard
CREATE OR REPLACE VIEW tictactoe.v_player_leaderboard AS
SELECT
    player_id,
    username,
    player_type,
    total_games,
    total_wins,
    total_losses,
    total_draws,
    score,
    CASE
        WHEN total_games > 0 THEN ROUND(100.0 * total_wins / total_games, 2)
        ELSE 0
    END as win_percentage,
    last_played_at
FROM tictactoe.players
WHERE total_games > 0
ORDER BY score DESC, total_wins DESC;

-- =============================================================================
-- FUNCTIONS
-- =============================================================================

-- Function to update player's last_played_at timestamp
CREATE OR REPLACE FUNCTION tictactoe.update_player_last_played()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE tictactoe.players
    SET last_played_at = NOW()
    WHERE player_id IN (
        SELECT player_x_id FROM tictactoe.games WHERE game_id = NEW.game_id
        UNION
        SELECT player_o_id FROM tictactoe.games WHERE game_id = NEW.game_id
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update last_played_at when a move is made
CREATE TRIGGER trigger_update_last_played
AFTER INSERT ON tictactoe.moves
FOR EACH ROW
EXECUTE FUNCTION tictactoe.update_player_last_played();

-- =============================================================================
-- SAMPLE DATA (Optional - for testing)
-- =============================================================================

-- Insert sample players
INSERT INTO tictactoe.players (username, player_type) VALUES
    ('Guest', 'human'),
    ('AI_Easy', 'ai'),
    ('AI_Hard', 'ai')
ON CONFLICT (username) DO NOTHING;

-- =============================================================================
-- GRANTS (Adjust as needed for your environment)
-- =============================================================================

-- Grant permissions to the postgres user (you may want to create a specific app user)
GRANT ALL PRIVILEGES ON SCHEMA tictactoe TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tictactoe TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA tictactoe TO postgres;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA tictactoe TO postgres;

-- =============================================================================
-- COMPLETION MESSAGE
-- =============================================================================

DO $$
BEGIN
    RAISE NOTICE 'Tic-Tac-Toe database schema created successfully!';
    RAISE NOTICE 'Tables: players, games, moves';
    RAISE NOTICE 'Views: v_game_analysis, v_player_leaderboard';
END $$;
