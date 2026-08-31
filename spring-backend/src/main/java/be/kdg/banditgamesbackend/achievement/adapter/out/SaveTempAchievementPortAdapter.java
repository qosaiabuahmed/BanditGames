package be.kdg.banditgamesbackend.achievement.adapter.out;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;
import be.kdg.banditgamesbackend.achievement.port.out.SaveTempUserAchievementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveTempAchievementPortAdapter implements SaveTempUserAchievementPort {

    private final TempAchievementJpaRepository repository;

    @Override
    public void save(TempAchievement tempUserAchievement) {
        TempAchievementJpaEntity entity = new TempAchievementJpaEntity();
        entity.setGuestUserId(tempUserAchievement.getGuestUserId());
        entity.setGameId(tempUserAchievement.getGameId());
        entity.setAchievementCode(tempUserAchievement.getAchievementCode());
        entity.setMatchId(tempUserAchievement.getMatchId());
        entity.setUnlockedAt(tempUserAchievement.getUnlockedAt());

        repository.save(entity);
    }
}
