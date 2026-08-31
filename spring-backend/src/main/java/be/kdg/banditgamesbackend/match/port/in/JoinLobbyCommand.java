package be.kdg.banditgamesbackend.match.port.in;

import java.util.Objects;
import java.util.UUID;

public record JoinLobbyCommand(UUID gameId, UUID userId) {
    public JoinLobbyCommand {
        Objects.requireNonNull(gameId, "gameId is required");
        Objects.requireNonNull(userId, "userId is required");
    }
}
