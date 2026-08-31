package be.kdg.banditgamesbackend.common.events;

import java.util.UUID;

public record GameUnfavoritedEvent(UUID userId, UUID gameId) {
}
