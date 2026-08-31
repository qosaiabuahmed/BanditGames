package be.kdg.banditgamesbackend.notification.core;

import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationId;
import be.kdg.banditgamesbackend.notification.port.in.MarkNotificationAsReadCommand;
import be.kdg.banditgamesbackend.notification.port.in.MarkNotificationAsReadUseCase;
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
public class MarkNotificationAsReadUseCaseImpl implements MarkNotificationAsReadUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkNotificationAsReadUseCaseImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    public void markAsRead(MarkNotificationAsReadCommand command) {
        log.debug("Marking notification {} as read for user {}", command.notificationId(), command.userId());

        Notification notification = notificationRepository
                .findById(new NotificationId(command.notificationId()))
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + command.notificationId()));

        if (!notification.getRecipientId().equals(command.userId())) {
            throw new IllegalArgumentException(
                    "User " + command.userId() + " does not own the notification " + command.notificationId());
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }
}
