package be.kdg.banditgamesbackend.common.messaging;

import be.kdg.banditgamesbackend.common.events.GameRegisteredEvent;
import be.kdg.banditgamesbackend.common.events.PlatformEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlatformEventPublisher {

    @Qualifier("platformRabbitTemplate")
    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    public PlatformEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties properties, ApplicationEventPublisher eventPublisher) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    public void publishGameEvent(String routingKey, PlatformEvent event) {
        log.info("Publishing game event: {} with the routing key: {}", event.getEventType(), routingKey);
        rabbitTemplate.convertAndSend(
                properties.getExchanges().gameEvents(),
                routingKey,
                event
        );
    }

    public void publishPlatformEvent(String routingKey, PlatformEvent event) {
        log.info("Publishing platform event: {} with the routing key: {}", event.getEventType(), routingKey);
        rabbitTemplate.convertAndSend(
                properties.getExchanges().platformEvents(),
                routingKey,
                event
        );
    }

    // For specific events

    public void publishGameRegistered(GameRegisteredEvent event) {
        publishGameEvent(properties.getRoutingKeys().gameInternalRegistered(), event);
    }

    public void publishExternalGameRegistered(GameRegisteredEvent event) {
        publishGameEvent(properties.getRoutingKeys().gameExternalRegistered(), event);
    }

    public void publishMoveMade(PlatformEvent event) {
        publishGameEvent(properties.getRoutingKeys().moveMade(), event);
    }

    public void publishAchievementUnlocked(PlatformEvent event) {
        publishGameEvent(properties.getRoutingKeys().achievementUnlocked(), event);
    }

    public void publishPlayerJoined(PlatformEvent event) {
        publishPlatformEvent(properties.getRoutingKeys().playerJoined(), event);
    }

    public void publishPlayerLeft(PlatformEvent event) {
        publishPlatformEvent(properties.getRoutingKeys().playerLeft(), event);
    }

    public void publishMatchCreated(PlatformEvent event) {
        publishPlatformEvent(properties.getRoutingKeys().matchCreated(), event);
        eventPublisher.publishEvent(event);
    }

    public void publishMatchStarted(PlatformEvent event) {
        publishPlatformEvent(properties.getRoutingKeys().matchStarted(), event);
    }

    public void publishMatchCompleted(PlatformEvent event) {
        publishPlatformEvent(properties.getRoutingKeys().matchCompleted(), event);
    }
}
