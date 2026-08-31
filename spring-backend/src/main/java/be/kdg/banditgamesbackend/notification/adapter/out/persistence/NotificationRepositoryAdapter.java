package be.kdg.banditgamesbackend.notification.adapter.out.persistence;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationId;
import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;
import be.kdg.banditgamesbackend.notification.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity jpaEntity = NotificationMapper.toJpaEntity(notification);
        NotificationJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return NotificationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findById(NotificationId notificationId) {
        return jpaRepository.findById(notificationId.id())
                .map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findByRecipientId(UUID recipientId) {
        return jpaRepository.findByRecipientIdAndStatusNotOrderByCreatedAtDesc(recipientId, NotificationStatus.DELETED)
                .stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status) {
        return jpaRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(recipientId, status)
                .stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByRecipientIdAndType(UUID recipientId, NotificationType type) {
        return jpaRepository.findByRecipientIdAndType(recipientId, type)
                .stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public long countUnreadByRecipientId(UUID recipientId) {
        return jpaRepository.countUnreadByRecipientId(recipientId);
    }

    @Override
    public void markAllAsReadByRecipientId(UUID recipientId) {
        jpaRepository.markAllAsReadByRecipientId(recipientId, LocalDateTime.now());
    }

    @Override
    public void deleteOlderThan(LocalDateTime cutoffDate) {
        jpaRepository.deleteOlderThan(cutoffDate);
    }

    @Override
    public void deleteById(NotificationId notificationId) {
        jpaRepository.deleteById(notificationId.id());
    }
}
