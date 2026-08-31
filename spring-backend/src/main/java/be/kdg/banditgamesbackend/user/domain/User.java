package be.kdg.banditgamesbackend.user.domain;

import be.kdg.banditgamesbackend.common.events.GuestUserConvertedEvent;
import be.kdg.banditgamesbackend.common.events.UserLoggedInEvent;
import be.kdg.banditgamesbackend.common.events.UserProfileUpdatedEvent;
import be.kdg.banditgamesbackend.common.events.UserRegisteredEvent;
import be.kdg.banditgamesbackend.common.validation.Validators;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class User {

    private final UUID userId;
    private final LocalDateTime registeredAt;
    private final List<Object> domainEvents = new ArrayList<>();
    private String username;
    private Email email;
    private String playerTag;
    private String avatar;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime guestExpiresAt;
    private UserType userType;


    private User(UUID userId, String username, Email email, String playerTag,
                 String avatar, UserStatus status, LocalDateTime registeredAt,
                 LocalDateTime lastLoginAt, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.playerTag = playerTag;
        this.avatar = avatar;
        this.status = status;
        this.registeredAt = registeredAt;
        this.lastLoginAt = lastLoginAt;
        this.userType = userType;
    }

    private User(UUID userId, String username, Email email, String playerTag,
                 String avatar, UserStatus status, LocalDateTime registeredAt,
                 LocalDateTime lastLoginAt, UserType userType, LocalDateTime guestExpiresAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.playerTag = playerTag;
        this.avatar = avatar;
        this.status = status;
        this.registeredAt = registeredAt;
        this.lastLoginAt = lastLoginAt;
        this.userType = userType;
        this.guestExpiresAt = guestExpiresAt;
    }


    public static User create(UUID userId, String username, Email email, String playerTag) {
        Validators.requireNonNull(userId, "User ID cannot be null");
        Validators.requireNonBlank(username, "Username");
        Validators.requireNonNull(email, "Email cannot be null");
        Validators.requireNonBlank(playerTag, "PlayerTag");

        LocalDateTime now = LocalDateTime.now();
        User user = new User(
                userId,
                username,
                email,
                playerTag,
                null,
                UserStatus.OFFLINE,
                now,
                null,
                UserType.REGISTERED
        );

        user.domainEvents.add(new UserRegisteredEvent(
                user.userId,
                user.username,
                user.email.address(),
                user.playerTag,
                now
        ));

        return user;
    }

    public static User creatGuest(UUID externalUserId, String username) {
        return new User(
                externalUserId,
                "guest_" + username,
                new Email(("guest_" + username + "@guest.com").trim().replace(" ", "")),
                "guest_" + username,
                null,
                UserStatus.OFFLINE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                UserType.GUEST,
                LocalDateTime.now().plusHours(24)
        );
    }


    public static User reconstitute(UUID userId, String username, Email email,
                                    String playerTag, String avatar, UserStatus status,
                                    LocalDateTime registeredAt, LocalDateTime lastLoginAt, UserType userType) {
        return new User(userId, username, email, playerTag, avatar, status,
                registeredAt, lastLoginAt, userType);
    }

    public void updateProfile(String username, Email email, String playerTag, String avatar) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
        if (email != null) {
            this.email = email;
        }
        if (playerTag != null && !playerTag.isBlank()) {
            this.playerTag = playerTag;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }

        this.domainEvents.add(new UserProfileUpdatedEvent(
                this.userId,
                this.username,
                this.email.address(),
                this.playerTag,
                this.avatar,
                LocalDateTime.now()
        ));
    }

    public void login() {
        this.lastLoginAt = LocalDateTime.now();
        this.status = UserStatus.ONLINE;

        this.domainEvents.add(new UserLoggedInEvent(
                this.userId,
                this.lastLoginAt
        ));
    }

    public void logout() {
        this.status = UserStatus.OFFLINE;
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Status cannot be null");
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void convertToRegistered(String email, String username, String playerTag) {
        if (this.userType != UserType.GUEST) {
            throw new IllegalStateException("Only guest users can be converted to registered users");
        }

        this.userType = UserType.REGISTERED;
        this.email = new Email(email);
        this.username = username;
        this.playerTag = playerTag;
        this.guestExpiresAt = null;

        // Add domain event
        this.domainEvents.add(new GuestUserConvertedEvent(
                this.userId,
                username,
                email
        ));
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isGuest() {
        return this.userType == UserType.GUEST;
    }

    public boolean isExpired() {
        return isGuest() && guestExpiresAt != null && LocalDateTime.now().isAfter(guestExpiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}