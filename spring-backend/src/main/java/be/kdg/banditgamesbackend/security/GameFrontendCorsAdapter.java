package be.kdg.banditgamesbackend.security;

import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GameFrontendCorsAdapter implements CorsOriginProvider {

    private final GameLookupService gameLookupService;

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String staticAllowedOrigins;

    @Override
    public List<String> getAllowedOrigins() {
        List<String> origins = new ArrayList<>(Arrays.asList(staticAllowedOrigins.split(",")));

        System.out.println("[CORS] Static origins: " + origins);

        try {
            List<String> gameFrontendUrls = gameLookupService.getAllFrontendUrls();

            System.out.println("[CORS] Game frontend URLs from service: " + gameFrontendUrls);

            for (String url : gameFrontendUrls) {
                if (!origins.contains(url)) {
                    origins.add(url);
                    System.out.println("[CORS] Added game URL: " + url);
                }
            }
        } catch (Exception e) {
            // If service call fails, just use static origins
            // This ensures CORS still works even if service is unavailable
            System.err.println("[CORS] Failed to load game URLs from service: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[CORS] Final allowed origins: " + origins);
        return origins;
    }
}