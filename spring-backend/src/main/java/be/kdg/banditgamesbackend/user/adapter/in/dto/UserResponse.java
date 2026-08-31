package be.kdg.banditgamesbackend.user.adapter.in.dto;

import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.domain.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID userId,
    String username,
    String email,
    String playerTag,
    String avatar,
    UserStatus status,
    LocalDateTime registeredAt,
    LocalDateTime lastLoginAt
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
            user.getUserId(),
            user.getUsername(),
            user.getEmail().address(),
            user.getPlayerTag(),
            user.getAvatar(),
            user.getStatus(),
            user.getRegisteredAt(),
            user.getLastLoginAt()
        );
    }
}