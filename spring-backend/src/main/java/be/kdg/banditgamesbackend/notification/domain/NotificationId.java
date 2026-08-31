package be.kdg.banditgamesbackend.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID id) {
    public NotificationId {
        Objects.requireNonNull(id, "Notification ID cannot be null");
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }
}
