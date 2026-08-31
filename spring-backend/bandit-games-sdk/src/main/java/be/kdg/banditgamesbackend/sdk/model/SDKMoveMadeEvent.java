package be.kdg.banditgamesbackend.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SDKMoveMadeEvent {
    private static final String EVENT_TYPE = "MOVE_MADE";
    private final LocalDateTime timestamp = LocalDateTime.now();
    private UUID matchId;
    private UUID gameId;
    private String playerName;
    private Integer moveNumber;
    private String fromSquare;
    private String toSquare;
    private String moveNotation;
    private String boardStateAfter;
    private final LocalDateTime movedAt = LocalDateTime.now();
}
