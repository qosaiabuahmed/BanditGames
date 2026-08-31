package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.MatchStatistics;

public interface GetMatchStatisticsQuery {
    MatchStatistics getMatchStatistics();
}