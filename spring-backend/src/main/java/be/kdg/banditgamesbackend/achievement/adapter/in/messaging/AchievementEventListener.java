package be.kdg.banditgamesbackend.achievement.adapter.in.messaging;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;
import be.kdg.banditgamesbackend.achievement.port.in.*;
import be.kdg.banditgamesbackend.common.events.AchievementUnlockedEvent;
import be.kdg.banditgamesbackend.common.events.GuestUserConvertedEvent;
import be.kdg.banditgamesbackend.common.events.PlatformEvent;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventListener {

    private final UnlockAchievementUseCase unlockAchievementUseCase;
    private final UserLookupService userLookupService;
    private final SaveTempAchievementUseCase saveTempAchievementUseCase;
    private final DeleteTempAchievementUseCase deleteTempAchievementUseCase;
    private final LoadTempAchievementUseCase loadTempAchievementUseCase;

    @RabbitListener(queues = "${platform.messaging.queues.achievements}")
    public void handleAchievementEvent(PlatformEvent event) {
        log.info("Received achievement event: {}", event.getEventType());

        if (event instanceof AchievementUnlockedEvent unlockEvent) {
            handleAchievementUnlocked(unlockEvent);
        } else {
            log.warn("Unhandled achievement event type: {}", event.getEventType());
        }
    }

    public void handleAchievementUnlocked(AchievementUnlockedEvent event) {
        final UUID playerId = event.getPlayerId();
        final UUID gameId = event.getGameId();

        if (playerId == null || gameId == null) {
            throw new IllegalArgumentException("Invalid achievement unlock event data: " + event);
        }
        
        boolean isGuest = userLookupService.isGuestUser(event.getPlayerId());
        // Todo: Broadcast to WebSocket subscribers for real-time updates

        try {
            if (isGuest) {
                SaveTempAchievementCommand command = new SaveTempAchievementCommand(
                        event.getPlayerId(),
                        event.getGameId(),
                        event.getMatchId(),
                        event.getAchievementCode(),
                        event.getUnlockedAt()
                );

                saveTempAchievementUseCase.save(command);

                log.info("Saved temporary achievement {} for guest user {}",
                        event.getAchievementCode(), event.getPlayerId());
            } else {
                UnlockAchievementCommand command = new UnlockAchievementCommand(
                        event.getPlayerId(),
                        event.getGameId(),
                        event.getMatchId(),
                        event.getAchievementCode(),
                        event.getUnlockedAt()
                );
                
                unlockAchievementUseCase.unlock(command);
            }

            log.info("Achievement {} unlocked for user {}",
                    event.getAchievementCode(), event.getPlayerId());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process AchievementUnlockedEvent: " + gameId, e);
        }

    }

    @EventListener
    @Transactional
    public void handleGuestConverted(GuestUserConvertedEvent event) {
        log.info("Migrating achievements for converted guests: {}", event.userId());

        List<TempAchievement> tempAchievements = loadTempAchievementUseCase.findByUserId(event.userId());

        tempAchievements.forEach(tempAchievement -> {
            UnlockAchievementCommand command = new UnlockAchievementCommand(
                    tempAchievement.getGuestUserId(),
                    tempAchievement.getGameId(),
                    tempAchievement.getMatchId(),
                    tempAchievement.getAchievementCode(),
                    tempAchievement.getUnlockedAt()
            );

            unlockAchievementUseCase.unlock(command);
        });

        deleteTempAchievementUseCase.deleteByUserId(event.userId());

        log.info("Migrated {} achievements for guest {}", tempAchievements.size(), event.userId());
    }
}