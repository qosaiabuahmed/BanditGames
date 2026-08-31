package be.kdg.banditgamesbackend.match.adapter.in.response;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MatchResponse(
        UUID matchId,
        UUID gameId,
        MatchStatus status,
        List<MatchPlayerResponse> players,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        MatchResultResponse result
) {
    public static MatchResponse from(Match match) {
        List<MatchPlayerResponse> playerResponses = match.getPlayers().stream()
                .map(MatchPlayerResponse::from)
                .toList();
        return new MatchResponse(
                match.getMatchId().value(),
                match.getGameId(),
                match.getStatus(),
                playerResponses,
                match.getCreatedAt(),
                match.getStartedAt(),
                match.getFinishedAt(),
                MatchResultResponse.from(match.getResult())
        );
    }
}
