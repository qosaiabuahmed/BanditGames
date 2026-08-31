package be.kdg.banditgamesbackend.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserLoggedInEvent(
    UUID userId,
    LocalDateTime loginAt
) {
    public static UserLoggedInEvent of(UUID userId) {
        return new UserLoggedInEvent(userId, LocalDateTime.now());
    }
}