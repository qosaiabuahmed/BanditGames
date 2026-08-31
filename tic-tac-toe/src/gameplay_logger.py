"""
Gameplay Logger Module for Tic-Tac-Toe

Handles logging all gameplay data to PostgreSQL database for:
- Game analytics and insights
- Move history tracking
- Player statistics
"""

import psycopg2
from psycopg2 import pool
from psycopg2.extras import Json, RealDictCursor
from typing import Optional, Dict, Any, List
from datetime import datetime
from contextlib import contextmanager
from dataclasses import dataclass
import os


@dataclass
class PlayerInfo:
    """Information about a player."""
    player_id: Optional[int] = None
    username: str = ""
    player_type: str = "human"


@dataclass
class MoveLog:
    """Complete information about a single move."""
    game_id: int
    player_id: int
    move_number: int
    row: int
    col: int

    # Board states
    board_state_before: List[List[str]]
    board_state_after: List[List[str]]

    # Move metadata
    legal_moves: Optional[List[Dict[str, int]]] = None
    metadata: Optional[Dict] = None


class DatabaseConfig:
    """Database configuration from environment variables."""

    def __init__(self):
        self.host = os.getenv('DB_HOST', 'localhost')
        self.port = os.getenv('DB_PORT', '5432')
        self.database = os.getenv('DB_NAME', 'tictactoe')
        self.user = os.getenv('DB_USER', 'postgres')
        self.password = os.getenv('DB_PASSWORD', 'postgres')
        self.min_connections = int(os.getenv('DB_MIN_CONN', '1'))
        self.max_connections = int(os.getenv('DB_MAX_CONN', '10'))

    def get_connection_string(self) -> str:
        """Get PostgreSQL connection string."""
        return f"host={self.host} port={self.port} dbname={self.database} user={self.user} password={self.password}"


class GameplayLogger:
    """
    Manages logging of all gameplay data to PostgreSQL.

    Features:
    - Connection pooling for efficient database access
    - Comprehensive move logging with game state
    - Player and game management
    """

    def __init__(self, config: Optional[DatabaseConfig] = None):
        """
        Initialize the gameplay logger.

        Args:
            config: Database configuration (uses defaults if None)
        """
        self.config = config or DatabaseConfig()
        self.connection_pool = None
        self._initialize_pool()

    def _initialize_pool(self):
        """Initialize the connection pool."""
        try:
            self.connection_pool = psycopg2.pool.SimpleConnectionPool(
                self.config.min_connections,
                self.config.max_connections,
                host=self.config.host,
                port=self.config.port,
                database=self.config.database,
                user=self.config.user,
                password=self.config.password,
                keepalives=1,
                keepalives_idle=30,
                keepalives_interval=10,
                keepalives_count=5
            )
            print(f"[OK] Database connection pool initialized")
        except psycopg2.Error as e:
            print(f"[WARNING] Failed to initialize database connection pool: {e}")
            raise

    @contextmanager
    def get_connection(self):
        """
        Context manager for getting a database connection from the pool.

        Yields:
            Database connection
        """
        conn = None
        try:
            if not self.connection_pool:
                raise Exception("Connection pool not initialized")
            conn = self.connection_pool.getconn()
            if conn.closed:
                # Connection is closed, remove it and get a new one
                self.connection_pool.putconn(conn, close=True)
                conn = self.connection_pool.getconn()
            yield conn
            conn.commit()
        except Exception as e:
            if conn:
                conn.rollback()
            raise
        finally:
            if conn and not conn.closed:
                self.connection_pool.putconn(conn)

    def close(self):
        """Close all connections in the pool."""
        if self.connection_pool:
            self.connection_pool.closeall()

    # =========================================================================
    # PLAYER MANAGEMENT
    # =========================================================================

    def create_player(self, player_info: PlayerInfo) -> int:
        """
        Create or get existing player.

        Args:
            player_info: Player information

        Returns:
            player_id
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                # Try to get existing player first
                cur.execute(
                    "SELECT player_id FROM tictactoe.players WHERE username = %s",
                    (player_info.username,)
                )
                result = cur.fetchone()

                if result:
                    return result[0]

                # Create new player
                cur.execute("""
                    INSERT INTO tictactoe.players (username, player_type)
                    VALUES (%s, %s)
                    RETURNING player_id
                """, (
                    player_info.username,
                    player_info.player_type
                ))

                player_id = cur.fetchone()[0]
                return player_id

    def get_player(self, username: str) -> Optional[Dict]:
        """
        Get player information by username.

        Args:
            username: Player username

        Returns:
            Player information dict or None
        """
        with self.get_connection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(
                    "SELECT * FROM tictactoe.players WHERE username = %s",
                    (username,)
                )
                return cur.fetchone()

    def get_player_stats(self, player_id: int) -> Optional[Dict]:
        """
        Get statistics for a player.

        Args:
            player_id: Player ID

        Returns:
            Player statistics dict
        """
        with self.get_connection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute("""
                    SELECT
                        p.*,
                        ROUND(100.0 * p.total_wins / NULLIF(p.total_games, 0), 2) as win_rate
                    FROM tictactoe.players p
                    WHERE player_id = %s
                """, (player_id,))
                return cur.fetchone()

    # =========================================================================
    # GAME MANAGEMENT
    # =========================================================================

    def create_game(
        self,
        player_x_id: int,
        player_o_id: int,
        board_size: int = 3,
        game_mode: str = "pvp"
    ) -> int:
        """
        Create a new game session.

        Args:
            player_x_id: ID of player X
            player_o_id: ID of player O
            board_size: Size of the board (default 3x3)
            game_mode: Game mode (pvp, pve, ai_vs_ai)

        Returns:
            game_id
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                # Create game
                cur.execute("""
                    INSERT INTO tictactoe.games (player_x_id, player_o_id, board_size, game_mode, started_at)
                    VALUES (%s, %s, %s, %s, NOW())
                    RETURNING game_id
                """, (player_x_id, player_o_id, board_size, game_mode))

                game_id = cur.fetchone()[0]
                return game_id

    def end_game(
        self,
        game_id: int,
        winner: Optional[str],
        final_board_state: List[List[str]]
    ):
        """
        Mark a game as completed.

        Args:
            game_id: Game ID
            winner: Winner ('x', 'o', or 'draw')
            final_board_state: Final board state
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                # Convert board state to JSON-serializable format
                board_json = [[cell for cell in row] for row in final_board_state]

                cur.execute("""
                    UPDATE tictactoe.games
                    SET
                        ended_at = NOW(),
                        winner = %s,
                        final_board_state = %s,
                        game_status = 'completed'
                    WHERE game_id = %s
                """, (winner, Json(board_json), game_id))

                # Update player statistics
                if winner and winner != 'draw':
                    # Get player IDs
                    cur.execute(
                        "SELECT player_x_id, player_o_id FROM tictactoe.games WHERE game_id = %s",
                        (game_id,)
                    )
                    player_x_id, player_o_id = cur.fetchone()

                    winner_id = player_x_id if winner == 'x' else player_o_id
                    loser_id = player_o_id if winner == 'x' else player_x_id

                    # Update winner: +1 win, +10 score points
                    cur.execute("""
                        UPDATE tictactoe.players
                        SET total_games = total_games + 1,
                            total_wins = total_wins + 1,
                            score = score + 10
                        WHERE player_id = %s
                    """, (winner_id,))

                    # Update loser: +1 loss, -5 score points
                    cur.execute("""
                        UPDATE tictactoe.players
                        SET total_games = total_games + 1,
                            total_losses = total_losses + 1,
                            score = score - 5
                        WHERE player_id = %s
                    """, (loser_id,))
                elif winner == 'draw':
                    # Update both players: +1 draw, +2 score points each
                    cur.execute("""
                        UPDATE tictactoe.players
                        SET total_games = total_games + 1,
                            total_draws = total_draws + 1,
                            score = score + 2
                        WHERE player_id IN (
                            SELECT player_x_id FROM tictactoe.games WHERE game_id = %s
                            UNION
                            SELECT player_o_id FROM tictactoe.games WHERE game_id = %s
                        )
                    """, (game_id, game_id))

    # =========================================================================
    # MOVE LOGGING
    # =========================================================================

    def log_move(self, move_log: MoveLog):
        """
        Log a single move with complete state information.

        Args:
            move_log: Complete move information
        """
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                # Convert board states to JSON
                board_before_json = [[cell for cell in row] for row in move_log.board_state_before]
                board_after_json = [[cell for cell in row] for row in move_log.board_state_after]

                cur.execute("""
                    INSERT INTO tictactoe.moves (
                        game_id, player_id, move_number, row, col,
                        board_state_before, board_state_after,
                        legal_moves, metadata
                    ) VALUES (
                        %s, %s, %s, %s, %s,
                        %s, %s,
                        %s, %s
                    )
                """, (
                    move_log.game_id,
                    move_log.player_id,
                    move_log.move_number,
                    move_log.row,
                    move_log.col,
                    Json(board_before_json),
                    Json(board_after_json),
                    Json(move_log.legal_moves) if move_log.legal_moves else None,
                    Json(move_log.metadata) if move_log.metadata else None
                ))

    # =========================================================================
    # ANALYTICS & QUERIES
    # =========================================================================

    def get_game_moves(self, game_id: int) -> List[Dict]:
        """
        Get all moves for a specific game.

        Args:
            game_id: Game ID

        Returns:
            List of move dictionaries
        """
        with self.get_connection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute("""
                    SELECT
                        m.*,
                        p.username as player_username
                    FROM tictactoe.moves m
                    JOIN tictactoe.players p ON m.player_id = p.player_id
                    WHERE m.game_id = %s
                    ORDER BY m.move_number
                """, (game_id,))
                return cur.fetchall()

    def get_player_games(self, player_id: int, limit: int = 10) -> List[Dict]:
        """
        Get recent games for a player.

        Args:
            player_id: Player ID
            limit: Number of games to return

        Returns:
            List of game dictionaries
        """
        with self.get_connection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute("""
                    SELECT
                        g.*,
                        px.username as player_x_username,
                        po.username as player_o_username
                    FROM tictactoe.games g
                    JOIN tictactoe.players px ON g.player_x_id = px.player_id
                    JOIN tictactoe.players po ON g.player_o_id = po.player_id
                    WHERE g.player_x_id = %s OR g.player_o_id = %s
                    ORDER BY g.started_at DESC
                    LIMIT %s
                """, (player_id, player_id, limit))
                return cur.fetchall()


if __name__ == "__main__":
    # Example usage
    print("Gameplay Logger Module")
    print("=" * 50)

    # Test database connection
    try:
        logger = GameplayLogger()
        print("[OK] Successfully connected to database")

        # Test player creation
        player1 = PlayerInfo(username="test_player_1", player_type="human")
        player1_id = logger.create_player(player1)
        print(f"[OK] Created player: {player1.username} (ID: {player1_id})")

        player2 = PlayerInfo(username="test_player_2", player_type="human")
        player2_id = logger.create_player(player2)
        print(f"[OK] Created player: {player2.username} (ID: {player2_id})")

        logger.close()
        print("[OK] Database connection closed")

    except Exception as e:
        print(f"[ERROR] Error: {e}")
