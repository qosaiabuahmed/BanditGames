package be.kdg.banditgamesbackend.acl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ExternalGameRabbitMQConfig {

    private final ExternalGameProperties props;

    public ExternalGameRabbitMQConfig(ExternalGameProperties props) {
        this.props = props;
    }

    @Bean(name = "externalGameConnectionFactory")
    public ConnectionFactory externalGameConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(props.getRabbitmq().getHost());
        factory.setPort(props.getRabbitmq().getPort());
        factory.setUsername(props.getRabbitmq().getUsername());
        factory.setPassword(props.getRabbitmq().getPassword());
        return factory;
    }

    @Bean(name = "externalGameRabbitTemplate")
    public RabbitTemplate externalGameRabbitTemplate(
            @Qualifier("externalGameConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    @Bean(name = "externalGameListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory externalGameListenerContainerFactory(
            @Qualifier("externalGameConnectionFactory") ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(250);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);

        factory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 10000)
                        .build()
        );
        return factory;
    }

    @Bean(name = "externalGameRabbitAdmin")
    public RabbitAdmin externalGameRabbitAdmin(
            @Qualifier("externalGameConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(false);
        return admin;
    }

    /**
     * Manually declare queue, exchange, and binding after beans are created
     */
    @Bean
    public InitializingBean externalGameInfrastructureInitializer(
            @Qualifier("externalGameRabbitAdmin") RabbitAdmin admin
    ) {
        return () -> {

            log.info("Declaring external game infrastructure on Chess RabbitMQ (port 5673)...");

            TopicExchange exchange = new TopicExchange(props.getRabbitmq().getExchange(), true, false);
            admin.declareExchange(exchange);
            log.info("Exchange declared: {}", props.getRabbitmq().getExchange());

            Queue queue = new Queue(props.getRabbitmq().getQueue(), true, false, false);
            admin.declareQueue(queue);
            log.info("Queue declared: {}", props.getRabbitmq().getQueue());

            Binding binding = BindingBuilder.bind(queue).to(exchange).with("#");
            admin.declareBinding(binding);
            log.info("Binding declared: {} -> {} (#)", props.getRabbitmq().getQueue(), props.getRabbitmq().getExchange());

            log.info("External game infrastructure fully declared!");
        };
    }
}
