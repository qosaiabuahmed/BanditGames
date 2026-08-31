package be.kdg.banditgamesbackend.common.events;

import java.util.List;
import java.util.UUID;

public record GuestPlayersDetectedEvent(UUID matchId, List<UUID> guestPlayerIds) {
}
