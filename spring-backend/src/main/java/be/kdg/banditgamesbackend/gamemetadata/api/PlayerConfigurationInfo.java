package be.kdg.banditgamesbackend.gamemetadata.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface("api")
public record PlayerConfigurationInfo(
        int minPlayers,
        int maxPlayers,
        java.util.Set<be.kdg.banditgamesbackend.gamemetadata.domain.PlayerConfiguration.PlayerType> playerTypes) {
}
