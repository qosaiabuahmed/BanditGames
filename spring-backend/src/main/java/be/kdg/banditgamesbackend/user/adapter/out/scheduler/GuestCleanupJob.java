package be.kdg.banditgamesbackend.user.adapter.out.scheduler;

import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaEntity;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestCleanupJob {

    private final UserJpaRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiringGuests() {
        log.info("Cleaning up expired Guests");

        LocalDateTime now = LocalDateTime.now();
        List<UserJpaEntity> expiredGuests = userRepository.findByGuestExpiresAtAfter(now);

        if (expiredGuests.isEmpty()) {
            log.info("No expired Guests found");
            return;
        }

        expiredGuests.forEach(guest -> {
            userRepository.delete(guest);
            log.info("User {} with Id {} has been cleaned up", guest.getUsername(), guest.getUserId());
        });

        log.info("Cleaned up {} expired Guests", expiredGuests.size());
    }
}
