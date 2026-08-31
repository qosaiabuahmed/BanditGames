package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.match.domain.LobbyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LobbyJpaRepository extends JpaRepository<LobbyJpaEntity, UUID> {
    Optional<LobbyJpaEntity> findFirstByGameIdAndStatusOrderByCreatedAt(UUID gameId, LobbyStatus status);

    List<LobbyJpaEntity> findByGameIdAndStatus(UUID gameId, LobbyStatus status);

    @Query("select l from LobbyJpaEntity l join l.players p where p.userId = :userId and l.status = 'OPEN'")
    Optional<LobbyJpaEntity> findOpenLobbyByUser(@Param("userId") UUID userId);
}
