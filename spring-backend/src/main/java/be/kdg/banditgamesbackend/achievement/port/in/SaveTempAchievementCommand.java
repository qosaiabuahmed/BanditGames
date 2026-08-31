package be.kdg.banditgamesbackend.achievement.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public record SaveTempAchievementCommand(
        UUID guestUserId,
        UUID gameId,
        UUID matchId,
        String achievementCode,
        LocalDateTime unlockedAt
) {
}
