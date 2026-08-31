package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchStateUpdatedEvent implements PlatformEvent {
    private String eventType = "GAME_UPDATED";
    private LocalDateTime timestamp = LocalDateTime.now();


    private UUID matchId;
    private UUID gameId;
    private String gameState;
    private String status;
    private LocalDateTime updatedAt;

    public MatchStateUpdatedEvent(UUID matchId, UUID gameId, String gameState, String status, LocalDateTime updatedAt) {
        this.matchId = matchId;
        this.gameId = gameId;
        this.gameState = gameState;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
