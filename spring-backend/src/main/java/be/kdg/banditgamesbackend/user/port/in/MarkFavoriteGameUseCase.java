package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.common.events.GameFavoritedEvent;
import be.kdg.banditgamesbackend.common.events.GameUnfavoritedEvent;

public interface MarkFavoriteGameUseCase {
    GameFavoritedEvent markFavoriteGame(MarkFavoriteGameCommand command);
    GameUnfavoritedEvent unmarkFavoriteGame(MarkFavoriteGameCommand command);
}
