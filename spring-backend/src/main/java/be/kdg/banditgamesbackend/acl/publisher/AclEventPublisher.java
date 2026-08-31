package be.kdg.banditgamesbackend.acl.publisher;

import be.kdg.banditgamesbackend.common.events.*;
import be.kdg.banditgamesbackend.common.messaging.MessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AclEventPublisher {

    @Qualifier("aclToPlatformRabbitTemplate")
    private final RabbitTemplate rabbitTemplate;

    private final MessagingProperties properties;

    public void publishToPlatform(PlatformEvent event) {
        switch (event) {
            case GameRegisteredEvent e -> publishGameRegistered(e);
            case MatchStartedEvent e -> publishMatchStarted(e);
            case MatchEndedEvent e -> publishMatchEnded(e);
            case AchievementUnlockedEvent e -> publishAchievementUnlocked(e);
            case MoveMadeEvent e -> publishMoveMade(e);
            case MatchStateUpdatedEvent e -> publishMatchStateUpdated(e);
            default -> log.warn("Unhandled platform event type: {}", event.getClass().getSimpleName());
        }
    }

    public void publishGameRegistered(GameRegisteredEvent event) {
        printPublishingEvent(event);

        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                properties.getRoutingKeys().gameExternalRegistered(),
                event
        );
    }

    private void publishMatchStarted(MatchStartedEvent event) {
        printPublishingEvent(event);

        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                properties.getRoutingKeys().matchStarted(),
                event
        );
    }

    private void publishMatchEnded(MatchEndedEvent event) {
        printPublishingEvent(event);

        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                properties.getRoutingKeys().matchCompleted(),
                event
        );
    }

    private void publishMoveMade(MoveMadeEvent event) {
        printPublishingEvent(event);

        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                properties.getRoutingKeys().moveMade(),
                event
        );
    }

    private void publishMatchStateUpdated(MatchStateUpdatedEvent event) {
        printPublishingEvent(event);

        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                properties.getRoutingKeys().matchStateUpdated(),
                event
        );
    }

    private void publishAchievementUnlocked(AchievementUnlockedEvent event) {
        printPublishingEvent(event);

        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                properties.getRoutingKeys().achievementUnlocked(),
                event
        );
    }


    private static void printPublishingEvent(PlatformEvent event) {
        log.info("ACL publishing to platform: {}", event.getEventType());
    }

}
