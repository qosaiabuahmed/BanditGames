package be.kdg.banditgamesbackend.mlanalytics.port.in;

import be.kdg.banditgamesbackend.mlanalytics.domain.MLPredictionStats;

public interface GetMLPredictionStatsQuery {
    MLPredictionStats getMLPredictionStats();
}