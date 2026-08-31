package be.kdg.banditgamesbackend.acl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalAchievementMessage extends ExternalGameEvent {
    private String gameId;
    private String playerId;
    private String playerName;
    private String achievementType;

    @JsonProperty("achievementDescription")
    private String description;
}
