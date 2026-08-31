package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.Match;

public interface CreateMatchUseCase {
    Match createMatch(CreateMatchCommand command);
}
