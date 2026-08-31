package be.kdg.banditgamesbackend.acl.config;

import org.springframework.stereotype.Component;

@Component
public final class ChessGameDefaults {
    /**
     * These are hardcoded since the external chess game doesn't provide them in the message but our backend needs them.
     */
    public static final String NAME = "Chess";
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 2;
    public static final String DESCRIPTION = """
            Chess is a classic two-player strategy board game played on an 8×8 grid, where each player controls 16 pieces with unique movements.
            The goal is to outmaneuver your opponent and place their king in checkmate, meaning it cannot escape capture.
            Chess combines logic, planning, and creativity, and is played competitively and recreationally around the world.
            """;
    public static final String RULES_URL = "https://www.fide.com/rules-of-chess";
    public static final String RULES_TEXT = """
            Chess is played by two players on an 8×8 board.
            Each player starts with 16 pieces: king, queen, rooks, bishops, knights, and pawns, each moving in specific ways.
            Players take turns moving one piece at a time, aiming to attack the opponent’s king.
            The game ends in checkmate, when the king is under attack and cannot escape.
            Special rules include castling, pawn promotion, and en passant. A game may also end in a draw under certain conditions.
            """;
    public static final String SUMMARY = """
            Chess is a strategic two-player board game where each player uses 16 pieces on an 8×8 board.
            The objective is to checkmate the opponent’s king by planning moves, controlling the board, and using each piece’s unique abilities.
            """;
    public static final String CATEGORY = "Strategy Board Game";
    public static final String THEME = "Medieval Battle Strategy";
    public static final String DESIGNER = "Smeyers Kevin";
    public static final String PUBLISHER = "Karel de Grote";
    public static final int RELEASE_YEAR = 2025;
    public static final int PLAY_TIME_MINUTES = 30;
    public static final String COMPLEXITY = "highly complex";

    private ChessGameDefaults() {}
}
