package be.kdg.banditgamesbackend.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtUtils {

    /**
     * Extracts the user ID from JWT.
     * Since we use Keycloak's user ID as our primary key, we just extract the 'sub' claim.
     */
    public UUID extractUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("Subject (sub) not found in JWT");
        }
        return UUID.fromString(sub);
    }

    /**
     * Extracts the user's email from JWT
     */
    public String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email not found in JWT token");
        }
        return email;
    }

    /**
     * Extracts username from JWT claims
     */
    public String extractUsername(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            return extractEmail(jwt);  // Fallback to email
        }
        return username;
    }

}