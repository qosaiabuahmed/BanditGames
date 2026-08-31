package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface GetUserProfileUseCase {
    User getUserProfile(UUID userId);

    User getUserProfileByEmail(String email);

    Optional<User> getUserProfileByUsername(String username);
}