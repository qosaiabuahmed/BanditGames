package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchEndedEvent implements PlatformEvent {
    private String eventType = "GAME_ENDED";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID gameId;
    private UUID matchId;
    private String outcome;
    private String endReason;
    private String winner;
    private Map<UUID, Integer> playerScores;
    private String finalGameState;
    private int totalMoves;
    private LocalDateTime endTime;

    public MatchEndedEvent(UUID gameId, UUID matchId, String outcome, String endReason, String winner, Map<UUID, Integer> playerScores, String finalGameState, int totalMoves, LocalDateTime endTime) {
        this.gameId = gameId;
        this.matchId = matchId;
        this.outcome = outcome;
        this.endReason = endReason;
        this.winner = winner;
        this.playerScores = playerScores;
        this.finalGameState = finalGameState;
        this.totalMoves = totalMoves;
        this.endTime = endTime;
    }
}
