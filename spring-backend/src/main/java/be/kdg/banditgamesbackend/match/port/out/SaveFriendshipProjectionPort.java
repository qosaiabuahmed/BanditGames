package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.FriendshipProjection;

import java.util.UUID;

public interface SaveFriendshipProjectionPort {
    void save(FriendshipProjection projection);
    void deleteFriendship(UUID userId1, UUID userId2);
}