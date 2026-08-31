package be.kdg.banditgamesbackend.user.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @Test
    void create_WithValidEmail_ShouldWork() {
        Email email = new Email("test@example.com");
        assertThat(email.address()).isEqualTo("test@example.com");
    }

    @Test
    void create_WithInvalidEmail_ShouldThrowException() {
        assertThatThrownBy(() -> new Email("invalid-email"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_WithNull_ShouldThrowException() {
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(NullPointerException.class);
    }
}
