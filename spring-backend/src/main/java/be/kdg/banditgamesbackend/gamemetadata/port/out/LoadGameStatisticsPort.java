package be.kdg.banditgamesbackend.gamemetadata.port.out;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatistics;

public interface LoadGameStatisticsPort {
    GameStatistics loadGameStatistics();
}