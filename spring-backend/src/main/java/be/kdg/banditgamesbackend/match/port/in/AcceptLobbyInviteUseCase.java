package be.kdg.banditgamesbackend.match.port.in;

public interface AcceptLobbyInviteUseCase {
    LobbyJoinResult acceptInvite(AcceptLobbyInviteCommand command);
}