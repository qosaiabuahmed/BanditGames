package be.kdg.banditgamesbackend.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MoveMadeEvent.class, name = "MOVE_MADE"),
        @JsonSubTypes.Type(value = AchievementUnlockedEvent.class, name = "ACHIEVEMENT_UNLOCKED"),
        @JsonSubTypes.Type(value = PlayerJoinedEvent.class, name = "PLAYER_JOINED"),
        @JsonSubTypes.Type(value = PlayerLeftEvent.class, name = "PLAYER_LEFT"),
        @JsonSubTypes.Type(value = MatchCreatedEvent.class, name = "MATCH_CREATED"),
        @JsonSubTypes.Type(value = MatchStartedEvent.class, name = "MATCH_STARTED"),
        @JsonSubTypes.Type(value = MatchEndedEvent.class, name = "MATCH_COMPLETED")
}) // Add each new event here as a type
public interface PlatformEvent {
    String getEventType();
    LocalDateTime getTimestamp();
}
