package be.kdg.banditgamesbackend.notification.core;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;
import be.kdg.banditgamesbackend.notification.port.in.GetNotificationsQuery;
import be.kdg.banditgamesbackend.notification.port.out.NotificationPublisher;
import be.kdg.banditgamesbackend.notification.port.out.NotificationRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GetNotificationsQueryImpl implements GetNotificationsQuery {

    private static final Logger log = LoggerFactory.getLogger(GetNotificationsQueryImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;


    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(UUID userId) {
        log.debug("Fetching all notifications for user {}", userId);
        return notificationRepository.findByRecipientId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByStatus(UUID userId, NotificationStatus status) {
        log.debug("Fetching {} notifications for user {}", status, userId);
        return notificationRepository.findByRecipientIdAndStatus(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByRecipientId(userId);
    }
}
