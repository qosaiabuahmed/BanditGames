package be.kdg.banditgamesbackend.gamemetadata.domain;

public record AchievementId(String achievementId) {
    public AchievementId {
        if (achievementId == null || achievementId.isBlank()) {
            throw new IllegalArgumentException("Achievement ID cannot be null or blank");
        }

    }
}
