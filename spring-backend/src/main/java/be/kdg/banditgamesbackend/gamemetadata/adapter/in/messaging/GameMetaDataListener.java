package be.kdg.banditgamesbackend.gamemetadata.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.GameRegisteredEvent;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus;
import be.kdg.banditgamesbackend.gamemetadata.domain.RenderType;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterGameProjectionCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterGameProjectionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameMetaDataListener {

    private final RegisterGameProjectionUseCase registerGameProjectionUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleInternalGameRegistration(GameRegisteredEvent event) {
        log.info("Creating projection for game: gameId{}, name={}", event.getGameId(), event.getName());

        RegisterGameProjectionCommand command = new RegisterGameProjectionCommand(
                event.getGameId(),
                event.getName(),
                event.getDescription(),
                GameStatus.valueOf(event.getStatus().toUpperCase()),
                event.getRegisteredAt(),
                event.getMetaData().frontendUrl(),
                event.getMetaData().pictureUrl(),
                RenderType.INTERNAL
        );

        try {
            registerGameProjectionUseCase.registerGameProjection(command);
            log.info("Successfully created projection for game: {}", command.gameId());
        } catch (IllegalStateException e) {
            log.error("Error creating projection for game: {}", e.getMessage());
            throw e;
        }
    }
}
