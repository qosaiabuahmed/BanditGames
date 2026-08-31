package be.kdg.banditgamesbackend.gamemetadata.domain;

import java.util.Set;

public record PlayerConfiguration(
        int minPlayers,
        int maxPlayers,
        Set<PlayerType> supportedPlayerTypes
) {
    public PlayerConfiguration {
        if (minPlayers < 1) {
            throw new IllegalArgumentException("minPlayers must be at least 1");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException("maxPlayers must be greater than or equal to minPlayers");
        }
        if (supportedPlayerTypes == null || supportedPlayerTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one supported player type is required");
        }
    }

    public enum PlayerType {
        HUMAN,
        AI
    }
}
