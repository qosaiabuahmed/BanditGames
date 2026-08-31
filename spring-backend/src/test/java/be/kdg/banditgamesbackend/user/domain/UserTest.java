package be.kdg.banditgamesbackend.user.domain;

import be.kdg.banditgamesbackend.common.events.UserLoggedInEvent;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void create_WithValidData_ShouldCreateUser() {
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        Email email = new Email("test@example.com");
        String playerTag = "TEST#0001";

        User user = User.create(userId, username, email, playerTag);

        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPlayerTag()).isEqualTo(playerTag);
        assertThat(user.getUserType()).isEqualTo(UserType.REGISTERED);
        assertThat(user.getDomainEvents()).hasSize(1);
    }

    @Test
    void create_WithBlankUsername_ShouldThrowException() {
        UUID userId = UUID.randomUUID();
        Email email = new Email("test@example.com");
        String playerTag = "TEST#0001";

        assertThatThrownBy(() -> User.create(userId, "", email, playerTag))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creatGuest_ShouldCreateGuestUser() {
        UUID externalId = UUID.randomUUID();
        User guest = User.creatGuest(externalId, "Player");

        assertThat(guest.getUserId()).isEqualTo(externalId);
        assertThat(guest.getUsername()).isEqualTo("guest_Player");
        assertThat(guest.getUserType()).isEqualTo(UserType.GUEST);
        assertThat(guest.isGuest()).isTrue();
    }

    @Test
    void convertToRegistered_ShouldChangeUserTypeAndData() {
        User guest = User.creatGuest(UUID.randomUUID(), "Player");
        
        guest.convertToRegistered("new@example.com", "newuser", "NEW#1234");

        assertThat(guest.getUserType()).isEqualTo(UserType.REGISTERED);
        assertThat(guest.getEmail().address()).isEqualTo("new@example.com");
        assertThat(guest.getUsername()).isEqualTo("newuser");
        assertThat(guest.getPlayerTag()).isEqualTo("NEW#1234");
        assertThat(guest.isGuest()).isFalse();
    }

    @Test
    void convertToRegistered_WhenAlreadyRegistered_ShouldThrowException() {
        User user = User.create(UUID.randomUUID(), "user", new Email("a@b.com"), "TAG");
        
        assertThatThrownBy(() -> user.convertToRegistered("c@d.com", "other", "OTHER"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateProfile_ShouldUpdateFieldsAndAddEvent() {
        User user = User.create(UUID.randomUUID(), "user", new Email("a@b.com"), "TAG");
        user.clearDomainEvents();

        user.updateProfile("newuser", new Email("new@b.com"), "NEWTAG", "avatar.png");

        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getEmail().address()).isEqualTo("new@b.com");
        assertThat(user.getPlayerTag()).isEqualTo("NEWTAG");
        assertThat(user.getAvatar()).isEqualTo("avatar.png");
        assertThat(user.getDomainEvents()).hasSize(1);
    }

    @Test
    void login_ShouldUpdateStatusAndLastLogin() {
        User user = User.create(UUID.randomUUID(), "user", new Email("a@b.com"), "TAG");
        user.clearDomainEvents();
        user.login();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ONLINE);
        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOfSatisfying(UserLoggedInEvent.class, event -> {
                    assertThat(event.userId()).isEqualTo(user.getUserId());
                    assertThat(event.loginAt()).isNotNull();
                });
    }
}
