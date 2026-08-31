package be.kdg.banditgamesbackend.match.adapter.in.response;

import be.kdg.banditgamesbackend.match.domain.MatchOutcome;
import be.kdg.banditgamesbackend.match.domain.MatchResult;

import java.util.UUID;

public record MatchResultResponse(MatchOutcome outcome, UUID winnerId, String reason) {
    public static MatchResultResponse from(MatchResult result) {
        if (result == null) {
            return null;
        }
        return new MatchResultResponse(result.outcome(), result.winnerId(), result.reason());
    }
}
