package be.kdg.banditgamesbackend.match.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class MatchTest {

    @Test
    void create_ShouldInitializeMatch() {
        MatchId matchId = new MatchId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        MatchPlayer p1 = new MatchPlayer(UUID.randomUUID(), 1);
        MatchPlayer p2 = new MatchPlayer(UUID.randomUUID(), 2);

        Match match = Match.create(matchId, gameId, List.of(p1, p2));

        assertThat(match.getMatchId()).isEqualTo(matchId);
        assertThat(match.getGameId()).isEqualTo(gameId);
        assertThat(match.getPlayers()).hasSize(2);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING_ASSIGNMENT);
    }

    @Test
    void markStarted_ShouldChangeStatusToInProgress() {
        Match match = createTestMatch();
        match.markStarted();
        assertThat(match.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match.getStartedAt()).isNotNull();
    }

    @Test
    void complete_WithResult_ShouldChangeStatusToFinished() {
        Match match = createTestMatch();
        match.markStarted();
        UUID winnerId = match.getPlayers().getFirst().userId();
        MatchResult result = new MatchResult(MatchOutcome.WIN, winnerId, "Reason");
        
        match.complete(result);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.FINISHED);
        assertThat(match.getFinishedAt()).isNotNull();
        assertThat(match.getResult()).isEqualTo(result);
    }

    @Test
    void cancel_ShouldChangeStatusToCancelled() {
        Match match = createTestMatch();
        match.cancel();
        assertThat(match.getStatus()).isEqualTo(MatchStatus.CANCELLED);
        assertThat(match.getResult().outcome()).isEqualTo(MatchOutcome.CANCELLED);
    }

    private Match createTestMatch() {
        return Match.create(
            new MatchId(UUID.randomUUID()),
            UUID.randomUUID(),
            List.of(new MatchPlayer(UUID.randomUUID(), 1), new MatchPlayer(UUID.randomUUID(), 2))
        );
    }
}
