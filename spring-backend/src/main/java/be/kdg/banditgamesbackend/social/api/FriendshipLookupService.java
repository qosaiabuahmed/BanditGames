package be.kdg.banditgamesbackend.social.api;

import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface(name = "api")
public interface FriendshipLookupService {
    boolean areFriends(UUID userId1, UUID userId2);
}
