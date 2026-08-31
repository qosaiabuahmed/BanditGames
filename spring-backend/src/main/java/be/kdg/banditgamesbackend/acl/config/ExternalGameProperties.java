package be.kdg.banditgamesbackend.acl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "external.game")
public class ExternalGameProperties {

    private RabbitMQConfig rabbitmq = new RabbitMQConfig();

    @Getter
    @Setter
    public static class RabbitMQConfig {
        private String host;
        private int port;
        private String username;
        private String password;
        private String exchange;
        private String queue;
        private String routingKeyPattern;
    }
}
