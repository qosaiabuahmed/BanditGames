package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.out.SaveGamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameEventPublisher implements SaveGamePort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Game save(Game game) {
        game.getDomainEvents().forEach(applicationEventPublisher::publishEvent);
        return game;
    }

    @Override
    public void deleteById(GameId id) {
        // No-op for event publisher
    }
}
