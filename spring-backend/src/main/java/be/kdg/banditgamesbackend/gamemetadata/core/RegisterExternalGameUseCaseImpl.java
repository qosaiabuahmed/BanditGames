package be.kdg.banditgamesbackend.gamemetadata.core;

import be.kdg.banditgamesbackend.common.validation.Validators;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterExternalGameCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterExternalGameUseCase;
import be.kdg.banditgamesbackend.gamemetadata.port.out.SaveGamePort;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
public class RegisterExternalGameUseCaseImpl implements RegisterExternalGameUseCase {

    private final List<SaveGamePort> saveGamePorts;

    public RegisterExternalGameUseCaseImpl(List<SaveGamePort> saveGamePorts) {
        this.saveGamePorts = saveGamePorts;
    }

    @Override
    public void registerExternalGame(RegisterExternalGameCommand command) {
        validateGameData(command);

        Game externalGame = Game.registerExternal(
                new GameId(command.gameId()),
                command.gameName(),
                command.gameDescription(),
                command.rules(),
                command.achievements(),
                command.playerConfig(),
                command.metaData(),
                command.registeredAt(),
                command.renderType()
                );

        saveGamePorts.forEach(port -> port.save(externalGame));
        externalGame.clearDomainEvents();

        log.info("External game saved to domain: {} with {} achievements", command.gameId(), command.achievements().size());
    }

    private void validateGameData(RegisterExternalGameCommand command) {
        Validators.requireNonNull(new GameId(command.gameId()), "Game Id");
        Validators.requireNonBlank(command.gameName(), "Game Name");
        Validators.requireNonBlank(command.gameDescription(), "Game Description");
        Validators.requireNonNull(command.rules(), "Rules");
        Validators.requireNonNull(command.achievements(), "Achievements");
        Validators.requireNonNull(command.playerConfig(), "Player Configuration");
        Validators.requireNonNull(command.metaData(), "Meta Data");
        Validators.requireNonNull(command.renderType(), "Render Type");
        Validators.requireNonNull(command.registeredAt(), "Registered At");
    }
}
