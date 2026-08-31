package be.kdg.banditgamesbackend.notification.port.in;

import java.util.UUID;

public record MarkNotificationAsReadCommand(
        UUID notificationId,
        UUID userId
) {
    public MarkNotificationAsReadCommand {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
}
