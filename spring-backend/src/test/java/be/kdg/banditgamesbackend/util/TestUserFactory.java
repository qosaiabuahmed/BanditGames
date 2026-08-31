package be.kdg.banditgamesbackend.util;

import be.kdg.banditgamesbackend.social.adapter.out.persistence.SocialUserProjectionJpaEntity;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaEntity;
import be.kdg.banditgamesbackend.user.domain.UserStatus;
import be.kdg.banditgamesbackend.user.domain.UserType;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestUserFactory {

    public static UserJpaEntity createUser(UUID userId, String username, String email) {
        return new UserJpaEntity(
                userId,
                username,
                email,
                username.toUpperCase(),
                UserStatus.ONLINE,
                LocalDateTime.now(),
                null,
                UserType.REGISTERED
        );
    }

    public static SocialUserProjectionJpaEntity createSocialProjection(
            UUID userId, String username, String email) {
        LocalDateTime now = LocalDateTime.now();
        return new SocialUserProjectionJpaEntity(userId, username, email, now, now);
    }
}
