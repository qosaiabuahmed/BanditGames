package be.kdg.banditgamesbackend.common.events;

import java.time.Instant;
import java.util.UUID;

public record LobbyInviteDeclinedEvent(
        UUID inviteId,
        UUID lobbyId,
        UUID fromUserId,
        UUID toUserId,
        Instant occurredAt
) {}