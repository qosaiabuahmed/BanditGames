package be.kdg.banditgamesbackend.match.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Getter
@AllArgsConstructor
public class Match {
    private final MatchId matchId;
    private final UUID gameId;
    private final List<MatchPlayer> players;
    private MatchStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private MatchResult result;

    public static Match create(MatchId matchId, UUID gameId, List<MatchPlayer> players) {
        return new Match(
                matchId,
                gameId,
                players,
                MatchStatus.PENDING_ASSIGNMENT,
                LocalDateTime.now(),
                null,
                null,
                null);
    }

    public static Match hydrate(MatchId id,
                                UUID gameId,
                                List<MatchPlayer> players,
                                MatchStatus status,
                                LocalDateTime createdAt,
                                LocalDateTime startedAt,
                                LocalDateTime finishedAt,
                                MatchResult result) {
        return new Match(id, gameId, players, status, createdAt, startedAt, finishedAt, result);
    }

    // Used mainly for external games
    public static Match createInProgress(MatchId matchId, UUID gameId, List<MatchPlayer> players, LocalDateTime startedAt) {
        return new Match(
            matchId,
            gameId,
            players,
            MatchStatus.IN_PROGRESS,
            LocalDateTime.now(),
            startedAt,
            null,
            null
        );
    }

    public List<MatchPlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void start(LocalDateTime startedAt) {
        if (this.status != MatchStatus.WAITING) {
            throw new IllegalStateException(
                    "Match can only be started from WAITING status. Current status: " + this.status
            );
        }
        this.status = MatchStatus.IN_PROGRESS;
        this.startedAt = startedAt;
    }

    public void markStarted() {
        if (status == MatchStatus.CANCELLED || status == MatchStatus.FINISHED) {
            throw new IllegalStateException("Cannot start a finished or cancelled match");
        }
        this.status = MatchStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(MatchResult matchResult) {
        if (this.status == MatchStatus.FINISHED) {
            log.warn("Match {} already finished", this.matchId);
            return;
        }

        if (this.status != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete an IN_PROGRESS match");
        }

        this.status = MatchStatus.FINISHED;
        this.finishedAt = LocalDateTime.now();
        this.result = Objects.requireNonNull(matchResult, "matchResult is required");
    }

    public void cancel() {
        if (status == MatchStatus.FINISHED) {
            throw new IllegalStateException("Cannot cancel a finished match");
        }
        this.status = MatchStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
        this.result = new MatchResult(MatchOutcome.CANCELLED, null, "Cancelled");
    }
}
