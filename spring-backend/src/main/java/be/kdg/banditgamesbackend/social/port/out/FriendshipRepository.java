package be.kdg.banditgamesbackend.social.port.out;

import be.kdg.banditgamesbackend.social.domain.Friendship;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository {
    Friendship save(Friendship friendship);
    Optional<Friendship> findById(UUID friendshipId);
    Optional<Friendship> findByUserAndFriend(UUID userId, UUID friendId);
    List<Friendship> findFriendsByUserId(UUID userId);
    List<Friendship> findPendingRequestsByUserId(UUID userId);
    List<Friendship> findSentRequestsByUserId(UUID userId);
    boolean areFriends(UUID userId, UUID friendId);
    void delete(UUID friendshipId);
}
