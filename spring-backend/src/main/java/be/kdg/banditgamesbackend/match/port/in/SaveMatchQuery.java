package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.Match;

public interface SaveMatchQuery {
    void saveMatch(Match match);
}
