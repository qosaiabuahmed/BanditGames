package be.kdg.banditgamesbackend.sdk.messaging;

import be.kdg.banditgamesbackend.sdk.model.SDKAchievementUnlockedEvent;
import be.kdg.banditgamesbackend.sdk.model.SDKMoveMadeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class BanditGamesEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String GAME_EVENTS_EXCHANGE = "game.events.exchange";

    public void publishMoveMade(SDKMoveMadeEvent event) {
        log.info("SDK publishing MOVE_MADE event for match {}", event.getMatchId());
        publishEvent("move.made", event);
    }

    public void publishAchievementUnlocked(SDKAchievementUnlockedEvent event) {
        log.info("SDK publishing ACHIEVEMENT_UNLOCKED event for match {}", event.getMatchId());
        publishEvent("achievement.unlocked", event);
    }

    public void publishEvent(String routingKey, Object event) {
        log.info("SDK publishing event to exchange {} with routing key {}", GAME_EVENTS_EXCHANGE, routingKey);
        rabbitTemplate.convertAndSend(GAME_EVENTS_EXCHANGE, routingKey, event);
    }
}
