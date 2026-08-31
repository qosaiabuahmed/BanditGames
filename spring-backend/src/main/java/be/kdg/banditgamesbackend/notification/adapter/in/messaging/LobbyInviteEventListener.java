package be.kdg.banditgamesbackend.notification.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.LobbyInviteAcceptedEvent;
import be.kdg.banditgamesbackend.common.events.LobbyInviteSentEvent;
import be.kdg.banditgamesbackend.notification.api.NotificationService;
import be.kdg.banditgamesbackend.notification.domain.NotificationPriority;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener in Notification context that listens to lobby invite events from Match context.
 * Uses only public APIs (@NamedInterface) from other contexts - NO direct repository access.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LobbyInviteEventListener {

    private final NotificationService notificationService;
    private final UserLookupService userLookupService;

    @EventListener
    @Transactional
    public void handleLobbyInviteSent(LobbyInviteSentEvent event) {
        log.info("Handling LobbyInviteSentEvent: inviteId={}, lobbyId={}, from={}, to={}",
                event.inviteId(), event.lobbyId(),
                event.fromUserId(), event.toUserId());

        try {
            // Fetch inviter username via public API
            String inviterUsername = userLookupService.getUsernameById(event.fromUserId());

            // Send notification
            notificationService.sendGameInvitationNotification(
                    event.toUserId(),
                    event.fromUserId(),
                    inviterUsername,
                    event.gameName(),
                    event.lobbyId()
            );

            log.info("Notification sent to user {} for lobby invite from {}",
                    event.toUserId(), inviterUsername);

        } catch (Exception e) {
            log.error("Failed to send lobby invite notification", e);
        }
    }

    @EventListener
    @Transactional
    public void handleLobbyInviteAccepted(LobbyInviteAcceptedEvent event) {
        log.info("Handling LobbyInviteAcceptedEvent: inviteId={}, acceptedBy={}",
                event.inviteId(), event.toUserId());

        try {
            // Fetch accepter username via public API
            String accepterUsername = userLookupService.getUsernameById(event.toUserId());

            // Send notification to the inviter
            notificationService.sendNotification(
                    new be.kdg.banditgamesbackend.notification.api.CreateNotificationRequest(
                            event.fromUserId(),
                            NotificationType.GAME_INVITATION_ACCEPTED,
                            "Invitation Accepted",
                            accepterUsername + " accepted your invitation to play " + event.gameName(),
                            NotificationPriority.NORMAL,
                            java.util.Map.of(
                                    "accepterId", event.toUserId().toString(),
                                    "accepterUsername", accepterUsername,
                                    "gameName", event.gameName(),
                                    "lobbyId", event.lobbyId().toString(),
                                    "action", "view_lobby",
                                    "actionUrl", "/lobby/" + event.lobbyId()
                            )
                    )
            );

            log.info("Notification sent to inviter {} that {} accepted their invite",
                    event.fromUserId(), accepterUsername);

        } catch (Exception e) {
            log.error("Failed to send lobby invite accepted notification", e);
        }
    }
}