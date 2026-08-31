package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCreatedEvent implements PlatformEvent {
    private String eventType = "MATCH_CREATED";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID matchId;
    private UUID gameId;
    private UUID[] playerIds;
}
