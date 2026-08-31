package be.kdg.banditgamesbackend.notification.core;

import be.kdg.banditgamesbackend.notification.port.out.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

    private static final Logger log = LoggerFactory.getLogger(NotificationCleanupService.class.getName());
    private static final int RETENTION_DAYS = 30;

    private final NotificationRepository notificationRepository;

    /**
     * Run daily at 2 AM to clean up old notifications
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        log.info("Starting cleanup of notifications older than {}", cutoffDate);

        try {
            notificationRepository.deleteOlderThan(cutoffDate);
            log.info("Successfully cleaned up old notifications older than {} days", RETENTION_DAYS);
        } catch (Exception ex) {
            log.error("Failed to cleanup old notifications", ex);
        }
    }
}
