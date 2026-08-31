package be.kdg.banditgamesbackend.notification.adapter.in.response;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationPriority;
import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


public record NotificationDto(
        UUID notificationId,
        NotificationType type,
        String title,
        String message,
        NotificationStatus status,
        NotificationPriority priority,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        Map<String, String> metadata
)  {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getNotificationId().id(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getPriority(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getMetadata()
        );
    }

}