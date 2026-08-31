package be.kdg.banditgamesbackend.notification.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Notification {
    private final NotificationId notificationId;
    private final UUID recipientId;
    private final NotificationType notificationType;
    private final String title;
    private final String message;
    private NotificationStatus status;
    private final NotificationPriority priority;
    private final LocalDateTime createdAt;
    private LocalDateTime readAt;
    private final Map<String, String> metadata;

    private Notification(NotificationId notificationId,
                        UUID recipientId,
                        NotificationType notificationType,
                        String title,
                        String message,
                        NotificationStatus status,
                        NotificationPriority priority,
                        LocalDateTime createdAt,
                        LocalDateTime readAt,
                        Map<String, String> metadata) {
        this.notificationId = Objects.requireNonNull(notificationId);
        this.recipientId = Objects.requireNonNull(recipientId);
        this.notificationType = Objects.requireNonNull(notificationType);
        this.title = Objects.requireNonNull(title);
        this.message = Objects.requireNonNull(message);
        this.status = Objects.requireNonNull(status);
        this.priority = Objects.requireNonNull(priority);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.readAt = readAt;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public static Notification create(UUID recipientId,
                                      NotificationType type,
                                      String title,
                                      String message,
                                      NotificationPriority priority,
                                      Map<String, String> metadata) {
        validateTitle(title);
        validateMessage(message);

        return new Notification(
                NotificationId.generate(),
                recipientId,
                type,
                title,
                message,
                NotificationStatus.UNREAD,
                priority,
                LocalDateTime.now(),
                null,
                metadata
        );
    }

    public static Notification hydrate(NotificationId notificationId,
                                       UUID recipientId,
                                       NotificationType type,
                                       String title,
                                       String message,
                                       NotificationStatus status,
                                       NotificationPriority priority,
                                       LocalDateTime createdAt,
                                       LocalDateTime readAt,
                                       Map<String, String> metadata) {
        return new Notification(
                notificationId,
                recipientId,
                type,
                title,
                message,
                status,
                priority,
                createdAt,
                readAt,
                metadata
        );
    }

    public void markAsRead() {
        if (this.status == NotificationStatus.DELETED) {
            throw new IllegalStateException("Cannot mark deleted notification as read");
        }
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        if (this.status == NotificationStatus.DELETED) {
            throw new IllegalStateException("Cannot mark deleted notification as unread");
        }
        this.status = NotificationStatus.UNREAD;
        this.readAt = null;
    }

    public void delete() {
        this.status = NotificationStatus.DELETED;
    }

    public boolean isUnread() {
        return this.status == NotificationStatus.UNREAD;
    }

    public boolean isDeleted() {
        return this.status == NotificationStatus.DELETED;
    }

    private static void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message cannot be empty");
        }
        if (message.length() > 500) {
            throw new IllegalArgumentException("Notification message cannot be longer than 500 characters");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Notification title cannot be empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Notification title cannot be longer than 100 characters");
        }
    }
}
