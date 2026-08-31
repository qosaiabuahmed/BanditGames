package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.domain.LobbyStatus;
import be.kdg.banditgamesbackend.match.port.in.SendLobbyInviteCommand;
import be.kdg.banditgamesbackend.match.port.in.SendLobbyInviteUseCase;
import be.kdg.banditgamesbackend.match.port.out.LoadLobbyInvitePort;
import be.kdg.banditgamesbackend.match.port.out.LobbyRepository;
import be.kdg.banditgamesbackend.match.port.out.SaveLobbyInvitePort;
import be.kdg.banditgamesbackend.social.api.FriendshipLookupService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SendLobbyInviteUseCaseImpl implements SendLobbyInviteUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendLobbyInviteUseCaseImpl.class);

    private final LobbyRepository lobbyRepository;
    private final FriendshipLookupService friendshipLookupService;
    private final LoadLobbyInvitePort loadLobbyInvitePort;
    private final List<SaveLobbyInvitePort> saveLobbyInvitePorts;
    private final GameLookupService gameLookupService;

    @Override
    public LobbyInviteId sendInvite(SendLobbyInviteCommand command) {
        Lobby lobby = lobbyRepository.findById(new LobbyId(command.lobbyId()))
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        if (lobby.getStatus() != LobbyStatus.OPEN) {
            throw new IllegalStateException("Cannot invite to a closed lobby");
        }

        if (lobby.getPlayers().isEmpty() ||
                !lobby.getPlayers().getFirst().userId().equals(command.fromUserId())) {
            throw new IllegalStateException("Only lobby creator can send invites");
        }

        if (!friendshipLookupService.areFriends(command.fromUserId(), command.toUserId())) {
            throw new IllegalStateException("Can only invite friends");
        }

        if (lobby.containsUser(command.toUserId())) {
            throw new IllegalStateException("User is already in the lobby");
        }

        if (loadLobbyInvitePort.findPendingInvite(
                new LobbyId(command.lobbyId()),
                command.toUserId()).isPresent()) {
            throw new IllegalStateException("Invite already sent to this user");
        }

        UUID gameId = lobby.getGameId();
        GameInfoDto game = gameLookupService.findGameById(gameId).orElseThrow(() -> new IllegalStateException("Game not found"));
        String gameName = game.name();


        LobbyInvite invite = LobbyInvite.createNew(
                new LobbyId(command.lobbyId()),
                command.fromUserId(),
                command.toUserId(),
                gameId,
                gameName
        );

        saveLobbyInvitePorts.forEach(port -> port.save(invite));
        invite.clearDomainEvents();

        log.info("User {} sent lobby invite to user {} for lobby {}",
                command.fromUserId(), command.toUserId(), command.lobbyId());

        return invite.getInviteId();
    }
}