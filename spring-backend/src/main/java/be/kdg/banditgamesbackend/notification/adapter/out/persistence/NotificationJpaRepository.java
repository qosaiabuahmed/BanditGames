package be.kdg.banditgamesbackend.notification.adapter.out.persistence;

import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByRecipientIdAndStatusNotOrderByCreatedAtDesc(UUID recipientId, NotificationStatus status);

    List<NotificationJpaEntity> findByRecipientIdAndStatusOrderByCreatedAtDesc(UUID recipientId, NotificationStatus status);

    long countUnreadByRecipientId(UUID recipientId);

    @Modifying
    @Query("UPDATE NotificationJpaEntity n " +
            "SET n.status = 'READ', n.readAt = :readAt " +
            "WHERE n.recipientId = :recipientId " +
            "AND n.status = 'UNREAD'")
    void markAllAsReadByRecipientId(@Param("recipientId") UUID recipientId,
                                    @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("DELETE FROM NotificationJpaEntity n " +
            "WHERE n.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutOffDate") LocalDateTime cutOffDate);

    List<NotificationJpaEntity> findByRecipientIdAndType(UUID recipientId, NotificationType type);
}
