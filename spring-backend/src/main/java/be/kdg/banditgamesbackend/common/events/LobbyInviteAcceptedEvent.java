package be.kdg.banditgamesbackend.common.events;

import java.time.Instant;
import java.util.UUID;

public record LobbyInviteAcceptedEvent(
        UUID inviteId,
        UUID lobbyId,
        String gameName,
        UUID fromUserId,
        UUID toUserId,
        Instant occurredAt
) {}