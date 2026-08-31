package be.kdg.banditgamesbackend.match.domain;

import java.util.Objects;
import java.util.UUID;

public record MatchResult(MatchOutcome outcome, UUID winnerId, String reason) {
    public MatchResult {
        Objects.requireNonNull(outcome, "outcome is required");
    }
}
