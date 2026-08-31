package be.kdg.banditgamesbackend.match.port.in;

public interface DeclineLobbyInviteUseCase {
    void declineInvite(DeclineLobbyInviteCommand command);
}