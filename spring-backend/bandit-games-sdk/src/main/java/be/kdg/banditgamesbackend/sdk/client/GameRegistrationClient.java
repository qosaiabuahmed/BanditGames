package be.kdg.banditgamesbackend.sdk.client;

import be.kdg.banditgamesbackend.sdk.config.BanditGamesSDKProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class GameRegistrationClient {
    private final RestTemplate restTemplate;
    private final BanditGamesSDKProperties properties;

    public void registerGame() {
        if (properties.getPlatformUrl() == null || properties.getPlatformUrl().isEmpty()) {
            log.warn("Platform URL not set. Skipping game registration.");
            return;
        }

        String url = properties.getPlatformUrl() + "/api/games";
        log.info("Registering game '{}' with platform at {}", properties.getGame().getName(), url);

        try {
            restTemplate.postForObject(url, properties.getGame(), Object.class);
            log.info("Successfully registered game '{}'", properties.getGame().getName());
        } catch (Exception e) {
            log.error("Failed to register game with platform: {}", e.getMessage());
        }
    }
}
