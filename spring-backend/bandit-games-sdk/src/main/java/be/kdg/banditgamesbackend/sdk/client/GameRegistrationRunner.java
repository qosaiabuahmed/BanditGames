package be.kdg.banditgamesbackend.sdk.client;

import be.kdg.banditgamesbackend.sdk.config.BanditGamesSDKProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

@Slf4j
@RequiredArgsConstructor
public class GameRegistrationRunner implements CommandLineRunner {
    private final GameRegistrationClient registrationClient;
    private final BanditGamesSDKProperties properties;

    @Override
    public void run(String... args) {
        if (properties.isAutoRegister()) {
            registrationClient.registerGame();
        } else {
            log.info("Auto-registration is disabled via properties.");
        }
    }
}
