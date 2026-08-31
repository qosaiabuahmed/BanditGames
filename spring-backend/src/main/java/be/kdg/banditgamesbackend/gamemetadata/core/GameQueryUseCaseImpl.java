package be.kdg.banditgamesbackend.gamemetadata.core;

import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameFilterCriteria;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameQueryUseCase;
import be.kdg.banditgamesbackend.gamemetadata.port.out.LoadGamePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GameQueryUseCaseImpl implements GameQueryUseCase {

    private final LoadGamePort loadGamePort;

    @Override
    public List<Game> findAll() {
        return loadGamePort.findAll();
    }

    @Override
    public Optional<Game> findById(GameId id) {
        return loadGamePort.findById(id);
    }

    @Override
    public List<Game> getFilteredGames(GameFilterCriteria filterCriteria) {
        return loadGamePort.findByFilters(filterCriteria);
    }

    @Override
    public Optional<GameBasicInfo> findGameBasicInfoById(UUID gameId) {
        return loadGamePort.findById(new GameId(gameId))
                .map(game -> new GameBasicInfo(
                        game.getGameId().gameId(),
                        game.getName(),
                        game.getMetaData().frontendUrl()
                ));
    }

    @Override
    public Optional<GameBasicInfo> findGameBasicInfoByName(String name) {
        return loadGamePort.findByName(name)
                .map(game -> new GameBasicInfo(
                        game.getGameId().gameId(),
                        game.getName(),
                        game.getMetaData().frontendUrl()
                ));
    }
}
