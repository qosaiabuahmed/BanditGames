package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatistics;

public interface GetGameStatisticsQuery {
    GameStatistics getGameStatistics();
}