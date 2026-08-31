package be.kdg.banditgamesbackend.user.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.FriendRequestSentEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SocialEventListener {

    @EventListener
    @Transactional
    public void handleFriendRequestSent(FriendRequestSentEvent event) {
        log.info("Friend request sent from {} to {}", event.fromUserId(), event.toUserId());
    }
}
