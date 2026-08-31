package be.kdg.banditgamesbackend.notification.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.GuestPlayersDetectedEvent;
import be.kdg.banditgamesbackend.notification.api.CreateNotificationRequest;
import be.kdg.banditgamesbackend.notification.api.NotificationService;
import be.kdg.banditgamesbackend.notification.domain.NotificationPriority;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestNotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleGuestPlayersDetected(GuestPlayersDetectedEvent event) {
        log.info("Notifying {} guest player(s) for match: {}",
                event.guestPlayerIds().size(), event.matchId());

        for (UUID guestId : event.guestPlayerIds()) {
            notifyGuestUser(guestId);
        }
    }

    private void notifyGuestUser(UUID guestUserId) {
        notificationService.sendNotification(new CreateNotificationRequest(
                guestUserId,
                NotificationType.GUEST_WELCOME,
                "Playing as Guest",
                "Register to save your progress, unlock achievements, and add friends!",
                NotificationPriority.NORMAL,
                Map.of(
                        "action", "register",
                        "actionUrl", "/register?userId=" + guestUserId,
                        "dismissible", "true"
                )
        ));
    }
}
