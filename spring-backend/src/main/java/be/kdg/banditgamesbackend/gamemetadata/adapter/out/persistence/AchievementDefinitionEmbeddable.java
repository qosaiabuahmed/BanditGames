package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class AchievementDefinitionEmbeddable {
    private String achievementId;
    private String name;
    private String description;
    private String iconUrl;
}
