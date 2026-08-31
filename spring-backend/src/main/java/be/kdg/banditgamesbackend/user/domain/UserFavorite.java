package be.kdg.banditgamesbackend.user.domain;

import java.sql.Timestamp;
import java.util.UUID;

public record UserFavorite(
        UUID userId,
        UUID gameId,
        Timestamp favoritedAt
) {
}
