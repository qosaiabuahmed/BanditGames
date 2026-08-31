package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadLobbyInvitePort {
    Optional<LobbyInvite> findById(LobbyInviteId inviteId);
    List<LobbyInvite> findPendingByToUserId(UUID toUserId);
    Optional<LobbyInvite> findPendingInvite(LobbyId lobbyId, UUID toUserId);
    List<LobbyInvite> findByLobbyId(LobbyId lobbyId);
}