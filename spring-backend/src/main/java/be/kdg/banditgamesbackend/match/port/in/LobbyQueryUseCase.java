package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyId;

import java.util.Optional;

public interface LobbyQueryUseCase {
    Optional<Lobby> getLobby(LobbyId lobbyId);
}
