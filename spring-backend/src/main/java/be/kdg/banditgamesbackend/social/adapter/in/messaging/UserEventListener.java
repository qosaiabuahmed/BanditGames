package be.kdg.banditgamesbackend.social.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.UserProfileUpdatedEvent;
import be.kdg.banditgamesbackend.common.events.UserRegisteredEvent;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.SocialUserProjectionJpaEntity;
import be.kdg.banditgamesbackend.social.adapter.out.persistence.SocialUserProjectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final SocialUserProjectionRepository projectionRepository;

    @EventListener
    @Transactional
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        SocialUserProjectionJpaEntity projection = new SocialUserProjectionJpaEntity(
                event.userId(),
                event.username(),
                event.email(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        projectionRepository.save(projection);
    }

    @EventListener
    @Transactional
    public void handleUserUpdated(UserProfileUpdatedEvent event) {
        projectionRepository.findById(event.userId()).ifPresentOrElse(
                projection -> {
                    projection.setUsername(event.username());
                    projection.setEmail(event.email());
                    projection.setUpdatedAt(LocalDateTime.now());
                    projectionRepository.save(projection);
                    log.info("Updated social user projection for user: {}", event.userId());
                }, () -> log.warn("User projection not found for update: {}", event.userId())
        );
    }
}
