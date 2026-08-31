package be.kdg.banditgamesbackend.social.port.in;

public interface SendFriendRequestUseCase {
    void sendFriendRequest(SendFriendRequestCommand command);
}
