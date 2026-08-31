package be.kdg.banditgamesbackend.user.port.in;

import java.util.UUID;

public record SyncUserFromJwtCommand(
        UUID userId,
        String email,
        String username
) {
}