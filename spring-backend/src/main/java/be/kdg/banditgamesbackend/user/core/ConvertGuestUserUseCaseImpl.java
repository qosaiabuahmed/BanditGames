package be.kdg.banditgamesbackend.user.core;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaEntity;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaRepository;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserMapper;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.domain.UserType;
import be.kdg.banditgamesbackend.user.port.in.ConvertGuestUserCommand;
import be.kdg.banditgamesbackend.user.port.in.ConvertGuestUserUseCase;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
@Transactional
public class ConvertGuestUserUseCaseImpl implements ConvertGuestUserUseCase {

    private final UserJpaRepository userRepository;
    private final List<SaveUserPort> saveUserPorts;

    public ConvertGuestUserUseCaseImpl(
            UserJpaRepository userRepository,
            List<SaveUserPort> saveUserPorts) {
        this.userRepository = userRepository;
        this.saveUserPorts = saveUserPorts;
    }

    @Override
    public User convertToRegistered(ConvertGuestUserCommand command) {
        UserJpaEntity guest = userRepository.findById(command.guestUserId())
        .orElseThrow(() -> new IllegalArgumentException("Guest User not found: " + command.guestUserId()));

        if (guest.getUserType() != UserType.GUEST) {
            throw new IllegalStateException("User is already registered");
        }

        if (userRepository.findByUsername(command.username()).isPresent()) {
            throw new IllegalArgumentException("User with username " + command.username() + " already exists");
        }

        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("User with email " + command.email() + " already exists");
        }

        User domainGuest = UserMapper.toDomain(guest);

        domainGuest.convertToRegistered(command.email(), command.username(), command.playerTag());

        saveUserPorts.forEach(port -> port.save(domainGuest));
        domainGuest.clearDomainEvents();

        log.info("Guest user {} converted to registered user: {}", command.guestUserId(), command.username());

        return domainGuest;
    }

}
