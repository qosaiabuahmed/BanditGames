package be.kdg.banditgamesbackend.user.adapter.out.persistence;

import be.kdg.banditgamesbackend.user.domain.UserStatus;
import be.kdg.banditgamesbackend.user.domain.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {

    @Id
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String playerTag;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime lastLoginAt;

    private LocalDateTime guestExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    public UserJpaEntity(UUID userId, String username, String email, String playerTag, UserStatus status, LocalDateTime registeredAt, LocalDateTime guestExpiresAt, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.playerTag = playerTag;
        this.status = status;
        this.registeredAt = registeredAt;
        this.guestExpiresAt = guestExpiresAt;
        this.userType = userType;
    }
}