package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserFavoriteJpaRepository;
import be.kdg.banditgamesbackend.user.port.in.GetFavoriteGamesQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class GetUserFavoriteGameQueryImpl implements GetFavoriteGamesQuery {

    private final UserFavoriteJpaRepository favoriteRepository;
    private final GameLookupService gameLookupService;

    @Override
    public List<GameInfoDto> getFavoriteGames(UUID userId) {

        List<UUID> favoriteGameIds = favoriteRepository.findGameIdsByUserId(userId);

        return gameLookupService.findGamesByIds(favoriteGameIds);

    }
}
