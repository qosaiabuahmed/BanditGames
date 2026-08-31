package be.kdg.banditgamesbackend.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String username,
        String email,
        String playerTag,
        LocalDateTime registeredAt
) {
    public static UserRegisteredEvent of(UUID userId, String username, String email,
                                         String playerTag, LocalDateTime registeredAt) {
        return new UserRegisteredEvent(userId, username, email, playerTag, registeredAt);
    }
}
