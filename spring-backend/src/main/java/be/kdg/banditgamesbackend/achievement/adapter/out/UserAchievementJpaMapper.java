package be.kdg.banditgamesbackend.achievement.adapter.out;

import org.springframework.stereotype.Component;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;
import be.kdg.banditgamesbackend.achievement.domain.UserAchievementId;

@Component
public class UserAchievementJpaMapper {

    public UserAchievementJpaEntity toEntity(UserAchievement domain) {
        UserAchievementJpaEntity entity = new UserAchievementJpaEntity();
        entity.setUserAchievementId(domain.getUserAchievementId().id());
        entity.setUserId(domain.getUserId());
        entity.setGameId(domain.getGameId());
        entity.setAchievementCode(domain.getAchievementCode());
        entity.setMatchId(domain.getMatchId());
        entity.setUnlockedAt(domain.getUnlockedAt());
        return entity;
    }
    
    public UserAchievement toDomain(UserAchievementJpaEntity entity) {
        return UserAchievement.hydrate(
            new UserAchievementId(entity.getUserAchievementId()),
            entity.getUserId(),
            entity.getGameId(),
            entity.getMatchId(),
            entity.getAchievementCode(),
            entity.getUnlockedAt()
        );
    }

}
