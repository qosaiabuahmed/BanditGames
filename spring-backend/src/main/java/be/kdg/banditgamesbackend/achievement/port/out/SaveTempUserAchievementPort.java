package be.kdg.banditgamesbackend.achievement.port.out;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;

public interface SaveTempUserAchievementPort {
    void save(TempAchievement tempUserAchievement);
}
