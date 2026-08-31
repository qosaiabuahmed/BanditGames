package be.kdg.banditgamesbackend.acl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalGameRegisteredMessage extends ExternalGameEvent{

    @JsonProperty("registrationId")
    private String registrationId;

    @JsonProperty("frontendUrl")
    private String frontendUrl;

    @JsonProperty("pictureUrl")
    private String pictureUrl;

    @JsonProperty("availableAchievements")
    private List<AchievementDto> availableAchievements;

    @JsonProperty("timestamp")
    private String timestamp;

    @Data
    public static class AchievementDto {
        private String code;
        private String description;
    }
}
