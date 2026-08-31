package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.LobbyInvite;

public interface SaveLobbyInvitePort {
    LobbyInvite save(LobbyInvite invite);
}