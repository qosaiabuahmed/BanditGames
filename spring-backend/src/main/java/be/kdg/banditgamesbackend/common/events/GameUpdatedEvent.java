package be.kdg.banditgamesbackend.common.events;

import java.util.UUID;

public record GameUpdatedEvent(UUID gameId, String name) {
}
