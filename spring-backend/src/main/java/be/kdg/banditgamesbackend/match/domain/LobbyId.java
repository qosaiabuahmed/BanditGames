package be.kdg.banditgamesbackend.match.domain;

import java.util.Objects;
import java.util.UUID;

public record LobbyId(UUID value) {
    public LobbyId {
        Objects.requireNonNull(value, "value is required");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
