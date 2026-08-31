package be.kdg.banditgamesbackend.match.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection of Friendship from Social context into Match context.
 * Updated via events - no direct access to Social context repository.
 */
@Getter
public class FriendshipProjection {

    private final UUID userId1;
    private final UUID userId2;
    private final LocalDateTime createdAt;

    public FriendshipProjection(UUID userId1, UUID userId2, LocalDateTime createdAt) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.createdAt = createdAt;
    }

    public boolean involves(UUID userId) {
        return userId1.equals(userId) || userId2.equals(userId);
    }

    public boolean isFriendshipBetween(UUID user1, UUID user2) {
        return (userId1.equals(user1) && userId2.equals(user2)) ||
               (userId1.equals(user2) && userId2.equals(user1));
    }
}