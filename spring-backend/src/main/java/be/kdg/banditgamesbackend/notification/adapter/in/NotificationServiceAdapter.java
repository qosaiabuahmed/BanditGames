package be.kdg.banditgamesbackend.notification.adapter.in;

import be.kdg.banditgamesbackend.notification.api.CreateNotificationRequest;
import be.kdg.banditgamesbackend.notification.api.NotificationService;
import be.kdg.banditgamesbackend.notification.domain.NotificationPriority;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;
import be.kdg.banditgamesbackend.notification.port.in.CreateNotificationCommand;
import be.kdg.banditgamesbackend.notification.port.in.CreateNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceAdapter implements NotificationService {
    private static final String ACTION = "action";

    private final CreateNotificationUseCase createNotificationUseCase;

    @Override
    public void sendNotification(CreateNotificationRequest request) {
        NotificationType type = parseNotificationType(String.valueOf(request.type()));
        NotificationPriority priority = parseNotificationPriority(String.valueOf(request.priority()));

        CreateNotificationCommand command = new CreateNotificationCommand(
                request.recipientId(),
                type,
                request.title(),
                request.message(),
                priority,
                request.metadata()
        );

        createNotificationUseCase.createNotification(command);
    }

    @Override
    public void sendFriendRequestNotification(UUID recipientId, UUID senderId, String senderUsername) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("senderId", senderId.toString());
        metadata.put("senderUsername", senderUsername);
        metadata.put(ACTION, "view_friend_requests");

        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                "New Friend Request",
                senderUsername + " sent you a friend request",
                NotificationPriority.NORMAL,
                metadata
        );

        createNotificationUseCase.createNotification(command);
    }

    @Override
    public void sendFriendRequestAcceptedNotification(UUID recipientId, UUID friendId, String friendUsername) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("friendId", friendId.toString());
        metadata.put("friendUsername", friendUsername);
        metadata.put(ACTION, "view_friends");

        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId,
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                "Friend Request Accepted",
                friendUsername + " accepted your friend request",
                NotificationPriority.NORMAL,
                metadata
        );

        createNotificationUseCase.createNotification(command);
    }

    @Override
    public void sendFriendRequestDeclinedNotification(UUID recipientId, UUID friendId, String friendUsername) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("friendId", friendId.toString());
        metadata.put("friendUsername", friendUsername);

        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId,
                NotificationType.FRIEND_REQUEST_DECLINED,
                "Friend Request Declined",
                friendUsername + " declined your friend request",
                NotificationPriority.LOW,
                metadata
        );

        createNotificationUseCase.createNotification(command);
    }

    @Override
    public void sendGameInvitationNotification(UUID recipientId, UUID inviterId, String inviterUsername, String gameName, UUID lobbyId) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("inviterId", inviterId.toString());
        metadata.put("inviterUsername", inviterUsername);
        metadata.put("gameName", gameName);
        metadata.put("lobbyId", lobbyId.toString());
        metadata.put(ACTION, "join_lobby");
        metadata.put("actionUrl", "/lobby/" + lobbyId);

        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId,
                NotificationType.GAME_INVITATION_RECEIVED,
                "Game Invitation Received",
                inviterUsername + " invited you to play " + gameName,
                NotificationPriority.HIGH,
                metadata
        );

        createNotificationUseCase.createNotification(command);
    }

    @Override
    public void sendMatchStartedNotification(UUID recipientId, String gameName, UUID matchId) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("gameName", gameName);
        metadata.put("matchId", matchId.toString());
        metadata.put(ACTION, "join_match");
        metadata.put("actionUrl", "/match/" + matchId);

        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId,
                NotificationType.MATCH_STARTED,
                "Match Started",
                "Your " + gameName + " match is ready!",
                NotificationPriority.HIGH,
                metadata
        );

        createNotificationUseCase.createNotification(command);
    }

    @Override
    public void sendAchievementUnlockedNotification(UUID recipientId, String achievementName, String achievementDescription, String gameId) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("achievementName", achievementName);
        metadata.put("achievementDescription", achievementDescription);
        metadata.put("gameId", gameId);
        metadata.put(ACTION, "view_achievements");

        CreateNotificationCommand command = new CreateNotificationCommand(
                recipientId,
                NotificationType.ACHIEVEMENT_UNLOCKED,
                "Achievement Unlocked",
                "You unlocked: " + achievementName,
                NotificationPriority.LOW,
                metadata
        );

        createNotificationUseCase.createNotification(command);
    }

    private NotificationType parseNotificationType(String type) {
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }
    }

    private NotificationPriority parseNotificationPriority(String priority) {
        if (priority == null ||  priority.isBlank()) {
            return NotificationPriority.NORMAL;
        }

        try {
            return NotificationPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotificationPriority.NORMAL;
        }
    }
}
