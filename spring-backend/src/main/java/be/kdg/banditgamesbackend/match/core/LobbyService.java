package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.exceptions.AlreadyInLobbyException;
import be.kdg.banditgamesbackend.match.port.in.CreateMatchCommand;
import be.kdg.banditgamesbackend.match.port.in.JoinLobbyCommand;
import be.kdg.banditgamesbackend.match.port.in.JoinLobbyUseCase;
import be.kdg.banditgamesbackend.match.port.in.LeaveLobbyCommand;
import be.kdg.banditgamesbackend.match.port.in.LeaveLobbyUseCase;
import be.kdg.banditgamesbackend.match.port.in.LobbyJoinResult;
import be.kdg.banditgamesbackend.match.port.in.LobbyQueryUseCase;
import be.kdg.banditgamesbackend.match.port.out.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LobbyService implements JoinLobbyUseCase, LobbyQueryUseCase, LeaveLobbyUseCase {

    private static final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private static final Duration STALE_LOBBY_AGE = Duration.ofMinutes(10);

    private final LobbyRepository lobbyRepository;
    private final GameLookupService gameLookupService;
    private final MatchmakingService matchmakingService;
    private final Random random = new SecureRandom();

    @Override
    public LobbyJoinResult joinLobby(JoinLobbyCommand command) {
        GameInfoDto game = gameLookupService.findGameById(command.gameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found for id " + command.gameId()));

        int maxPlayers = game.playerConfiguration().maxPlayers();

        enforceSingleLobby(command.userId());
        Lobby lobby = selectLobby(command.gameId(), maxPlayers, command.userId());
        lobby.addPlayer(command.userId(), maxPlayers);

        Match createdMatch = null;
        if (lobby.isFull(maxPlayers)) {
            createdMatch = matchmakingService.createMatch(new CreateMatchCommand(lobby.getGameId(), lobby.getPlayerIds()));
            lobby.markMatched(createdMatch.getMatchId().value());
            log.info("Lobby {} matched into match {} for game {}", lobby.getLobbyId(), createdMatch.getMatchId(), game.name());
        }

        Lobby savedLobby = lobbyRepository.save(lobby);
        return new LobbyJoinResult(savedLobby, Optional.ofNullable(createdMatch));
    }

    @Override
    public Optional<Lobby> getLobby(LobbyId lobbyId) {
        return lobbyRepository.findById(lobbyId);
    }

    @Override
    public Lobby leaveLobby(LeaveLobbyCommand command) {
        Lobby lobby = lobbyRepository.findById(new LobbyId(command.lobbyId()))
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found for id " + command.lobbyId()));
        lobby.removePlayer(command.userId());
        Lobby savedLobby = lobbyRepository.save(lobby);
        log.info("User {} left lobby {}. Status now {}", command.userId(), savedLobby.getLobbyId(), savedLobby.getStatus());
        return savedLobby;
    }

    private void enforceSingleLobby(UUID userId) {
        lobbyRepository.findOpenLobbyByUser(userId).ifPresent(existing -> {
            throw new AlreadyInLobbyException(existing.getLobbyId().value());
        });
    }

    private Lobby selectLobby(UUID gameId, int maxPlayers, UUID userId) {
        Instant cutoff = Instant.now().minus(STALE_LOBBY_AGE);
        List<Lobby> openLobbies = new ArrayList<>(lobbyRepository.findOpenLobbiesByGameId(gameId));

        List<Lobby> candidates = new ArrayList<>();
        for (Lobby lobby : openLobbies) {
            if (lobby.getCreatedAt().isBefore(cutoff)) {
                lobby.cancel();
                lobbyRepository.save(lobby);
                continue;
            }
            if (!lobby.isFull(maxPlayers) && !lobby.containsUser(userId)) {
                candidates.add(lobby);
            }
        }

        if (candidates.isEmpty()) {
            return Lobby.create(gameId);
        }

        Collections.shuffle(candidates, random);
        return candidates.getFirst();
    }
}
