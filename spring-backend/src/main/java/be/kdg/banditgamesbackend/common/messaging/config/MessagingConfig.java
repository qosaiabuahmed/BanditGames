package be.kdg.banditgamesbackend.common.messaging.config;

import be.kdg.banditgamesbackend.common.messaging.MessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class MessagingConfig {

    private final MessagingProperties properties;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(properties.getDeadLetter().exchange(), true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(properties.getDeadLetter().queue())
                .build();
    }

    @Bean
    public Binding deadLetterBinding(
        @Qualifier("deadLetterQueue") Queue queue,
        @Qualifier("deadLetterExchange") DirectExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getDeadLetter().routingKey());
    }

    @Bean("platformEventsExchange")
    public TopicExchange platformEventsExchange() {
        return new TopicExchange(properties.getExchanges().platformEvents(), true, false);
    }

    @Bean("gameEventsExchange")
    public TopicExchange gameEventsExchange() {
        return new TopicExchange(properties.getExchanges().gameEvents(), true, false);
    }

    @Bean("platformCommandsExchange")
    public TopicExchange platformCommandsExchange() {
        return new TopicExchange(properties.getExchanges().platformCommands(), true, false);
    }

    private Map<String, Object> queueArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getDeadLetter().exchange());
        args.put("x-dead-letter-routing-key", properties.getDeadLetter().routingKey());
        args.put("x-message-ttl", properties.getDeadLetter().messageTtl());
        return args;
    }

    @Bean
    public Queue gameLifecycleQueue() {
        return QueueBuilder
                .durable(properties.getQueues().gameLifecycle())
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue gameRegistrationQueue() {
        return QueueBuilder
                .durable(properties.getQueues().gameRegistration())
                .withArguments(queueArguments())
                .build();
    }


    @Bean
    public Queue playerActionsQueue() {
        return QueueBuilder
                .durable(properties.getQueues().playerActions())
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue achievementsQueue() {
        return QueueBuilder
                .durable(properties.getQueues().achievements())
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue matchEventsQueue() {
        return QueueBuilder
                .durable(properties.getQueues().matchEvents())
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue lobbyEventsQueue() {
        return QueueBuilder
                .durable(properties.getQueues().lobbyEvents())
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue userEventsQueue() {
        return QueueBuilder
                .durable(properties.getQueues().userEvents())
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Binding bindAllGameRegistrations(
        @Qualifier("gameRegistrationQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with("game.*.registered");
    }

    @Bean
    public Binding moveMadeBinding(
        @Qualifier("playerActionsQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().moveMade());
    }

    @Bean
    public Binding playerJoinedBinding(
        @Qualifier("playerActionsQueue") Queue queue,
        @Qualifier("platformEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().playerJoined());
    }

    @Bean
    public Binding playerLeftBinding(
        @Qualifier("playerActionsQueue") Queue queue,
        @Qualifier("platformEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().playerLeft());
    }

    @Bean
    public Binding achievementUnlockedBinding(
        @Qualifier("achievementsQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().achievementUnlocked());
    }

    @Bean
    public Binding matchCreatedBinding(
        @Qualifier("matchEventsQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().matchCreated());
    }

    @Bean
    public Binding matchStartedBinding(
        @Qualifier("matchEventsQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().matchStarted());
    }

    @Bean
    public Binding matchStateUpdatedBinding(
        @Qualifier("matchEventsQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().matchStateUpdated());
    }

    @Bean
    public Binding matchCompletedBinding(
        @Qualifier("matchEventsQueue") Queue queue,
        @Qualifier("gameEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().matchCompleted());
    }

    @Bean
    public Binding lobbyCreatedBinding(
        @Qualifier("lobbyEventsQueue") Queue queue,
        @Qualifier("platformEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().lobbyCreated());
    }

    @Bean
    public Binding lobbyReadyBinding(
        @Qualifier("lobbyEventsQueue") Queue queue,
        @Qualifier("platformEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().lobbyReady());
    }

    @Bean
    public Binding userCreatedBinding(
        @Qualifier("userEventsQueue") Queue queue,
        @Qualifier("platformEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().userCreated());
    }

    @Bean
    public Binding userUpdatedBinding(
        @Qualifier("userEventsQueue") Queue queue,
        @Qualifier("platformEventsExchange") TopicExchange exchange
    ) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.getRoutingKeys().userUpdated());
    }

}
