package be.kdg.banditgamesbackend.achievement.adapter.out;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;
import be.kdg.banditgamesbackend.achievement.port.out.LoadTempAchievementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadTempAchievementPortAdapter implements LoadTempAchievementPort {

    private final TempAchievementJpaRepository repository;
    private final TempAchievementMapper tempAchievementMapper;

    @Override
    public List<TempAchievement> findByUserId(UUID userId) {
        return repository.findByGuestUserId(userId).stream()
                .map(tempAchievementMapper::toDomain)
                .toList();
    }

    @Override
    public List<TempAchievement> findByUserIdAndGame(UUID userId, UUID gameId) {
        return repository.findByGuestUserIdAndGameId(userId, gameId).stream()
                .map(tempAchievementMapper::toDomain)
                .toList();
    }
}
