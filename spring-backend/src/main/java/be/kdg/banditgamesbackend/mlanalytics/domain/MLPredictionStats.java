package be.kdg.banditgamesbackend.mlanalytics.domain;

import java.time.LocalDateTime;
import java.util.List;

public record MLPredictionStats(
    List<ModelStats> statsByModel,
    LocalDateTime firstPrediction,
    LocalDateTime lastPrediction
) {}