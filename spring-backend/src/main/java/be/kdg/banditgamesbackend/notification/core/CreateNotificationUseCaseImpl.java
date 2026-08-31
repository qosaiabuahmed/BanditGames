package be.kdg.banditgamesbackend.notification.core;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.port.in.CreateNotificationCommand;
import be.kdg.banditgamesbackend.notification.port.in.CreateNotificationUseCase;
import be.kdg.banditgamesbackend.notification.port.out.NotificationPublisher;
import be.kdg.banditgamesbackend.notification.port.out.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateNotificationUseCaseImpl implements CreateNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateNotificationUseCaseImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    public Notification createNotification(CreateNotificationCommand command) {
        log.info("Creating notification for user {} of type {}",
                command.recipientId(), command.type());

        Notification notification = Notification.create(
                command.recipientId(),
                command.type(),
                command.title(),
                command.message(),
                command.priority(),
                command.metadata()
        );

        Notification savedNotification = notificationRepository.save(notification);

        try {
            notificationPublisher.publishToUser(savedNotification);
            log.debug("Notification {} sent via WebSocket to user {}",
                    savedNotification.getNotificationId(), command.recipientId());
        } catch (Exception e) {
            log.warn("Failed to publish notification {} via WebSocket: {}",
                    savedNotification.getNotificationId(), e.getMessage());
        }

        return savedNotification;
    }
}
