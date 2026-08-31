package be.kdg.banditgamesbackend.user.adapter.out;

import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.out.SaveUserPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher implements SaveUserPort {

    private final ApplicationEventPublisher eventPublisher;

    public UserEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public User save(User user) {
        user.getDomainEvents().forEach(eventPublisher::publishEvent);
        return user;
    }
}