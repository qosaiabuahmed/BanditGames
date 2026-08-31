package be.kdg.banditgamesbackend.user.port.in;

import java.util.UUID;

public record ConvertGuestUserCommand(
    UUID guestUserId,
    String username,
    String email,
    String playerTag
) {}
