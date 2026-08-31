package be.kdg.banditgamesbackend.notification.adapter.out.websocket;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.port.out.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationPublisher implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationPublisher.class.getName());

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishToUser(Notification notification) {
        String destination = "/queue/notifications";
        String userId = notification.getRecipientId().toString();

        NotificationWebSocketDto dto = NotificationWebSocketDto.from(notification);

        try {
            messagingTemplate.convertAndSendToUser(userId, destination, dto);
            log.debug("Sent notification {} to user {} via websocket", dto, userId);
        } catch (Exception ex) {
            log.error("Failed to send notification {} to user {} via websocket", notification.getNotificationId(), userId, ex);
            throw ex;
        }
    }

    @Override
    public void publishToAll(Notification notification) {
        String destination = "/topic/notifications";
        NotificationWebSocketDto dto = NotificationWebSocketDto.from(notification);

        try {
            messagingTemplate.convertAndSend(destination, dto);
            log.debug("Broadcast notification {} to all users", notification.getNotificationId());
        } catch (Exception ex) {
            log.error("Failed to broadcast notification {}", notification.getNotificationId(), ex);
            throw ex;
        }
    }
}
