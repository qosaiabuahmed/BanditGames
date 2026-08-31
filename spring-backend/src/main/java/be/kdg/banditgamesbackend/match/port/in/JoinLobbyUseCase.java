package be.kdg.banditgamesbackend.match.port.in;

public interface JoinLobbyUseCase {
    LobbyJoinResult joinLobby(JoinLobbyCommand command);
}
