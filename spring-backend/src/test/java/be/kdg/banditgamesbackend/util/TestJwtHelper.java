package be.kdg.banditgamesbackend.util;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


@TestComponent
public class TestJwtHelper {

    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_USERNAME = "testuser";

    public static JwtRequestPostProcessor mockJwtWithEmail(String email, String userId) {
        return jwt().jwt(builder -> builder
                .claim("email", email)
                .claim("sub", userId)
                .claim("preferred_username", email.split("@")[0])
                .claim("realm_access", Map.of("roles", List.of("userId")))
        );
    }

    public static JwtRequestPostProcessor jwtWithUser(String email, String userId) {
        return jwt().jwt(builder -> builder
                .claim("email", email)
                .claim("sub", userId)
                .claim("preferred_username", email.split("@")[0])
                .claim("email_verified", true)
                .claim("name", email.substring(0, email.indexOf("@")))
        );
    }

    public static JwtRequestPostProcessor mockJwtWithEmail(String email) {
        return mockJwtWithEmail(email, UUID.randomUUID().toString());
    }

    public static JwtRequestPostProcessor jwtWithUser(String email) {
        return mockJwtWithEmail(email, UUID.randomUUID().toString());
    }

    public static JwtRequestPostProcessor mockJwtWithEmail() {
        return mockJwtWithEmail(DEFAULT_EMAIL, UUID.randomUUID().toString());
    }

    public static JwtRequestPostProcessor mockJwtWithUserId(UUID userId) {
        return mockJwtWithEmail(DEFAULT_EMAIL, userId.toString());
    }


    public static JwtRequestPostProcessor mockJwtWithUserIdAsEmail(UUID userId) {
        String email = "user-" + userId + "@test.com";
        return mockJwtWithEmail(email, userId.toString());
    }

    private static String extractUsername(String email) {
        if (email == null || !email.contains("@")) {
            return DEFAULT_USERNAME;
        }
        return email.substring(0, email.indexOf("@"));
    }
}
