package be.kdg.banditgamesbackend.achievement.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

@Getter
public class UserAchievement {
    private final UserAchievementId userAchievementId;
    private final UUID userId;
    private final UUID gameId;
    private final UUID matchId;
    private final String achievementCode;
    private final LocalDateTime unlockedAt;

    private UserAchievement(UserAchievementId userAchievementId, UUID userId, UUID gameId, UUID matchId, String achievementCode, LocalDateTime unlockedAt) {
        this.userAchievementId = userAchievementId;
        this.userId = userId;
        this.gameId = gameId;
        this.matchId = matchId;
        this.achievementCode = achievementCode;
        this.unlockedAt = unlockedAt;
    }

    public static UserAchievement unlock(UUID userId, UUID gameId, UUID matchId, String achievementCode, LocalDateTime unlockedAt) {
        validateAchievementCode(achievementCode);
        return new UserAchievement(UserAchievementId.generate(), userId, gameId, matchId, achievementCode, unlockedAt);
    }

    public static UserAchievement hydrate(UserAchievementId userAchievementId, UUID userId, UUID gameId, UUID matchId, String achievementCode, LocalDateTime unlockedAt) {
        return new UserAchievement(userAchievementId, userId, gameId, matchId, achievementCode, unlockedAt);
    }

    private static void validateAchievementCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("achievementCode is required");
        }
    }

}
