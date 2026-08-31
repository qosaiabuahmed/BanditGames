package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MatchJpaRepository extends JpaRepository<MatchJpaEntity, UUID> {

    long countByStatus(MatchStatus status);

    @Query(value = """
        SELECT m.game_id, COUNT(*),
        SUM(CASE WHEN m.status = 'FINISHED' THEN 1 ELSE 0 END)
        FROM matches m
        GROUP BY m.game_id
        ORDER BY COUNT(*) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMostPlayedGames(@Param("limit") int limit);

    @Query(value = """
        SELECT AVG(EXTRACT(EPOCH FROM (m.finished_at - m.started_at)) / 60)
        FROM matches m
        WHERE m.status = 'FINISHED'
        AND m.started_at IS NOT NULL
        AND m.finished_at IS NOT NULL
        """, nativeQuery = true)
    Double findAverageMatchDurationMinutes();
}
