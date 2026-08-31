package be.kdg.banditgamesbackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.Modulith;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Modulith
@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
public class BanditGamesBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(BanditGamesBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BanditGamesBackendApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    void onApplicationStarted() {
        ApplicationModules modules = ApplicationModules.of(BanditGamesBackendApplication.class);
        modules.forEach(module -> log.info("\n{}", module));
    }
}
