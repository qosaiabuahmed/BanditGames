package be.kdg.banditgamesbackend.common.events;

import java.time.LocalDateTime;
import java.util.UUID;


public record UserProfileUpdatedEvent(
    UUID userId,
    String username,
    String email,
    String playerTag,
    String avatar,
    LocalDateTime updatedAt
) {
    public static UserProfileUpdatedEvent of(UUID userId, String username, String email,
                                            String playerTag, String avatar) {
        return new UserProfileUpdatedEvent(userId, username, email, playerTag, avatar,
                                          LocalDateTime.now());
    }
}