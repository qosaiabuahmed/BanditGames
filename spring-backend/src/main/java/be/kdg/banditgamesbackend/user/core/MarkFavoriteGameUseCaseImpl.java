package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.common.events.GameFavoritedEvent;
import be.kdg.banditgamesbackend.common.events.GameUnfavoritedEvent;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserFavoriteJpaRepository;
import be.kdg.banditgamesbackend.user.port.in.MarkFavoriteGameCommand;
import be.kdg.banditgamesbackend.user.port.in.MarkFavoriteGameUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class MarkFavoriteGameUseCaseImpl implements MarkFavoriteGameUseCase {

    private final UserFavoriteJpaRepository userFavoriteJpaRepository;

    @Override
    public GameFavoritedEvent markFavoriteGame(MarkFavoriteGameCommand command) {
        userFavoriteJpaRepository.addFavorite(command.userId(), command.gameId());
        return new GameFavoritedEvent(command.userId(), command.gameId());
    }

    @Override
    public GameUnfavoritedEvent unmarkFavoriteGame(MarkFavoriteGameCommand command) {
        userFavoriteJpaRepository.removeUserFavoriteJpaEntityByUser_UserIdAndGameId(command.userId(), command.gameId());
        return new GameUnfavoritedEvent(command.userId(), command.gameId());
    }
}
