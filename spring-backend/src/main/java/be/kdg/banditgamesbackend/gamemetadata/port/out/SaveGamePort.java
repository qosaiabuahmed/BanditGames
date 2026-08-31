package be.kdg.banditgamesbackend.gamemetadata.port.out;

import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;

public interface SaveGamePort {
    Game save(Game game);
    void deleteById(GameId id);
}
