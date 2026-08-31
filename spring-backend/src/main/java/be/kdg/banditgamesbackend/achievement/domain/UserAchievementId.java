package be.kdg.banditgamesbackend.achievement.domain;

import java.util.UUID;

public record UserAchievementId(UUID id) {
    public UserAchievementId {
        if (id == null) {
            throw new IllegalArgumentException("UserAchievementId cannot be null");
        }
    }

    public static UserAchievementId generate() {
        return new UserAchievementId(UUID.randomUUID());
    }
} 
