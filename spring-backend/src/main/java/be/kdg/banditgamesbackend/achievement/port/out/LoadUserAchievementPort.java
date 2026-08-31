package be.kdg.banditgamesbackend.achievement.port.out;

import java.util.List;
import java.util.UUID;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;

public interface LoadUserAchievementPort {
    boolean hasUnlocked(UUID userId, UUID gameId, String achievementCode);
    List<UserAchievement> findByUserIdAndGame(UUID userId, UUID gameId);
    List<UserAchievement> findByUserId(UUID userId);
}
