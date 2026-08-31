package be.kdg.banditgamesbackend.achievement.port.in;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;

import java.util.List;
import java.util.UUID;

public interface LoadTempAchievementUseCase {
    List<TempAchievement> findByUserId(UUID userId);
    List<TempAchievement> findByUserIdAndGame(UUID userId, UUID gameId);

}
