package be.kdg.banditgamesbackend.acl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalGameUpdatedMessage extends ExternalGameEvent {
    private String gameId;
    private String whitePlayer;
    private String blackPlayer;
    private String currentFen;
    private String status;
    private String updateType;
}

