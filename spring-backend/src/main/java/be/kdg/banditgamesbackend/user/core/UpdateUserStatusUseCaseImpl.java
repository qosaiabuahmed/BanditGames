package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.UpdateUserStatusCommand;
import be.kdg.banditgamesbackend.user.port.in.UpdateUserStatusUseCase;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateUserStatusUseCaseImpl implements UpdateUserStatusUseCase {

    private final LoadUserPort loadUserPort;
    private final List<SaveUserPort> saveUserPorts;

    public UpdateUserStatusUseCaseImpl(LoadUserPort loadUserPort, List<SaveUserPort> saveUserPorts) {
        this.loadUserPort = loadUserPort;
        this.saveUserPorts = saveUserPorts;
    }

    @Override
    @Transactional
    public void updateUserStatus(UpdateUserStatusCommand command) {
        User user = loadUserPort.loadById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        if (command.status() == be.kdg.banditgamesbackend.user.domain.UserStatus.ONLINE) {
            user.login();
        } else {
            user.changeStatus(command.status());
        }

        for (SaveUserPort port : saveUserPorts) {
            port.save(user);
        }
    }
}