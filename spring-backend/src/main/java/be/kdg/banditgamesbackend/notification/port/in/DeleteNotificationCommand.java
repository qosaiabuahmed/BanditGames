package be.kdg.banditgamesbackend.notification.port.in;

import java.util.UUID;

public record DeleteNotificationCommand(
        UUID notificationId,
        UUID userId
) {
    public DeleteNotificationCommand {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
}
