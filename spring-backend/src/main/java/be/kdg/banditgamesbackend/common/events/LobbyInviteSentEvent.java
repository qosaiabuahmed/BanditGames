package be.kdg.banditgamesbackend.common.events;

import java.time.Instant;
import java.util.UUID;

public record LobbyInviteSentEvent(
        UUID inviteId,
        UUID lobbyId,
        UUID gameId,
        String gameName,
        UUID fromUserId,
        UUID toUserId,
        Instant occurredAt
) {}