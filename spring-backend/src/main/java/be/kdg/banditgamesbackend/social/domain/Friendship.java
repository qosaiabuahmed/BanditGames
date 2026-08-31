package be.kdg.banditgamesbackend.social.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Friendship {
    private final UUID friendshipId;
    private final UUID userId;
    private final UUID friendId;
    private FriendshipStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime acceptedAt;


    public static Friendship sendRequest(UUID fromUserId, UUID toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new IllegalStateException("You can only accept friends requests sent to you.");
        }
        return new Friendship(
                UUID.randomUUID(),
                fromUserId,
                toUserId,
                FriendshipStatus.PENDING,
                LocalDateTime.now(),
                null
        );
    }

    public static Friendship hydrate(UUID friendshipId,
                                     UUID userId,
                                     UUID friendId,
                                     FriendshipStatus status,
                                     LocalDateTime createdAt,
                                     LocalDateTime acceptedAt) {
        return new Friendship(
                friendshipId,
                userId,
                friendId,
                status,
                createdAt,
                acceptedAt
        );
    }

    public void accept() {
        if (status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Can only accept pending friend requests");
        }
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void decline() {
        if (status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Can only decline pending friend requests");
        }
        this.status = FriendshipStatus.DECLINED;
    }

    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }

    public boolean isPending() {
        return status == FriendshipStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }
}

