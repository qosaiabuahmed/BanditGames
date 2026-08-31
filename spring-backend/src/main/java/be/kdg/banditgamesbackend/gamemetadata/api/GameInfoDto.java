package be.kdg.banditgamesbackend.gamemetadata.api;

import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;
import java.util.UUID;

@NamedInterface("api")
public record GameInfoDto(
        UUID gameId,
        String name,
        String description,
        String status,
        String frontendUrl,
        LocalDateTime registeredAt,
        PlayerConfigurationInfo playerConfiguration
) {
}
