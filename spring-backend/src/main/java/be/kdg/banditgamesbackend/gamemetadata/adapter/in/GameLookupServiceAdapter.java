package be.kdg.banditgamesbackend.gamemetadata.adapter.in;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import be.kdg.banditgamesbackend.gamemetadata.api.PlayerConfigurationInfo;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameLookupServiceAdapter implements GameLookupService {

    private final GameQueryUseCase gameQueryUseCase;

    @Override
    public Optional<GameInfoDto> findGameById(UUID gameId) {
        return gameQueryUseCase.findById(new GameId(gameId))
                .map(this::toGameInfoDto);
    }

    @Override
    public List<GameInfoDto> findGamesByIds(List<UUID> gameIds) {
        return gameIds.stream()
                .map(id -> findGameById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Optional<GameInfoDto> findGameByName(String name) {
        return gameQueryUseCase.findAll().stream()
                .filter(game -> game.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(this::toGameInfoDto);
    }

    @Override
    public Optional<String> getFrontendUrl(UUID gameId) {
        return gameQueryUseCase.findGameBasicInfoById(gameId)
                .map(GameQueryUseCase.GameBasicInfo::frontendUrl);
    }

    @Override
    public Optional<String> getFrontendUrlByName(String name) {
        return gameQueryUseCase.findGameBasicInfoByName(name)
                .map(GameQueryUseCase.GameBasicInfo::frontendUrl);
    }

    @Override
    public List<String> getAllFrontendUrls() {
        return gameQueryUseCase.findAll().stream()
                .map(game -> game.getMetaData().frontendUrl())
                .filter(url -> url != null && !url.isEmpty())
                .distinct()
                .toList();
    }

    private GameInfoDto toGameInfoDto(Game game) {
        return new GameInfoDto(
                game.getGameId().gameId(),
                game.getName(),
                game.getDescription(),
                game.getStatus().name(),
                game.getMetaData().frontendUrl(),
                game.getRegisteredAt(),
                new PlayerConfigurationInfo(
                        game.getPlayerConfiguration().minPlayers(),
                        game.getPlayerConfiguration().maxPlayers(),
                        game.getPlayerConfiguration().supportedPlayerTypes()
                )
        );
    }
}
