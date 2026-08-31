package be.kdg.banditgamesbackend.common.messaging;

import be.kdg.banditgamesbackend.common.messaging.config.DeadLetterConfig;
import be.kdg.banditgamesbackend.common.messaging.config.ExchangeConfig;
import be.kdg.banditgamesbackend.common.messaging.config.QueueConfig;
import be.kdg.banditgamesbackend.common.messaging.config.RoutingKeyConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "platform.messaging")
public class MessagingProperties {

    private ExchangeConfig exchanges = new ExchangeConfig();
    private QueueConfig queues = new QueueConfig();
    private RoutingKeyConfig routingKeys = new RoutingKeyConfig();
    private DeadLetterConfig deadLetter = new DeadLetterConfig();
}
