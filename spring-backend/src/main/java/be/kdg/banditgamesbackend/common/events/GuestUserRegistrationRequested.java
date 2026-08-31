package be.kdg.banditgamesbackend.common.events;

import java.util.UUID;

public record GuestUserRegistrationRequested(
        UUID userId,
        String username
) {}