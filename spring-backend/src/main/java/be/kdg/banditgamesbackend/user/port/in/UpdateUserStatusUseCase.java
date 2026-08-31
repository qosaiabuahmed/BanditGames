package be.kdg.banditgamesbackend.user.port.in;

public interface UpdateUserStatusUseCase {
    void updateUserStatus(UpdateUserStatusCommand command);
}