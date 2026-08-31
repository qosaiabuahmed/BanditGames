package be.kdg.banditgamesbackend.gamemetadata.adapter.in.response;

import be.kdg.banditgamesbackend.gamemetadata.domain.PlayerConfiguration;

import java.util.Set;

public record PlayerConfigurationDto(
        int minPlayers,
        int maxPlayers,
        Set<PlayerConfiguration.PlayerType> supportedPlayerTypes
) {
}
