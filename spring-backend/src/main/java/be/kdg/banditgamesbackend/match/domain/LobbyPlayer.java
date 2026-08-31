package be.kdg.banditgamesbackend.match.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LobbyPlayer(UUID userId, Instant joinedAt) {
    public LobbyPlayer {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(joinedAt, "joinedAt is required");
    }
}
