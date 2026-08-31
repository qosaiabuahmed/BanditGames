package be.kdg.banditgamesbackend.match.domain;

import java.util.Objects;
import java.util.UUID;

public record MatchPlayer(UUID userId, int seatNumber) {
    public MatchPlayer {
        Objects.requireNonNull(userId, "userId is required");
        if (seatNumber < 1) {
            throw new IllegalArgumentException("seatNumber must be at least 1");
        }
    }
}
