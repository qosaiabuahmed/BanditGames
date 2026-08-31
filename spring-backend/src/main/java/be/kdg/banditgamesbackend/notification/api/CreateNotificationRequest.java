package be.kdg.banditgamesbackend.notification.api;

import be.kdg.banditgamesbackend.notification.domain.NotificationType;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CreateNotificationRequest(
        UUID recipientId,
        NotificationType type,
        String title,
        String message,
        be.kdg.banditgamesbackend.notification.domain.NotificationPriority priority,
        Map<String, String> metadata
) {
    public CreateNotificationRequest {
        Objects.requireNonNull(recipientId, "Recipient ID is required");
        Objects.requireNonNull(type, "Type is required");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
    }
}