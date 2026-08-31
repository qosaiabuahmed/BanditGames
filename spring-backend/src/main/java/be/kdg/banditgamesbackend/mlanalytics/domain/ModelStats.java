package be.kdg.banditgamesbackend.mlanalytics.domain;

public record ModelStats(
    String predictionType,  // "policy" or "value"
    String modelType,       // "cnn" or "lr"
    long totalPredictions,
    double avgResponseTimeMs
) {}