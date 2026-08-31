package be.kdg.banditgamesbackend.common.events;

import java.util.UUID;

public record GameFavoritedEvent(UUID userId, UUID gameId) {

}
