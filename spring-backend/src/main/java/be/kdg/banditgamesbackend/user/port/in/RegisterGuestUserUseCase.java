package be.kdg.banditgamesbackend.user.port.in;

public interface RegisterGuestUserUseCase {
    void registerGuest(RegisterGuestUserCommand command);
}
