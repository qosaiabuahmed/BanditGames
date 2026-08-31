package be.kdg.banditgamesbackend.acl.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExternalGameCreatedMessage.class, name = "GAME_CREATED"),
        @JsonSubTypes.Type(value = ExternalMoveMadeMessage.class, name = "MOVE_MADE"),
        @JsonSubTypes.Type(value = ExternalGameEndedMessage.class, name = "GAME_ENDED"),
        @JsonSubTypes.Type(value = ExternalAchievementMessage.class, name = "ACHIEVEMENT_ACQUIRED"),
        @JsonSubTypes.Type(value = ExternalGameUpdatedMessage.class, name = "GAME_UPDATED"),
        @JsonSubTypes.Type(value = ExternalGameRegisteredMessage.class, name = "GAME_REGISTERED")
})
public abstract class ExternalGameEvent {
    private MessageType messageType;
    private String timestamp;
}
