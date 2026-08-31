package be.kdg.banditgamesbackend.common.events;

import java.util.UUID;

public record GuestUserConvertedEvent(
    UUID userId,
    String username,
    String email
) {}
