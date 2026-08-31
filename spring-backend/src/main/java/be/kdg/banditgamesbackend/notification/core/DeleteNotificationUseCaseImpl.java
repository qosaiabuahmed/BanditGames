package be.kdg.banditgamesbackend.notification.core;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationId;
import be.kdg.banditgamesbackend.notification.port.in.DeleteNotificationCommand;
import be.kdg.banditgamesbackend.notification.port.in.DeleteNotificationUseCase;
import be.kdg.banditgamesbackend.notification.port.out.NotificationPublisher;
import be.kdg.banditgamesbackend.notification.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteNotificationUseCaseImpl implements DeleteNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteNotificationUseCaseImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    public void deleteNotification(DeleteNotificationCommand command) {
        log.info("Deleting notification {} for user {}", command.notificationId(), command.userId());

        Notification notification=notificationRepository
                .findById(new NotificationId(command.notificationId()))
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + command.notificationId()));

        if (!notification.getRecipientId().equals(command.userId())) {
            throw new IllegalArgumentException(
                    "User " + command.userId() + " does not own notification " + command.notificationId());
        }

        notification.delete();
        notificationRepository.save(notification);
    }
}
