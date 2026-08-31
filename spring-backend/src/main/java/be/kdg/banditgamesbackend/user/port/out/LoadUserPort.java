package be.kdg.banditgamesbackend.user.port.out;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<User> loadById(UUID userId);

    Optional<User> loadByEmail(Email email);

    boolean existsByEmail(Email email);

    List<User> loadAll();

    Optional<UUID> findUserIdByEmail(String email);

    boolean userExistsById(UUID userId);

    Optional<UUID> findUserIdByUsername(String username);

    Optional<User> findUserByUsername(String username);

    boolean isGuestUser(UUID playerId);
}