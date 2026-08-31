package be.kdg.banditgamesbackend.user.api;

import org.springframework.modulith.NamedInterface;

import java.util.Optional;
import java.util.UUID;

@NamedInterface(name = "api")
public interface UserLookupService {
    Optional<UUID> findUserIdByEmail(String email);

    String getUsernameById(UUID userId);

    boolean userExists(UUID userId);

    Optional<UUID> findUserIdByUsername(String username);

    boolean isGuestUser(UUID playerId);
}
