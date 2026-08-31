package be.kdg.banditgamesbackend.match.adapter.in.messaging;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import be.kdg.banditgamesbackend.common.events.MatchEndedEvent;
import be.kdg.banditgamesbackend.common.events.MatchStartedEvent;
import be.kdg.banditgamesbackend.common.events.MatchStateUpdatedEvent;
import be.kdg.banditgamesbackend.common.events.MoveMadeEvent;
import be.kdg.banditgamesbackend.common.events.PlatformEvent;
import be.kdg.banditgamesbackend.common.events.PlayerJoinedEvent;
import be.kdg.banditgamesbackend.common.events.PlayerLeftEvent;
import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;
import be.kdg.banditgamesbackend.match.domain.MatchOutcome;
import be.kdg.banditgamesbackend.match.domain.MatchResult;
import be.kdg.banditgamesbackend.match.port.in.CreateExternalMatchCommand;
import be.kdg.banditgamesbackend.match.port.in.LoadMatchQuery;
import be.kdg.banditgamesbackend.match.port.in.MatchLifecycleService;
import be.kdg.banditgamesbackend.match.port.in.SaveMatchQuery;
import be.kdg.banditgamesbackend.user.api.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchLifecycleEventListener {

    private final MatchLifecycleService matchLifecycleService;
    private final UserLookupService userLookupService;
    private final LoadMatchQuery loadMatchQuery;
    private final SaveMatchQuery saveMatchQuery;

    @RabbitListener(queues = "${platform.messaging.queues.match-events}")
    public void handleMatchEvent(PlatformEvent event) {
        log.info("Match event received: {}", event.getEventType());

        switch (event) {
            case MatchStartedEvent e -> handleMatchStarted(e);
            case MatchEndedEvent e -> handleMatchEnded(e);
            case MatchStateUpdatedEvent e -> handleMatchStateUpdated(e);
            default -> log.warn("Unknown match event type: {}", event.getEventType());
        }
    }

    private void handleMatchStarted(MatchStartedEvent event) {
        log.info("Platform received MatchStartedEvent: matchId={}, players={} vs {}"
                , event.getMatchId(), event.getPlayerNames().getFirst(), event.getPlayerNames().get(1));

        try {
            CreateExternalMatchCommand command = new CreateExternalMatchCommand(
                event.getMatchId(),
                event.getGameId(),
                event.getPlayerIds(),
                event.getPlayerNames(),
                event.getStartTime() != null ? event.getStartTime() : LocalDateTime.now()
            );

            matchLifecycleService.createExternalMatch(command);
            log.info("Match {} marked as IN_PROGRESS", event.getMatchId());
        } catch (IllegalStateException e) {
            log.error("User lookup failed for MatchStartedEvent: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process MatchStartedEvent: {}", event.getMatchId(), e);
            throw e;
        }
    }

    private void handleMatchEnded(MatchEndedEvent event) {
        log.info("Platform received MatchCompletedEvent: matchId={}, winner={}, outcome={}", 
            event.getMatchId(), event.getWinner(), event.getOutcome());

        try {
            Match match = loadMatchQuery.getMatch(new MatchId(event.getMatchId()))
                .orElseThrow(() -> new IllegalStateException("Match not found: " + event.getMatchId()));
            
            UUID winnerId = null;
            if (event.getWinner() != null) {
                Optional<UUID> winnerOpt = userLookupService.findUserIdByUsername(event.getWinner());
                if (winnerOpt.isPresent()) {
                    winnerId = winnerOpt.get();
                } else {
                    log.warn("Winner user '{}' not found", event.getWinner());
                }
            }
            
            MatchOutcome outcome = MatchOutcome.valueOf(event.getOutcome());
            
            MatchResult result = new MatchResult(outcome, winnerId, event.getEndReason());
            match.complete(result);
            
            saveMatchQuery.saveMatch(match);
            
            log.info("Match {} completed: outcome={}, winner={}, reason={}", 
                event.getMatchId(), outcome, winnerId, event.getEndReason());
            
        } catch (Exception e) {
            log.error("Failed to process MatchCompletedEvent: {}", event.getMatchId(), e);
            throw e;
        }


    }

    private void handleMatchStateUpdated(MatchStateUpdatedEvent event) {
        log.info("Match state updated: matchId={}, gameId={}, state={}, status={}", 
            event.getMatchId(), event.getGameId(), event.getGameState(), event.getStatus());

        loadMatchQuery.getMatch(new MatchId(event.getMatchId()))
                .orElseThrow(() -> new IllegalStateException("Match not found: " + event.getMatchId()));

        // WebSocket broadcasting for real-time updates not implemented
    }

    @RabbitListener(queues = "${platform.messaging.queues.player-actions}")
    public void handlePlayerAction(PlatformEvent event) {
        log.info("Received player action event: {}", event.getEventType());

        switch (event) {
            case MoveMadeEvent moveEvent -> handleMoveMade(moveEvent);
            case PlayerJoinedEvent joinEvent -> handlePlayerJoined(joinEvent);
            case PlayerLeftEvent leftEvent -> handlePlayerLeft(leftEvent);
            default -> log.warn("Unhandled player action event type: {}", event.getEventType());
        }
    }

    private void handleMoveMade(MoveMadeEvent event) {
        log.info("Move made by player {} in game {}: move #{}",
                event.getPlayerName(), event.getGameId(), event.getMoveNumber());

        if (!userLookupService.userExists(event.getPlayerId())) {
            throw new IllegalStateException("User not found: " + event.getPlayerId());
        }

        loadMatchQuery.getMatch(new MatchId(event.getMatchId()))
                .orElseThrow(() -> new IllegalStateException("Match not found: " + event.getMatchId()));

        // WebSocket broadcasting for real-time spectating not implemented
        log.debug("Move details: {} -> {}, board state: {}",
        event.getFromSquare(), event.getToSquare(), event.getBoardStateAfter());
    }

    private void handlePlayerJoined(PlayerJoinedEvent event) {
        log.info("Player {} joined match {}", event.getPlayerId(), event.getMatchId());
    }

    private void handlePlayerLeft(PlayerLeftEvent event) {
        log.info("Player {} left match {} (reason: {})",
                event.getPlayerId(), event.getMatchId(), event.getReason());
    }


}
