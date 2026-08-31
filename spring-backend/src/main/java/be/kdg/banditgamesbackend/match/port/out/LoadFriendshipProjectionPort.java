package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.FriendshipProjection;

import java.util.Optional;
import java.util.UUID;

public interface LoadFriendshipProjectionPort {
    boolean areFriends(UUID userId1, UUID userId2);
    Optional<FriendshipProjection> findFriendship(UUID userId1, UUID userId2);
}