package be.kdg.banditgamesbackend.gamemetadata.core;

import be.kdg.banditgamesbackend.common.validation.Validators;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameUseCase;
import be.kdg.banditgamesbackend.gamemetadata.port.out.LoadGamePort;
import be.kdg.banditgamesbackend.gamemetadata.port.out.SaveGamePort;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class RegisterInternalGameUseCaseImpl implements RegisterInternalGameUseCase {

    private final LoadGamePort loadGamePort;
    private final List<SaveGamePort> saveGamePorts;

    public RegisterInternalGameUseCaseImpl(
            LoadGamePort loadGamePort,
            List<SaveGamePort> saveGamePorts) {
        this.loadGamePort = loadGamePort;
        this.saveGamePorts = saveGamePorts;
    }

    @Override
    public Game registerGame(RegisterInternalGameCommand command) {
        loadGamePort.findByName(command.name()).ifPresent(existing -> {
            throw new IllegalStateException("Game already exists with that name!");
        });

        Validators.requireNonBlank(command.name(), "Game name");

        Game game = Game.register(
                new GameId(UUID.randomUUID()),
                command.name(),
                command.description(),
                command.rules(),
                command.achievements(),
                command.playerConfiguration(),
                command.renderType(),
                command.metaData()
        );

        saveGamePorts.forEach(port -> port.save(game));
        game.clearDomainEvents();

        return game;
    }
}
