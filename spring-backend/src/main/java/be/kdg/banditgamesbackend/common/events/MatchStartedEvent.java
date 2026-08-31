package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchStartedEvent implements PlatformEvent {
    private String eventType = "MATCH_STARTED";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID matchId;
    private UUID gameId;
    private List<UUID> playerIds;
    private List<String> playerNames;
    private String initialGameState;
    private String status;
    private LocalDateTime startTime;
    Map<String, Object> arePlayersGuests;

    public MatchStartedEvent(
            UUID matchId,
            UUID gameId,
            List<UUID> playerIds,
            List<String> playerNames,
            String initialGameState,
            String status,
            LocalDateTime timestamp
    ) {
        this.matchId = matchId;
        this.gameId = gameId;
        this.playerIds = playerIds;
        this.playerNames = playerNames;
        this.initialGameState = initialGameState;
        this.status = status;
        this.timestamp = timestamp;
    }
}
