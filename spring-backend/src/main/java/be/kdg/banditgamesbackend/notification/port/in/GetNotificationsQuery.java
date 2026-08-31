package be.kdg.banditgamesbackend.notification.port.in;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;

import java.util.List;
import java.util.UUID;

public interface GetNotificationsQuery {
    List<Notification> getNotifications(UUID userId);
    List<Notification> getNotificationsByStatus(UUID userId, NotificationStatus status);
    long getUnreadCount(UUID userId);
}
