package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.Lobby;

public interface LeaveLobbyUseCase {
    Lobby leaveLobby(LeaveLobbyCommand command);
}
