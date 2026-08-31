package be.kdg.banditgamesbackend.notification.core;

import be.kdg.banditgamesbackend.notification.port.in.MarkAllNotificationsAsReadUseCase;
import be.kdg.banditgamesbackend.notification.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MarkAllNotificationsAsReadUseCaseImpl implements MarkAllNotificationsAsReadUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkAllNotificationsAsReadUseCaseImpl.class);

    private final NotificationRepository notificationRepository;

    @Override
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read as user {}", userId);
        notificationRepository.markAllAsReadByRecipientId(userId);
    }
}
