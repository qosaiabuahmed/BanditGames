package be.kdg.banditgamesbackend.mlanalytics.port.out;

import be.kdg.banditgamesbackend.mlanalytics.domain.MLPredictionStats;

public interface LoadMLPredictionsPort {
    MLPredictionStats loadMLPredictionStats();
}