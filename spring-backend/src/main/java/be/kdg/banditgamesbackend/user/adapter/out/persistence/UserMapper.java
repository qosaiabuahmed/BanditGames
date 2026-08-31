package be.kdg.banditgamesbackend.user.adapter.out.persistence;

import be.kdg.banditgamesbackend.user.domain.Email;
import be.kdg.banditgamesbackend.user.domain.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserJpaEntity toJpaEntity(User user) {
        return new UserJpaEntity(
            user.getUserId(),
            user.getUsername(),
            user.getEmail() != null ? user.getEmail().address() : null,
            user.getPlayerTag(),
            user.getAvatar(),
            user.getStatus(),
            user.getRegisteredAt(),
            user.getLastLoginAt(),
            user.getGuestExpiresAt(),
            user.getUserType()
        );
    }

    public static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
            entity.getUserId(),
            entity.getUsername(),
            entity.getEmail() != null ? new Email(entity.getEmail()) : null,
            entity.getPlayerTag(),
            entity.getAvatar(),
            entity.getStatus(),
            entity.getRegisteredAt(),
            entity.getLastLoginAt(),
            entity.getUserType()
        );
    }
}