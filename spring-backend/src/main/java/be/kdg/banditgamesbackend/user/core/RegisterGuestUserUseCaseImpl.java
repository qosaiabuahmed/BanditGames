package be.kdg.banditgamesbackend.user.core;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaEntity;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaRepository;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserMapper;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.RegisterGuestUserCommand;
import be.kdg.banditgamesbackend.user.port.in.RegisterGuestUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterGuestUserUseCaseImpl implements RegisterGuestUserUseCase {

    private final UserJpaRepository userRepository;

    @Override
    public void registerGuest(RegisterGuestUserCommand command) {
        if (userRepository.existsById(command.externalUserId())) {
            log.info("User {} already exists, skipping guest registration", command.externalUserId());
            return;
        }

        User guest = User.creatGuest(command.externalUserId(), command.username());
        UserJpaEntity guestEntity = UserMapper.toJpaEntity(guest);
        userRepository.save(guestEntity);

        log.info("Guest user created: userId={}, username={}", command.externalUserId(), command.username());
    }

}
