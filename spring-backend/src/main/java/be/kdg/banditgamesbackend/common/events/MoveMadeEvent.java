package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveMadeEvent implements PlatformEvent {
    private String eventType = "MOVE_MADE";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID matchId;
    private UUID gameId;
    private UUID playerId;
    private String playerName;
    private Integer moveNumber;
    private String fromSquare;
    private String toSquare;
    private String moveNotation;
    private String boardStateAfter;
    private LocalDateTime movedAt;

    public MoveMadeEvent(UUID matchId, UUID gameId, UUID playerId, String playerName, Integer moveNumber, String fromSquare, String toSquare, String moveNotation, String boardStateAfter, LocalDateTime movedAt) {
        this.matchId = matchId;
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.moveNumber = moveNumber;
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.moveNotation = moveNotation;
        this.boardStateAfter = boardStateAfter;
        this.movedAt = movedAt;
    }
}
