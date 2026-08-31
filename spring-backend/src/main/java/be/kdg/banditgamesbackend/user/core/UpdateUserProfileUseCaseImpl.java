package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.UpdateUserProfileCommand;
import be.kdg.banditgamesbackend.user.port.in.UpdateUserProfileUseCase;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateUserProfileUseCaseImpl implements UpdateUserProfileUseCase {

    private final LoadUserPort loadUserPort;
    private final List<SaveUserPort> saveUserPorts;

    public UpdateUserProfileUseCaseImpl(
            LoadUserPort loadUserPort,
            List<SaveUserPort> saveUserPorts) {
        this.loadUserPort = loadUserPort;
        this.saveUserPorts = saveUserPorts;
    }

    @Override
    @Transactional
    public User updateUserProfile(UpdateUserProfileCommand command) {
        User user = loadUserPort.loadById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        Email newEmail = command.email() != null ? new Email(command.email()) : null;

        user.updateProfile(
            command.username(),
            newEmail,
            command.playerTag(),
            command.avatar()
        );

        User updatedUser = null;
        for (SaveUserPort port : saveUserPorts) {
            updatedUser = port.save(user);
        }

        return updatedUser;
    }
}