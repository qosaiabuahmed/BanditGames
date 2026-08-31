package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.CreateUserCommand;
import be.kdg.banditgamesbackend.user.port.in.CreateUserUseCase;
import be.kdg.banditgamesbackend.user.port.out.CreateKeycloakUserPort;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final LoadUserPort loadUserPort;
    private final List<SaveUserPort> saveUserPorts;
    private final CreateKeycloakUserPort createKeycloakUserPort;

    public CreateUserUseCaseImpl(
            LoadUserPort loadUserPort,
            List<SaveUserPort> saveUserPorts,
            CreateKeycloakUserPort createKeycloakUserPort) {
        this.loadUserPort = loadUserPort;
        this.saveUserPorts = saveUserPorts;
        this.createKeycloakUserPort = createKeycloakUserPort;
    }

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        Email email = new Email(command.email());

        if (loadUserPort.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + command.email() + " already exists");
        }

        String keycloakUserId = createKeycloakUserPort.createUser(
            command.username(),
            command.email(),
            command.password()
        );

        User user = User.create(
            UUID.fromString(keycloakUserId),
            command.username(),
            email,
            command.playerTag()
        );

        User savedUser = null;
        for (SaveUserPort port : saveUserPorts) {
            savedUser = port.save(user);
        }

        return savedUser;
    }
}