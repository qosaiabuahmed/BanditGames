package be.kdg.banditgamesbackend.user.port.in;


import java.util.List;
import java.util.UUID;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;

public interface GetFavoriteGamesQuery {
    List<GameInfoDto> getFavoriteGames(UUID userId);
}
