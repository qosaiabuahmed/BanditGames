package be.kdg.banditgamesbackend.user.adapter.out.persistence;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.domain.UserType;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> loadById(UUID userId) {
        return userJpaRepository.findById(userId)
            .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> loadByEmail(Email email) {
        return userJpaRepository.findByEmail(email.address())
            .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.address());
    }

    @Override
    public List<User> loadAll() {
        return userJpaRepository.findAll().stream()
            .map(UserMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserJpaEntity::getUserId);
    }

    @Override
    public boolean userExistsById(UUID userId) {
        return userJpaRepository.existsById(userId);
    }

    @Override
    public Optional<UUID> findUserIdByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserJpaEntity::getUserId);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean isGuestUser(UUID playerId) {
        return userJpaRepository.findById(playerId)
                .map(user -> user.getUserType() == UserType.GUEST)
                .orElse(false);
    }
}