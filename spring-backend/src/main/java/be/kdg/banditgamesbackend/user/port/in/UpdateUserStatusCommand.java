package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.user.domain.UserStatus;

import java.util.UUID;

public record UpdateUserStatusCommand(
    UUID userId,
    UserStatus status
) {}