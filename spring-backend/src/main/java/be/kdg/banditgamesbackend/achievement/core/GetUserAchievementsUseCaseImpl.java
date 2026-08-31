package be.kdg.banditgamesbackend.achievement.core;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;
import be.kdg.banditgamesbackend.achievement.port.in.GetUserAchievementsUseCase;
import be.kdg.banditgamesbackend.achievement.port.out.LoadUserAchievementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserAchievementsUseCaseImpl implements GetUserAchievementsUseCase {

    private final LoadUserAchievementPort loadUserAchievementPort;

    @Override
    public List<UserAchievement> getUserAchievements(UUID userId) {
        return loadUserAchievementPort.findByUserId(userId);
    }

    @Override
    public List<UserAchievement> getUserAchievementsByGame(UUID userId, UUID gameId) {
        return loadUserAchievementPort.findByUserIdAndGame(userId, gameId);
    }
}
