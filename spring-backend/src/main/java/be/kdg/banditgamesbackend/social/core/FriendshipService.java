package be.kdg.banditgamesbackend.social.core;

import be.kdg.banditgamesbackend.notification.api.NotificationService;
import be.kdg.banditgamesbackend.social.adapter.in.dto.FriendDto;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.FriendshipMapper;
import be.kdg.banditgamesbackend.social.domain.Friendship;
import be.kdg.banditgamesbackend.social.port.in.*;
import be.kdg.banditgamesbackend.social.port.out.FriendshipRepository;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendshipService implements
        SendFriendRequestUseCase,
        AcceptFriendRequestUseCase,
        DeclineFriendRequestUseCase,
        GetFriendsQuery
{

    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapper mapper;
    private final FriendshipValidator validator;
    private final UserLookupService userLookupService;
    private final NotificationService notificationService;

    @Override
    public void sendFriendRequest(SendFriendRequestCommand command) {
        log.info("Sending friend request from {} to {}", command.fromUserId(), command.toUserId());

        validator.validateSendFriendRequest(command);

        Friendship friendship = Friendship.sendRequest(
                command.fromUserId(),
                command.toUserId()
        );

        friendshipRepository.save(friendship);

        try {
            String senderUsername = userLookupService.getUsernameById(command.fromUserId());
            notificationService.sendFriendRequestNotification(
                    command.toUserId(),
                    command.fromUserId(),
                    senderUsername
            );
        } catch (Exception e) {
            log.error("Failed to send friend request notification", e);
        }
    }

    @Override
    public void acceptFriendRequest(AcceptFriendRequestCommand command) {
        log.info("Accepting friend request: {}", command.friendshipId());

        Friendship friendship = findFriendshipById(command.friendshipId());
        validator.validateFriendshipAuthorization(friendship, command.userId(), "accept");

        try {
            String accepterUsername = userLookupService.getUsernameById(command.userId());
            notificationService.sendFriendRequestNotification(
                    friendship.getUserId(),
                    command.userId(),
                    accepterUsername
            );
        } catch (Exception e) {
            log.error("Failed to send friend request accepted notification", e);
        }

        friendship.accept();
        friendshipRepository.save(friendship);
    }

    @Override
    public void declineFriendRequest(DeclineFriendRequestCommand command) {
        log.info("Declining friend request: {}", command.friendshipId());

        Friendship friendship = findFriendshipById(command.friendshipId());
        validator.validateFriendshipAuthorization(friendship, command.userId(), "decline");

        friendship.decline();
        friendshipRepository.save(friendship);

        try {
            String declinerUsername = userLookupService.getUsernameById(command.userId());
            notificationService.sendFriendRequestDeclinedNotification(
                    friendship.getUserId(),
                    command.userId(),
                    declinerUsername
            );
        } catch (Exception e) {
            log.error("Failed to send friend request declined notification", e);
        }
    }

    @Override
    public List<FriendDto> getFriends(UUID userId) {
        log.debug("Getting friends for user: {}", userId);

        return friendshipRepository.findFriendsByUserId(userId)
                .stream()
                .map(friendship -> mapper.toFriendDto(friendship, userId))
                .toList();
    }

    @Override
    public List<FriendDto> getPendingRequests(UUID userId) {
        log.debug("Getting pending requests for user: {}", userId);

        return friendshipRepository.findPendingRequestsByUserId(userId)
                .stream()
                .map(mapper::toPendingRequestDto)
                .toList();
    }

    @Override
    public List<FriendDto> getSentRequests(UUID userId) {
        log.debug("Getting sent requests for user: {}", userId);

        return friendshipRepository.findSentRequestsByUserId(userId)
                .stream()
                .map(mapper::toSentRequestDto)
                .toList();
    }

    private Friendship findFriendshipById(UUID friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Friendship not found: %s", friendshipId)
                ));
    }
}
