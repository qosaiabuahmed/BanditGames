package be.kdg.banditgamesbackend.notification.port.in;

import be.kdg.banditgamesbackend.notification.domain.NotificationPriority;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;

import java.util.Map;
import java.util.UUID;

public record CreateNotificationCommand(
        UUID recipientId,
        NotificationType type,
        String title,
        String message,
        NotificationPriority priority,
        Map<String,String> metadata
) {
    public CreateNotificationCommand {
        if (recipientId == null) {
            throw new IllegalArgumentException("Recipient ID is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (priority == null) {
            priority = NotificationPriority.NORMAL;
        }
    }
}
