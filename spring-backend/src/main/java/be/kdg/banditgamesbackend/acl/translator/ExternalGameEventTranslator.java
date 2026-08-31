package be.kdg.banditgamesbackend.acl.translator;

import be.kdg.banditgamesbackend.acl.config.ChessGameDefaults;
import be.kdg.banditgamesbackend.acl.model.*;
import be.kdg.banditgamesbackend.common.events.*;
import be.kdg.banditgamesbackend.common.validation.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalGameEventTranslator {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String WHITE = "WHITE";
    private static final String BLACK = "BLACK";
    private static final String GAME_ID = "gameId";

    public PlatformEvent translate(ExternalGameEvent externalEvent) {
        Validators.requireNonNull(externalEvent, externalEvent.getMessageType().toString() + " event");

        log.debug("Translating external event: {}", externalEvent.getMessageType());

        return switch (externalEvent) {
            case ExternalGameRegisteredMessage e -> translateGameRegistered(e);
            case ExternalGameCreatedMessage e -> translateToMatchStarted(e);
            case ExternalMoveMadeMessage e -> translateMoveMade(e);
            case ExternalGameEndedMessage e -> translateToMatchEnded(e);
            case ExternalAchievementMessage e -> translateAchievement(e);
            case ExternalGameUpdatedMessage e -> translateMatchStateUpdated(e);
            default -> {
                log.warn("Unknown external event type: {}", externalEvent.getMessageType());
                yield null;
            }
        };
    }

    private GameRegisteredEvent translateGameRegistered(ExternalGameRegisteredMessage external) {
        Validators.requireNonBlank(external.getRegistrationId(), "RegistrationId");
        Validators.requireNonBlank(external.getFrontendUrl(), "FrontendUrl");

        log.debug("Found frontendUrl: {}, pictureUrl: {}", external.getFrontendUrl(), external.getPictureUrl());

        log.info("Translating GAME_REGISTERED: gameId {}", external.getRegistrationId());

        try {
            List<GameRegisteredEvent.AchievementInfo> achievements = external.getAvailableAchievements().stream()
                    .map(dto -> new GameRegisteredEvent.AchievementInfo(
                            dto.getCode(),
                            dto.getDescription()
                    )).toList();

            GameRegisteredEvent.MetaDataInfo metaData = new GameRegisteredEvent.MetaDataInfo(
                    ChessGameDefaults.CATEGORY,
                    ChessGameDefaults.THEME,
                    ChessGameDefaults.DESIGNER,
                    ChessGameDefaults.PUBLISHER,
                    ChessGameDefaults.RELEASE_YEAR,
                    ChessGameDefaults.PLAY_TIME_MINUTES,
                    ChessGameDefaults.COMPLEXITY,
                    external.getFrontendUrl(),
                    external.getPictureUrl()
            );

            GameRegisteredEvent.GameRulesInfo rules = new GameRegisteredEvent.GameRulesInfo(
                    ChessGameDefaults.RULES_TEXT,
                    ChessGameDefaults.RULES_URL,
                    ChessGameDefaults.SUMMARY
            );

            GameRegisteredEvent.PlayerConfigurationInfo playerConfigs = new GameRegisteredEvent.PlayerConfigurationInfo(
                    ChessGameDefaults.MIN_PLAYERS,
                    ChessGameDefaults.MAX_PLAYERS,
                    Set.of("HUMAN")
            );

            UUID gameId = parseUuid(external.getRegistrationId(), "GAME_REGISTERED");

            log.debug("Metadata: {}", metaData);

            return new GameRegisteredEvent(
                    gameId,
                    ChessGameDefaults.NAME,
                    ChessGameDefaults.DESCRIPTION,
                    achievements,
                    metaData,
                    rules,
                    playerConfigs,
                    parseTimestamp(external.getTimestamp()),
                    "EXTERNAL_IFRAME");

        } catch (Exception e) {
            log.error("[ACL] Translation failed for game: {}", external.getRegistrationId(), e);
            throw new AclTranslationException("Failed to translate external game registration", e);
        }
    }

    private MatchStartedEvent translateToMatchStarted(ExternalGameCreatedMessage external) {
        Validators.requireNonBlank(external.getGameId(), GAME_ID);
        Validators.requireNonEmpty(external.extractPlayers(), "Players");

        log.info("Translating GAME_CREATED: matchId {}", external.getGameId());

        List<UUID> playerIds = external.extractPlayerIds();
        List<String> players = external.extractPlayers();
        if (playerIds.size() < 2 || players.size() < 2) {
            throw new IllegalArgumentException(
                    "GAME_CREATED must have exactly 2 players, found Ids: %d, players: %d"
                            .formatted(playerIds.size(), players.size())
            );
        }

        log.info("Translating GAME_CREATED: matchId {}, players={} vs {}",
                external.getGameId(), players.getFirst(), players.get(1));

        UUID matchId = parseUuid(external.getGameId(), "GAME_CREATED");
        UUID gameId = lookupPlatformGameId();

        return new MatchStartedEvent(
                matchId,
                gameId,
                playerIds,
                players,
                external.getCurrentFen(),  // Use FEN as game state
                external.getStatus(),
                parseTimestamp(external.getTimestamp())
        );
    }

    private MatchEndedEvent translateToMatchEnded(ExternalGameEndedMessage external) {
        Validators.requireNonBlank(external.getGameId(), GAME_ID);
        Validators.requireNonBlank(external.getEndReason(), "EndReason");
        Validators.requireNonBlank(external.getWinner(), "Winner");

        log.info("Translating GAME_ENDED: matchId {}", external.getGameId());

        UUID gameId = lookupPlatformGameId();
        UUID matchId = parseUuid(external.getGameId(), "GAME_ENDED");
        String winnerUsername = null;
        String outcome;

        if ("DRAW".equalsIgnoreCase(external.getWinner())) {
            outcome = "DRAW";
        } else {
            outcome = "WIN";

            if (WHITE.equalsIgnoreCase(external.getWinner())) {
                winnerUsername = external.getWhitePlayerName();
            } else if (BLACK.equalsIgnoreCase(external.getWinner())) {
                winnerUsername = external.getBlackPlayerName();
            }
        }
        log.info("Translating GAME_ENDED: matchId {}, winner={}, reason={}",
                matchId, winnerUsername, external.getEndReason());


        return new MatchEndedEvent(
                gameId,
                matchId,
                outcome,
                external.getEndReason(),
                winnerUsername,
                Map.of(), // No score details from Chess
                external.getFinalFen(),
                external.getTotalMoves(),
                parseTimestamp(external.getTimestamp())
        );
    }

    private MatchStateUpdatedEvent translateMatchStateUpdated(ExternalGameUpdatedMessage external) {
        log.info("Translating GAME_UPDATED: matchId={}, updateType={}",
                external.getGameId(), external.getUpdateType());

        Validators.requireNonBlank(external.getGameId(), GAME_ID);
        UUID matchId = parseUuid(external.getGameId(), "GAME_UPDATED");
        UUID gameId = lookupPlatformGameId();

        return new MatchStateUpdatedEvent(
                matchId,
                gameId,
                external.getCurrentFen(),
                external.getStatus(),
                parseTimestamp(external.getTimestamp())
        );

    }

    private MoveMadeEvent translateMoveMade(ExternalMoveMadeMessage external) {
        Validators.requireNonBlank(external.getGameId(), GAME_ID);
        Validators.requireNonNull(external.getMoveNumber(), "MoveNumber");

        log.info("Translating MOVE_MADE: matchId={}, moveNumber={}, notation={}",
                external.getGameId(), external.getMoveNumber(), external.getSanNotation());

        UUID matchId = parseUuid(external.getGameId(), "MOVE_MADE");
        UUID gameId = lookupPlatformGameId();

        // Resolve player: Chess sends "WHITE" or "BLACK", map to player name
        String playerUsername = WHITE.equalsIgnoreCase(external.getPlayer())
                ? external.getWhitePlayerName()
                : external.getBlackPlayerName();

        UUID playerId = WHITE.equalsIgnoreCase(external.getPlayer())
                ? external.getWhitePlayerId()
                : external.getBlackPlayerId();

        log.debug("Player {} played", playerUsername);

        return new MoveMadeEvent(
                matchId,
                gameId,
                playerId,
                playerUsername,
                external.getMoveNumber(),
                external.getFromSquare(),
                external.getToSquare(),
                external.getSanNotation(),
                external.getFenAfterMove(),
                parseTimestamp(external.getMoveTime())
        );

    }

    private AchievementUnlockedEvent translateAchievement(ExternalAchievementMessage external) {
        log.info("Translating ACHIEVEMENT_ACQUIRED: gameId={}, player={}, achievement={}",
                external.getGameId(), external.getPlayerName(), external.getAchievementType());

        Validators.requireNonBlank(external.getGameId(), GAME_ID);
        Validators.requireNonBlank(external.getPlayerId(), "PlayerId");
        Validators.requireNonBlank(external.getAchievementType(), "AchievementType");
        Validators.requireNonBlank(external.getDescription(), "Description");

        UUID matchId = parseUuid(external.getGameId(), "ACHIEVEMENT_UNLOCKED"); // message from chess uses gameId as platforms matchId
        UUID playerId = null;
        String playerName = external.getPlayerName();

        if (external.getPlayerId() != null && !external.getPlayerId().isBlank()) {
            try {
                playerId = UUID.fromString(external.getPlayerId());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid playerId UUID: {}, falling back to playerName lookup",
                        external.getPlayerId());
                if (playerName == null || playerName.isBlank()) {
                    throw new IllegalArgumentException("Cannot resolve user: no playerId or playerName provided");
                }
            }
        }

        UUID gameId = lookupPlatformGameId();


        return new AchievementUnlockedEvent(
                gameId, // actual gameId not sent from Chess so using hardcoded UUID for now
                matchId,
                playerId,
                playerName,
                external.getAchievementType(),
                external.getDescription(),
                parseTimestamp(external.getTimestamp())
        );
    }

    private UUID lookupPlatformGameId() {
        // Chess game UUID - registry service for multiple external games to be implemented in future
        return UUID.fromString("8496c496-a884-48ed-9bb3-7c3aa50fb8ca");
    }

    private UUID parseUuid(String gameId, String context) {
        try {
            return UUID.fromString(gameId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID '{}' in {}", gameId, context);
            return lookupPlatformGameId(); // Fallback to hard-coded
        }
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, ISO_FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

}
