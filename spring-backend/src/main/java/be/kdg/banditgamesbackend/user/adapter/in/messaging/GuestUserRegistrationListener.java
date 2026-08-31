package be.kdg.banditgamesbackend.user.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.GuestUserRegistrationRequested;
import be.kdg.banditgamesbackend.user.port.in.RegisterGuestUserCommand;
import be.kdg.banditgamesbackend.user.port.in.RegisterGuestUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestUserRegistrationListener {
    private final RegisterGuestUserUseCase  registerGuestUserUseCase;

    @EventListener
    public void handleGuestUserRegistrationRequested(GuestUserRegistrationRequested event) {
        log.info("Received Guest User Registration Request: {}", event);

        registerGuestUserUseCase.registerGuest(
                new RegisterGuestUserCommand(event.userId(), event.username())
        );
    }
}
