package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.RenderType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterGameProjectionCommand(
        UUID gameId,
        String name,
        String description,
        be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus status,
        LocalDateTime registeredAt,
        String frontendUrl,
        String pictureUrl,
        RenderType renderType
) {}
