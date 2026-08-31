package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.common.events.GuestPlayersDetectedEvent;
import be.kdg.banditgamesbackend.common.events.GuestUserRegistrationRequested;
import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import be.kdg.banditgamesbackend.gamemetadata.api.PlayerConfigurationInfo;
import be.kdg.banditgamesbackend.match.domain.*;
import be.kdg.banditgamesbackend.match.port.in.CreateExternalMatchCommand;
import be.kdg.banditgamesbackend.match.port.in.MatchLifecycleService;
import be.kdg.banditgamesbackend.match.port.out.LoadMatchPort;
import be.kdg.banditgamesbackend.match.port.out.SaveMatchPort;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MatchLifecycleServiceImpl implements MatchLifecycleService {

    private final LoadMatchPort loadMatchPort;
    private final SaveMatchPort saveMatchPort;
    private final GameLookupService gameLookupService;
    private final UserLookupService userLookupService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void start(String gameId, Object payload) {
        Optional<UUID> matchId = extractMatchId(payload);
        if (matchId.isEmpty()) {
            log.warn("Cannot start match for gameId={} because payload missing matchId. payload={}", gameId, payload);
            return;
        }
        loadMatchPort.findById(new MatchId(matchId.get()))
                .ifPresentOrElse(
                        match -> {
                            match.markStarted();
                            saveMatchPort.save(match);
                            log.info("Marked match {} as started for gameId={}", match.getMatchId(), gameId);
                        },
                        () -> log.warn("Received start for unknown matchId={} gameId={}", matchId.get(), gameId)
                );
    }

    @Override
    public void finish(String gameId, Object payload) {
        Optional<UUID> matchId = extractMatchId(payload);
        if (matchId.isEmpty()) {
            log.warn("Cannot finish match for gameId={} because payload missing matchId. payload={}", gameId, payload);
            return;
        }
        MatchResult result = extractResult(payload);
        loadMatchPort.findById(new MatchId(matchId.get()))
                .ifPresentOrElse(
                        match -> {
                            match.complete(result);
                            saveMatchPort.save(match);
                            log.info("Marked match {} as finished with outcome {} for gameId={}", match.getMatchId(), result.outcome(), gameId);
                        },
                        () -> log.warn("Received finish for unknown matchId={} gameId={}", matchId.get(), gameId)
                );
    }

    private Optional<UUID> extractMatchId(Object payload) {
        if (payload instanceof Map<?, ?> payloadMap && payloadMap.containsKey("matchId")) {
            try {
                Object value = payloadMap.get("matchId");
                if (value instanceof String stringValue) {
                    return Optional.of(UUID.fromString(stringValue));
                }
                if (value instanceof UUID uuid) {
                    return Optional.of(uuid);
                }
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid matchId in payload: {}", valueOrNull(payloadMap.get("matchId")));
            }
        }
        return Optional.empty();
    }

    private MatchResult extractResult(Object payload) {
        if (payload instanceof Map<?, ?> payloadMap) {
            MatchOutcome outcome = parseOutcome(payloadMap.get("outcome"));
            UUID winnerId = parseUuid(payloadMap.get("winnerId"));
            String reason = payloadMap.get("reason") != null ? payloadMap.get("reason").toString() : null;
            if (outcome != null) {
                return new MatchResult(outcome, winnerId, reason);
            }
        }
        return new MatchResult(MatchOutcome.ERROR, null, "Unknown outcome from payload");
    }

    private MatchOutcome parseOutcome(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return MatchOutcome.valueOf(raw.toString().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown outcome value received: {}", raw);
            return null;
        }
    }

    private UUID parseUuid(Object raw) {
        if (raw instanceof UUID uuid) {
            return uuid;
        }
        if (raw instanceof String text && !text.isBlank()) {
            try {
                return UUID.fromString(text);
            } catch (IllegalArgumentException ignored) {
                log.warn("Invalid UUID string: {}", text);
            }
        }
        return null;
    }

    private Object valueOrNull(Object value) {
        return value == null ? "null" : value;
    }

    @Override
    public void createExternalMatch(CreateExternalMatchCommand command) {
        ensureUsersExist(command);

        MatchId matchId = new MatchId(command.matchId());

        GameInfoDto game = gameLookupService.findGameById(command.gameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found for id " + command.gameId()));

        PlayerConfigurationInfo configuration = game.playerConfiguration();

        List<UUID> distinctPlayerIds = new ArrayList<>(new LinkedHashSet<>(command.playerIds()));

        int playerCount = distinctPlayerIds.size();
        if (playerCount < configuration.minPlayers() || playerCount > configuration.maxPlayers()) {
            throw new IllegalArgumentException("Player count %d outside supported range %d-%d for game %s"
                    .formatted(playerCount, configuration.minPlayers(), configuration.maxPlayers(), game.name()));
        }

        List<MatchPlayer> players = IntStream.range(0, distinctPlayerIds.size())
                .mapToObj(i -> new MatchPlayer(distinctPlayerIds.get(i), i + 1))
                .toList();

        log.info("External match {} started for game {} with {} players", matchId, game.name(), players.size());
        Match match = Match.createInProgress(
            matchId,
            command.gameId(),
            players,
            command.startedAt());

        saveMatchPort.save(match);
    }

    private void ensureUsersExist(CreateExternalMatchCommand command) {
        for (int i = 0; i < command.playerIds().size(); i++) {
            UUID playerId = command.playerIds().get(i);

            if (userLookupService.userExists(playerId)) {
                log.debug("Player {} exists", playerId);
            } else {
                String playerName = extractPlayerName(command, i);

                applicationEventPublisher.publishEvent(new GuestUserRegistrationRequested(playerId, playerName));
                log.info("Auto-created guest user: {} ({})", playerName, playerId);
            }
        }

        notifyUserIfGuest(command);
    }

    private void notifyUserIfGuest(CreateExternalMatchCommand command) {
        List<UUID> guestPlayers = command.playerIds().stream()
                .filter(userLookupService::isGuestUser)
                .toList();

        if (!guestPlayers.isEmpty()) {
            applicationEventPublisher.publishEvent(new GuestPlayersDetectedEvent(
                    command.matchId(),
                    guestPlayers
            ));
        }
    }

    private String extractPlayerName(CreateExternalMatchCommand command, int playerIndex) {
        if (command.playerNames() != null && playerIndex < command.playerNames().size()) {
            String name = command.playerNames().get(playerIndex);
            if (name != null && !name.isBlank()) {
                return name;
            }
        }

        UUID playerId = command.playerIds().get(playerIndex);
        return "Guest_" + playerId.toString().substring(0, 8);
    }

}
