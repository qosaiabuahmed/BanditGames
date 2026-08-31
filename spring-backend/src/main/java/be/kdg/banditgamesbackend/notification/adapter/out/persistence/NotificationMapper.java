package be.kdg.banditgamesbackend.notification.adapter.out.persistence;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationId;

public class NotificationMapper {

    public static NotificationJpaEntity toJpaEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setNotificationId(notification.getNotificationId().id());
        entity.setRecipientId(notification.getRecipientId());
        entity.setType(notification.getNotificationType());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setStatus(notification.getStatus());
        entity.setPriority(notification.getPriority());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setReadAt(notification.getReadAt());
        entity.setMetadata(notification.getMetadata());
        return entity;
    }

    public static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.hydrate(
                new NotificationId(entity.getNotificationId()),
                entity.getRecipientId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getCreatedAt(),
                entity.getReadAt(),
                entity.getMetadata()
        );
    }
}
