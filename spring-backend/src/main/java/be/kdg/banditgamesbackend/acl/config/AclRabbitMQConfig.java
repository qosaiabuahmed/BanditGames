package be.kdg.banditgamesbackend.acl.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AclRabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String platformHost;

    @Value("${spring.rabbitmq.port}")
    private int platformPort;

    @Value("${spring.rabbitmq.username}")
    private String platformUsername;

    @Value("${spring.rabbitmq.password}")
    private String platformPassword;

    @Bean(name = "aclPlatformConnectionFactory")
    public ConnectionFactory platformConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(platformHost);
        factory.setPort(platformPort);
        factory.setUsername(platformUsername);
        factory.setPassword(platformPassword);
        return factory;
    }
    
    @Bean(name = "aclToPlatformRabbitTemplate")
    public RabbitTemplate aclToPlatformRabbitTemplate(
            @Qualifier("platformConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

}
