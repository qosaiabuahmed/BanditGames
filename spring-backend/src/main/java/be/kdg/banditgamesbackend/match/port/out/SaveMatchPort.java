package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.Match;

public interface SaveMatchPort {
    Match save(Match match);
}
