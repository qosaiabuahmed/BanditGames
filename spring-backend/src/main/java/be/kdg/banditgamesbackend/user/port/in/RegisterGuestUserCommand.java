package be.kdg.banditgamesbackend.user.port.in;

import java.util.Objects;
import java.util.UUID;

public record RegisterGuestUserCommand(
    UUID externalUserId,
    String username
) {
    public RegisterGuestUserCommand{
        Objects.requireNonNull(externalUserId, "External User Id cannot be null");
        
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
    }
}
