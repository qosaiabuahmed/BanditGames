package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.LobbyInvite;

import java.util.List;
import java.util.UUID;

public interface GetPendingLobbyInvitesQuery {
    List<LobbyInvite> getPendingInvites(UUID userId);
}