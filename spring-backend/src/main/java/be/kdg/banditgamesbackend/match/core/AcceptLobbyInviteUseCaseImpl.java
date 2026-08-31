package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.domain.LobbyStatus;
import be.kdg.banditgamesbackend.match.port.in.*;
import be.kdg.banditgamesbackend.match.port.out.LoadLobbyInvitePort;
import be.kdg.banditgamesbackend.match.port.out.LobbyRepository;
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
public class AcceptLobbyInviteUseCaseImpl implements AcceptLobbyInviteUseCase {

    private static final Logger log = LoggerFactory.getLogger(AcceptLobbyInviteUseCaseImpl.class);

    private final LoadLobbyInvitePort loadLobbyInvitePort;
    private final List<SaveLobbyInvitePort> saveLobbyInvitePorts;
    private final JoinLobbyUseCase joinLobbyUseCase;
    private final LobbyRepository lobbyRepository;

    @Override
    public LobbyJoinResult acceptInvite(AcceptLobbyInviteCommand command) {
        LobbyInvite invite = loadLobbyInvitePort.findById(
                        new LobbyInviteId(command.inviteId()))
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));

        if (!invite.getToUserId().equals(command.userId())) {
            throw new IllegalStateException("This invite is not for you");
        }

        Lobby lobby = lobbyRepository.findById(invite.getLobbyId())
                .orElseThrow(() -> new IllegalStateException("Lobby no longer exists"));

        if (lobby.getStatus() != LobbyStatus.OPEN) {
            throw new IllegalStateException("Lobby is no longer open");
        }

        invite.accept();

        saveLobbyInvitePorts.forEach(port -> port.save(invite));
        invite.clearDomainEvents();

        log.info("User {} accepted invite {} for lobby {}",
                command.userId(), command.inviteId(), invite.getLobbyId().value());

        JoinLobbyCommand joinCommand = new JoinLobbyCommand(
                lobby.getGameId(),
                command.userId()
        );
        LobbyJoinResult result = joinLobbyUseCase.joinLobby(joinCommand);

        return result;
    }
}