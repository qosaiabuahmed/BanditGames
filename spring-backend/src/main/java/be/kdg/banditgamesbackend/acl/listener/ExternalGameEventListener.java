package be.kdg.banditgamesbackend.acl.listener;

import be.kdg.banditgamesbackend.acl.model.ExternalGameEvent;
import be.kdg.banditgamesbackend.acl.publisher.AclEventPublisher;
import be.kdg.banditgamesbackend.acl.translator.AclTranslationException;
import be.kdg.banditgamesbackend.acl.translator.ExternalGameEventTranslator;
import be.kdg.banditgamesbackend.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalGameEventListener {

    private final ExternalGameEventTranslator translator;
    private final AclEventPublisher aclPublisher;
    private final ObjectMapper objectMapper;

    /**
     * ACL Entry Point: Receives events from external game RabbitMQ
     * Translates External game domain → platform domain
     */
    @RabbitListener(
            queues = "${external.game.rabbitmq.queue}",
            containerFactory = "externalGameListenerContainerFactory"
    )
    public void handleExternalGameEvent(Message message) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("ACL received raw message: {}", json);

            ExternalGameEvent externalEvent = deserializeExternalEvent(json);

            if (externalEvent == null) {
                log.warn("Could not deserialize external event, skipping...");
                return;
            }

            PlatformEvent platformEvent = translator.translate(externalEvent);

            if (platformEvent == null) {
                log.warn("Translation returned null for event type: {}", externalEvent.getMessageType());
                return;
            }

            aclPublisher.publishToPlatform(platformEvent);

            log.info("ACL successfully transformed: {} -> {}",
                    externalEvent.getMessageType(), platformEvent.getEventType());
        } catch (Exception ex) {
            log.error("ACL failed to process message", ex);
            throw new AclTranslationException("ACL translation failure", ex);
        }
    }

    private ExternalGameEvent deserializeExternalEvent(String json) {
        try {
            return objectMapper.readValue(json, ExternalGameEvent.class);
        } catch (Exception ex) {
            log.error("Deserialization of external event failed", ex);
            return null;
        }
    }
}
