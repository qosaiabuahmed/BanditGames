package be.kdg.banditgamesbackend.user.adapter.in.filter;

import be.kdg.banditgamesbackend.user.port.in.SyncUserFromJwtCommand;
import be.kdg.banditgamesbackend.user.port.in.SyncUserFromJwtUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class UserSyncFilter extends OncePerRequestFilter {

    private final SyncUserFromJwtUseCase syncUserFromJwtUseCase;

    public UserSyncFilter(SyncUserFromJwtUseCase syncUserFromJwtUseCase) {
        this.syncUserFromJwtUseCase = syncUserFromJwtUseCase;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only sync if user is authenticated with JWT
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            try {
                String sub = jwt.getClaimAsString("sub");
                String email = jwt.getClaimAsString("email");
                String username = jwt.getClaimAsString("preferred_username");

                if (sub != null && email != null && username != null) {
                    UUID userId = UUID.fromString(sub);

                    SyncUserFromJwtCommand command = new SyncUserFromJwtCommand(
                            userId,
                            email,
                            username
                    );

                    syncUserFromJwtUseCase.syncUserFromJwt(command);
                }
            } catch (Exception e) {
                log.error("Error syncing user from JWT", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/ws/");
    }
}