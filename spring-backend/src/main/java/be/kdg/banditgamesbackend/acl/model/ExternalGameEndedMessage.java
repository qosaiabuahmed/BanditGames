package be.kdg.banditgamesbackend.acl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalGameEndedMessage extends ExternalGameEvent {
    private String gameId;
    private String whitePlayerName;
    private String blackPlayerName;
    private String finalFen;
    private String endReason;
    private String winner;
    private Integer totalMoves;
}
