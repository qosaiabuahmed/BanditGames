package be.kdg.banditgamesbackend.social.adapter.in;

import be.kdg.banditgamesbackend.social.api.FriendshipLookupService;
import be.kdg.banditgamesbackend.social.port.out.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipLookupServiceAdapter implements FriendshipLookupService {

    private final FriendshipRepository friendshipRepository;

    @Override
    public boolean areFriends(UUID userId1, UUID userId2) {
        return friendshipRepository.areFriends(userId1, userId2);
    }
}