package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.GamePlayCount;
import be.kdg.banditgamesbackend.match.domain.MatchStatistics;

import java.util.List;

public interface LoadMatchStatisticsPort {
    MatchStatistics loadMatchStatistics();
    List<GamePlayCount> loadMostPlayedGames(int limit);
}