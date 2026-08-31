package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameJpaRepository extends JpaRepository<GameJpaEntity, UUID> {

    Optional<GameJpaEntity> findByName(String name);

    @Query("SELECT g FROM GameJpaEntity g " +
            "LEFT JOIN FETCH g.metaData md WHERE" +
            "(:name IS NULL OR :name = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND" +
            "(CAST(:registeredAfter AS timestamp) IS NULL OR g.registeredAt >= :registeredAfter) AND " +
            "(CAST(:registeredBefore AS timestamp ) IS NULL OR g.registeredAt <= :registeredBefore) AND " +
            "(:status IS NULL OR :status = '' OR g.status = :status) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(md.category) = LOWER(:category)) AND " +
            "(:theme IS NULL OR :theme = '' OR LOWER(md.theme) = LOWER(:theme)) AND " +
            "(:designer IS NULL OR :designer = '' OR LOWER(md.designer) LIKE LOWER(CONCAT('%', :designer, '%'))) AND " +
            "(:publisher IS NULL OR :publisher = '' OR LOWER(md.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
            "(:releaseYear IS NULL OR md.releaseYear = :releaseYear) AND " +
            "(:minDuration IS NULL OR md.estimatedDurationMinutes >= :minDuration) AND " +
            "(:maxDuration IS NULL OR md.estimatedDurationMinutes <= :maxDuration) AND " +
            "(:complexity IS NULL OR :complexity = '' OR LOWER(md.complexity) = LOWER(:complexity))")
    List<GameJpaEntity> findByFilters(
            @Param("name") String name,
            @Param("registeredAfter") LocalDateTime registeredAfter,
            @Param("registeredBefore") LocalDateTime registeredBefore,
            @Param("status") GameStatus status,
            @Param("category") String category,
            @Param("theme") String theme,
            @Param("designer") String designer,
            @Param("publisher") String publisher,
            @Param("releaseYear") Integer releaseYear,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            @Param("complexity") String complexity
    );

    long countByStatus(GameStatus status);

    @Query(value = "SELECT * FROM games ORDER BY registered_at DESC LIMIT :limit", nativeQuery = true)
    List<GameJpaEntity> findRecentGames(@Param("limit") int limit);
}
