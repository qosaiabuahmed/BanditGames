package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.mapper.GameJpaMapper;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameFilterCriteria;
import be.kdg.banditgamesbackend.gamemetadata.port.out.LoadGamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadGamePortAdapter implements LoadGamePort {

    private final GameJpaRepository gameJpaRepository;
    private final GameJpaMapper mapper;

    @Override
    public Optional<Game> findById(GameId id) {
        return gameJpaRepository.findById(id.gameId())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Game> findByName(String name) {
        return gameJpaRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public List<Game> findAll() {
        return gameJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Game> findByFilters(GameFilterCriteria filterCriteria) {
        if (!filterCriteria.hasFilters()) {
            return findAll();
        }

        return gameJpaRepository.findByFilters(
                        filterCriteria.name(),
                        filterCriteria.registeredAfter(),
                        filterCriteria.registeredBefore(),
                        filterCriteria.status(),
                        filterCriteria.category(),
                        filterCriteria.theme(),
                        filterCriteria.designer(),
                        filterCriteria.publisher(),
                        filterCriteria.releaseYear(),
                        filterCriteria.minDuration(),
                        filterCriteria.maxDuration(),
                        filterCriteria.complexity()
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
