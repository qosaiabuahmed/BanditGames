package be.kdg.banditgamesbackend.achievement.core;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;
import be.kdg.banditgamesbackend.achievement.port.in.LoadTempAchievementUseCase;
import be.kdg.banditgamesbackend.achievement.port.out.LoadTempAchievementPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadTempAchievementUseCaseImpl implements LoadTempAchievementUseCase {

    private final LoadTempAchievementPort loadPort;

    @Override
    public List<TempAchievement> findByUserId(UUID userId) {
        return loadPort.findByUserId(userId);
    }

    @Override
    public List<TempAchievement> findByUserIdAndGame(UUID userId, UUID gameId) {
        return loadPort.findByUserIdAndGame(userId, gameId);
    }
}
