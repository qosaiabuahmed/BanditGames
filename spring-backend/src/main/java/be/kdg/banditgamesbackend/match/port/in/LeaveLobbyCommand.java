package be.kdg.banditgamesbackend.match.port.in;

import java.util.Objects;
import java.util.UUID;

public record LeaveLobbyCommand(UUID lobbyId, UUID userId) {
    public LeaveLobbyCommand {
        Objects.requireNonNull(lobbyId, "lobbyId is required");
        Objects.requireNonNull(userId, "userId is required");
    }
}
