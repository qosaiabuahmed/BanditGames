package be.kdg.banditgamesbackend.gamemetadata.domain;

import java.util.Objects;

public record AchievementDefinition(
        AchievementId achievementId,
        String name,
        String description,
        String iconUrl
) {
    public AchievementDefinition {
        Objects.requireNonNull(achievementId, "achievementId is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(description, "description is required");
    }
}
