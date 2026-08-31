package be.kdg.banditgamesbackend.common.events;

import java.sql.Timestamp;
import java.util.UUID;

public record FriendRequestSentEvent(
        UUID friendshipId,
        UUID fromUserId,
        UUID toUserId,
        Timestamp sentAt
) {}
