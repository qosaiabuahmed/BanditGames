package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.mapper.GameJpaMapper;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.out.SaveGamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveGamePortAdapter implements SaveGamePort {

    private final GameJpaRepository gameJpaRepository;
    private final GameJpaMapper mapper;

    @Override
    public Game save(Game game) {
        GameJpaEntity persisted = gameJpaRepository.save(mapper.toEntity(game));
        return mapper.toDomain(persisted);
    }

    @Override
    public void deleteById(GameId id) {
        gameJpaRepository.deleteById(id.gameId());
    }
}
