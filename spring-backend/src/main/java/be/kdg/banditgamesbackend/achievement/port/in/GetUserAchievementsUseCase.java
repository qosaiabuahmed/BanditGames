package be.kdg.banditgamesbackend.achievement.port.in;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;

import java.util.List;
import java.util.UUID;

public interface GetUserAchievementsUseCase {
    List<UserAchievement> getUserAchievements(UUID userId);
    List<UserAchievement> getUserAchievementsByGame(UUID userId, UUID gameId);
}
