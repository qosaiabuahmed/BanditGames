package be.kdg.banditgamesbackend.user.port.in;

import java.util.UUID;

public record MarkFavoriteGameCommand(UUID userId, UUID gameId) {
}
