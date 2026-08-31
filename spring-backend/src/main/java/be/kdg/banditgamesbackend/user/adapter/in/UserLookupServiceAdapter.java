package be.kdg.banditgamesbackend.user.adapter.in;

import be.kdg.banditgamesbackend.user.api.UserLookupService;
import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLookupServiceAdapter implements UserLookupService {

    private final LoadUserPort loadUserPort;

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        try {
            Email emailVO = new Email(email);

            return loadUserPort.loadByEmail(emailVO)
                    .map(User::getUserId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public String getUsernameById(UUID userId) {
        if (userId == null) {
            return "";
        }

        try {
            return loadUserPort.loadById(userId)
                    .map(User::getUsername)
                    .orElse("");
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    @Override
    public boolean userExists(UUID userId) {
        return loadUserPort.loadById(userId).isPresent();
    }

    @Override
    public Optional<UUID> findUserIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        try {
            return loadUserPort.findUserIdByUsername(username);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Player could not be found with username: " + username);
        }
    }

    @Override
    public boolean isGuestUser(UUID playerId) {
        return loadUserPort.isGuestUser(playerId);
    }

}
