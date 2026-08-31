package be.kdg.banditgamesbackend.gamemetadata.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GameStatistics(
    long totalGames,
    Map<GameStatus, Long> gamesByStatus,
    List<GameSummary> recentGames
) {
    public static class GameSummary {
        private final UUID gameId;
        private final String name;
        private final GameStatus status;
        private final java.time.LocalDateTime registeredAt;

        public GameSummary(UUID gameId, String name, GameStatus status, java.time.LocalDateTime registeredAt) {
            this.gameId = gameId;
            this.name = name;
            this.status = status;
            this.registeredAt = registeredAt;
        }

        public UUID getGameId() {
            return gameId;
        }

        public String getName() {
            return name;
        }

        public GameStatus getStatus() {
            return status;
        }

        public java.time.LocalDateTime getRegisteredAt() {
            return registeredAt;
        }
    }
}
