package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameInfoProjectionRepository extends JpaRepository<GameInfoProjection, UUID> {
    List<GameInfoProjection> findByGameIdIn(Collection<UUID> gameIds);

    Optional<GameInfoProjection> findByName(String name);
}
