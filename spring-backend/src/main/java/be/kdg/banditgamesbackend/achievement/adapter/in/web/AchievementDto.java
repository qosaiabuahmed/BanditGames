package be.kdg.banditgamesbackend.achievement.adapter.in.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record AchievementDto(
        UUID achievementId,
        UUID userId,
        UUID gameId,
        UUID matchId,
        String achievementCode,
        LocalDateTime unlockedAt
) {
}
