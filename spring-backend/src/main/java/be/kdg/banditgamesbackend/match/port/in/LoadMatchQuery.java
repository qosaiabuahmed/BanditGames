package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;

import java.util.Optional;

public interface LoadMatchQuery {
    Optional<Match> getMatch(MatchId matchId);
}
