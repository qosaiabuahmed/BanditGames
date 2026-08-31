package be.kdg.banditgamesbackend.gamemetadata.domain;

import java.util.Objects;
import java.util.UUID;

public record GameId(UUID gameId) {
    public GameId {
        Objects.requireNonNull(gameId, "Game ID cannot be null");
    }
}
