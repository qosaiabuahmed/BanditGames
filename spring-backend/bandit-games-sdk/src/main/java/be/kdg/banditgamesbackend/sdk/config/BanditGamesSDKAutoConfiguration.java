package be.kdg.banditgamesbackend.sdk.config;

import be.kdg.banditgamesbackend.sdk.client.GameRegistrationClient;
import be.kdg.banditgamesbackend.sdk.client.GameRegistrationRunner;
import be.kdg.banditgamesbackend.sdk.messaging.BanditGamesEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(BanditGamesSDKProperties.class)
@ConditionalOnClass({RestTemplate.class, RabbitTemplate.class})
public class BanditGamesSDKAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate banditGamesRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public GameRegistrationClient gameRegistrationClient(RestTemplate restTemplate, BanditGamesSDKProperties properties) {
        return new GameRegistrationClient(restTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GameRegistrationRunner gameRegistrationRunner(GameRegistrationClient registrationClient, BanditGamesSDKProperties properties) {
        return new GameRegistrationRunner(registrationClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BanditGamesEventPublisher banditGamesEventPublisher(RabbitTemplate rabbitTemplate) {
        return new BanditGamesEventPublisher(rabbitTemplate);
    }
}
