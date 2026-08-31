package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.Game;

public interface RegisterInternalGameUseCase {
    Game registerGame(RegisterInternalGameCommand command);
}
