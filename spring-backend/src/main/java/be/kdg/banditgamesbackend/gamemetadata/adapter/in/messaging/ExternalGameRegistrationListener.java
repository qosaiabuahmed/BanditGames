package be.kdg.banditgamesbackend.gamemetadata.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.GameRegisteredEvent;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterExternalGameCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterExternalGameUseCase;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterGameProjectionCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterGameProjectionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalGameRegistrationListener {

    private final RegisterExternalGameUseCase registerGameUseCase;
    private final RegisterGameProjectionUseCase registerGameProjectionUseCase;

    @RabbitListener(queues = "${platform.messaging.queues.game-registration}")
    public void handleGameRegistration(GameRegisteredEvent event) {
        try {
            log.info("Received game registration event");

            PlayerConfiguration playerConfig = new PlayerConfiguration(
                    event.getPlayerConfigurations().minPlayers(),
                    event.getPlayerConfigurations().maxPlayers(),
                    event.getPlayerConfigurations().supportedPlayerTypes().stream()
                            .map(PlayerConfiguration.PlayerType::valueOf)
                            .collect(Collectors.toSet())
            );

            GameRules rules = new GameRules(
                    event.getRules().rulesText(),
                    event.getRules().rulesUrl(),
                    event.getRules().summary()
            );


            GameMetaData gameMetaData = new GameMetaData(
                    event.getMetaData().category(),
                    event.getMetaData().theme(),
                    event.getMetaData().designer(),
                    event.getMetaData().publisher(),
                    event.getMetaData().releaseYear(),
                    event.getMetaData().estimatedDurationMinutes(),
                    event.getMetaData().complexity(),
                    event.getMetaData().frontendUrl(),
                    event.getMetaData().pictureUrl()
            );

            log.info("Metadata: {}", gameMetaData);

            List<AchievementDefinition> achievements = event.getAchievements().stream()
                    .map(a -> new AchievementDefinition(
                            new AchievementId(a.code()),
                            a.code(),
                            a.description(),
                            null
                    ))
                    .toList();

            RegisterExternalGameCommand command = new RegisterExternalGameCommand(
                    event.getGameId(),
                    event.getName(),
                    event.getDescription(),
                    achievements,
                    rules,
                    gameMetaData,
                    playerConfig,
                    event.getRegisteredAt(),
                    RenderType.valueOf(event.getRenderType().toUpperCase())
            );



            RegisterGameProjectionCommand projectionCommand = new RegisterGameProjectionCommand(
                    event.getGameId(),
                    event.getName(),
                    event.getDescription(),
                    GameStatus.ACTIVE,
                    event.getRegisteredAt(),
                    event.getMetaData().frontendUrl(),
                    event.getMetaData().pictureUrl(),
                    RenderType.EXTERNAL_IFRAME
            );

            registerGameUseCase.registerExternalGame(command);
            log.info("Successfully registered external game with ID: {}", event.getGameId());

            registerGameProjectionUseCase.registerGameProjection(projectionCommand);
            log.info("Successfully registered game projection with ID: {}", event.getGameId());
        } catch (IllegalStateException e) {
            log.info("Game registration skipped - game already exists: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process game registration event - platform continues to work normally", e);
        }
    }
}
