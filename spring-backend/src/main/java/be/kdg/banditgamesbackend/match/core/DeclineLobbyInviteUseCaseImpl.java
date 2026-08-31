package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.port.in.DeclineLobbyInviteCommand;
import be.kdg.banditgamesbackend.match.port.in.DeclineLobbyInviteUseCase;
import be.kdg.banditgamesbackend.match.port.out.LoadLobbyInvitePort;
import be.kdg.banditgamesbackend.match.port.out.SaveLobbyInvitePort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DeclineLobbyInviteUseCaseImpl implements DeclineLobbyInviteUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeclineLobbyInviteUseCaseImpl.class);

    private final LoadLobbyInvitePort loadLobbyInvitePort;
    private final List<SaveLobbyInvitePort> saveLobbyInvitePorts;

    @Override
    public void declineInvite(DeclineLobbyInviteCommand command) {
        LobbyInvite invite = loadLobbyInvitePort.findById(
                        new LobbyInviteId(command.inviteId()))
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));

        if (!invite.getToUserId().equals(command.userId())) {
            throw new IllegalStateException("This invite is not for you");
        }

        invite.decline();

        saveLobbyInvitePorts.forEach(port -> port.save(invite));
        invite.clearDomainEvents();

        log.info("User {} declined invite {} for lobby {}",
                command.userId(), command.inviteId(), invite.getLobbyId().value());
    }
}