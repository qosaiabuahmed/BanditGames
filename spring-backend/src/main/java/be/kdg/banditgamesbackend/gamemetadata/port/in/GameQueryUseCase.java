package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameQueryUseCase {
    List<Game> findAll();
    Optional<Game> findById(GameId id);
    List<Game> getFilteredGames(GameFilterCriteria filterCriteria);

    Optional<GameBasicInfo> findGameBasicInfoById(UUID gameId);
    Optional<GameBasicInfo> findGameBasicInfoByName(String name);
    record GameBasicInfo(UUID gameId, String name, String frontendUrl) {}
}
