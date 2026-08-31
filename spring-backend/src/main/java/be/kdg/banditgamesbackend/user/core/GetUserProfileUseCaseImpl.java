package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.GetUserProfileUseCase;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetUserProfileUseCaseImpl implements GetUserProfileUseCase {

    private final LoadUserPort loadUserPort;

    public GetUserProfileUseCaseImpl(LoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserProfile(UUID userId) {
        return loadUserPort.loadById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserProfileByEmail(String emailAddress) {
        Email email = new Email(emailAddress);
        return loadUserPort.loadByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + emailAddress));
    }

    @Override
    public Optional<User> getUserProfileByUsername(String username) {
        String normalizedUsername = username.toLowerCase().trim();
        return loadUserPort.findUserByUsername(normalizedUsername);
    }
}