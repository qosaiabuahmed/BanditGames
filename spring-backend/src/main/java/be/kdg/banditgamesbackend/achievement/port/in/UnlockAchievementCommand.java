package be.kdg.banditgamesbackend.achievement.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public record UnlockAchievementCommand(
    UUID userId,
    UUID gameId,
    UUID matchId,
    String achievementCode,
    LocalDateTime unlockedAt
) {
    public UnlockAchievementCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (gameId == null) {
            throw new IllegalArgumentException("Game ID is required");
        }
        if (matchId == null) {
            throw new IllegalArgumentException("Match ID is required");
        }
        if (achievementCode == null) {
            throw new IllegalArgumentException("Achievement code is required");
        }
        if (unlockedAt == null) {
            throw new IllegalArgumentException("Unlocked at is required");
        }
    }
}
