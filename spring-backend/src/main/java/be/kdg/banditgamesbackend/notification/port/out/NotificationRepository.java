package be.kdg.banditgamesbackend.notification.port.out;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationId;
import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);
    Optional<Notification> findById(NotificationId notificationId);
    List<Notification> findByRecipientId(UUID recipientId);
    List<Notification> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status);
    List<Notification> findByRecipientIdAndType(UUID recipientId, NotificationType type);
    long countUnreadByRecipientId(UUID recipientId);
    void markAllAsReadByRecipientId(UUID recipientId);
    void deleteOlderThan(LocalDateTime cutoffDate);
    void deleteById(NotificationId notificationId);
}
