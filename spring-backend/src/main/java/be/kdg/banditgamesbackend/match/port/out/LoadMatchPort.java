package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;

import java.util.Optional;

public interface LoadMatchPort {
    Optional<Match> findById(MatchId matchId);
}
