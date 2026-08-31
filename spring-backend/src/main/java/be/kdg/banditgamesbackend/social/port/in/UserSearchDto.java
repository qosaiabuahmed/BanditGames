package be.kdg.banditgamesbackend.social.port.in;

import java.util.UUID;

public record UserSearchDto(
        UUID userId,
        String username,
        String email,
        String friendshipStatus
) {
}
