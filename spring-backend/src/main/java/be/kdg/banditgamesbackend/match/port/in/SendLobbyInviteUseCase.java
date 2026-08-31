package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;

public interface SendLobbyInviteUseCase {
    LobbyInviteId sendInvite(SendLobbyInviteCommand command);
}