package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.SyncUserFromJwtCommand;
import be.kdg.banditgamesbackend.user.port.in.SyncUserFromJwtUseCase;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SyncUserFromJwtUseCaseImpl implements SyncUserFromJwtUseCase {

    private final LoadUserPort loadUserPort;
    private final List<SaveUserPort> saveUserPorts;

    public SyncUserFromJwtUseCaseImpl(
            LoadUserPort loadUserPort,
            List<SaveUserPort> saveUserPorts) {
        this.loadUserPort = loadUserPort;
        this.saveUserPorts = saveUserPorts;
    }

    @Override
    @Transactional
    public User syncUserFromJwt(SyncUserFromJwtCommand command) {
        Email email = new Email(command.email());

        if (loadUserPort.existsByEmail(email)) {
            log.debug("User with email {} already exists in database", command.email());
            return loadUserPort.loadByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User exists but could not be loaded"));
        }

        log.info("Syncing new user from Keycloak: email={}, userId={}", command.email(), command.userId());

        User user = User.create(
                command.userId(),
                command.username(),
                email,
                command.username()
        );

        User savedUser = null;
        for (SaveUserPort port : saveUserPorts) {
            savedUser = port.save(user);
        }

        log.info("Successfully synced user from Keycloak: userId={}", savedUser.getUserId());
        return savedUser;
    }
}