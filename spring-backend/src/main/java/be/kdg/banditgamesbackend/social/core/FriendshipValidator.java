package be.kdg.banditgamesbackend.social.core;

import be.kdg.banditgamesbackend.social.domain.Friendship;
import be.kdg.banditgamesbackend.social.port.in.SendFriendRequestCommand;
import be.kdg.banditgamesbackend.social.port.out.FriendshipRepository;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FriendshipValidator {

    private final UserLookupService userLookupService;
    private final FriendshipRepository friendshipRepository;

    public void validateSendFriendRequest(SendFriendRequestCommand command) {
        validateUserExists(command.fromUserId(), "Sender");
        validateUserExists(command.toUserId(), "Receiver");
        validateNotSelfFriendRequest(command.fromUserId(), command.toUserId());
        validateNoExistingFriendship(command.fromUserId(), command.toUserId());
    }

    public void validateFriendshipAuthorization(Friendship friendship, UUID userId, String action) {
        if (!friendship.getFriendId().equals(userId)) {
            throw new IllegalStateException(
                    String.format("You can only %s friend requests sent to you", action)
            );
        }
    }

    private void validateUserExists(UUID userId, String userType) {
        if (!userLookupService.userExists(userId)) {
            throw new IllegalArgumentException(
                    String.format("%s user not found: %s", userType, userId)
            );
        }
    }

    private void validateNotSelfFriendRequest(UUID fromUserId, UUID toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }
    }

    private void validateNoExistingFriendship(UUID fromUserId, UUID toUserId) {
        if (friendshipRepository.areFriends(fromUserId, toUserId)) {
            throw new IllegalStateException(
                    String.format("Friend request already exists with user: %s", toUserId)
            );
        }

        if (friendshipRepository.areFriends(toUserId, fromUserId)) {
            throw new IllegalStateException(
                    "This user has already sent you a friend request"
            );
        }
    }
}
