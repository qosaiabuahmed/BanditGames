package be.kdg.banditgamesbackend.gamemetadata.api;

import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NamedInterface("api")
public interface GameLookupService {
    Optional<GameInfoDto> findGameById(UUID gameId);
    List<GameInfoDto> findGamesByIds(List<UUID> gameIds);
    Optional<GameInfoDto> findGameByName(String name);
    Optional<String> getFrontendUrl(UUID gameId);
    Optional<String> getFrontendUrlByName(String name);
    List<String> getAllFrontendUrls();
}
