package be.kdg.banditgamesbackend.user.port.in;

public record CreateUserCommand(
    String username,
    String email,
    String playerTag,
    String password
) {}