package be.kdg.banditgamesbackend.achievement.adapter.out;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;
import be.kdg.banditgamesbackend.achievement.port.out.LoadUserAchievementPort;
import be.kdg.banditgamesbackend.achievement.port.out.SaveUserAchievementPort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAchievementPortAdapter implements SaveUserAchievementPort, LoadUserAchievementPort {
    
    private final UserAchievementJpaRepository repository;
    private final UserAchievementJpaMapper mapper;
    
    @Override
    public void save(UserAchievement achievement) {
        UserAchievementJpaEntity entity = mapper.toEntity(achievement);
        repository.save(entity);
    }

    @Override
    public boolean hasUnlocked(UUID userId, UUID gameId, String achievementCode) {
        return repository.existsByUserIdAndGameIdAndAchievementCode(
            userId, 
            gameId, 
            achievementCode
        );
    }

    @Override
    public List<UserAchievement> findByUserIdAndGame(UUID userId, UUID gameId) {
        return repository.findByUserIdAndGameId(userId, gameId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<UserAchievement> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}

