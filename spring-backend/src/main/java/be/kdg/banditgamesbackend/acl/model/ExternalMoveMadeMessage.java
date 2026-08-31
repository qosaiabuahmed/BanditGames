package be.kdg.banditgamesbackend.acl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalMoveMadeMessage extends ExternalGameEvent {
    private String gameId;

    private String fromSquare;
    private String toSquare;
    private String sanNotation;
    private String fenAfterMove;

    private String player;
    private Integer moveNumber;

    private UUID whitePlayerId;
    private String whitePlayerName;

    private UUID blackPlayerId;
    private String blackPlayerName;

    private String moveTime;
    
}
