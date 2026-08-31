package be.kdg.banditgamesbackend.notification.adapter.out.websocket;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationPriority;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationWebSocketDto(
        UUID notificationId,
        NotificationType type,
        String title,
        String message,
        NotificationPriority priority,
        LocalDateTime createdAt,
        Map<String, String> metadata
) {
    public static NotificationWebSocketDto from(Notification notification) {
        return new NotificationWebSocketDto(
                notification.getNotificationId().id(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPriority(),
                notification.getCreatedAt(),
                notification.getMetadata()
        );
    }
}
