package be.kdg.banditgamesbackend.notification.api;

import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface(name = "api")
public interface NotificationService {

    void sendNotification(CreateNotificationRequest request);
    void sendFriendRequestNotification(UUID recipientId, UUID senderId, String senderUsername);
    void sendFriendRequestAcceptedNotification(UUID recipientId, UUID friendId, String friendUsername);
    void sendFriendRequestDeclinedNotification(UUID recipientId, UUID friendId, String friendUsername);
    void sendGameInvitationNotification(UUID recipientId, UUID inviterId, String inviterUsername, String gameName, UUID lobbyId);
    void sendMatchStartedNotification(UUID recipientId, String gameName, UUID matchId);
    void sendAchievementUnlockedNotification(UUID recipientId, String achievementName, String achievementDescription, String gameId);
}
