package be.kdg.banditgamesbackend.match.port.in;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateMatchCommand(UUID gameId, List<UUID> playerIds) {
    public CreateMatchCommand {
        Objects.requireNonNull(gameId, "gameId is required");
        Objects.requireNonNull(playerIds, "playerIds is required");
    }
}
