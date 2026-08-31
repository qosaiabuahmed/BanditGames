package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LobbyInviteJpaRepository extends JpaRepository<LobbyInviteJpaEntity, UUID> {

    @Query("SELECT i FROM LobbyInviteJpaEntity i WHERE i.toUserId = :toUserId AND i.status = 'PENDING'")
    List<LobbyInviteJpaEntity> findPendingByToUserId(UUID toUserId);

    @Query("SELECT i FROM LobbyInviteJpaEntity i WHERE i.lobbyId = :lobbyId AND i.toUserId = :toUserId AND i.status = 'PENDING'")
    Optional<LobbyInviteJpaEntity> findPendingInvite(UUID lobbyId, UUID toUserId);

    List<LobbyInviteJpaEntity> findByLobbyId(UUID lobbyId);
}