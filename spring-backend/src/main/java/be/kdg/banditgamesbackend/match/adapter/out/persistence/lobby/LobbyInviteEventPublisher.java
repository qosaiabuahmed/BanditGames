package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.port.out.SaveLobbyInvitePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LobbyInviteEventPublisher implements SaveLobbyInvitePort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public LobbyInvite save(LobbyInvite invite) {
        invite.getDomainEvents().forEach(applicationEventPublisher::publishEvent);
        return invite;
    }
}