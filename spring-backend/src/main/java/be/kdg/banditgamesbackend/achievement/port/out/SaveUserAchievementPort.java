package be.kdg.banditgamesbackend.achievement.port.out;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;

public interface SaveUserAchievementPort {
    void save(UserAchievement userAchievement);
}
