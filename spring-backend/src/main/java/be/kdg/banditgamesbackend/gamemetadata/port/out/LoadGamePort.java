package be.kdg.banditgamesbackend.gamemetadata.port.out;

import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameFilterCriteria;

import java.util.List;
import java.util.Optional;

public interface LoadGamePort {
    Optional<Game> findById(GameId id);
    Optional<Game> findByName(String name);
    List<Game> findAll();
    List<Game> findByFilters(GameFilterCriteria filterCriteria);
}
