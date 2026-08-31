package be.kdg.banditgamesbackend.mlanalytics.core;

import be.kdg.banditgamesbackend.mlanalytics.domain.MLPredictionStats;
import be.kdg.banditgamesbackend.mlanalytics.port.in.GetMLPredictionStatsQuery;
import be.kdg.banditgamesbackend.mlanalytics.port.out.LoadMLPredictionsPort;
import org.springframework.stereotype.Service;

@Service
public class GetMLPredictionStatsQueryImpl implements GetMLPredictionStatsQuery {

    private final LoadMLPredictionsPort loadMLPredictionsPort;

    public GetMLPredictionStatsQueryImpl(LoadMLPredictionsPort loadMLPredictionsPort) {
        this.loadMLPredictionsPort = loadMLPredictionsPort;
    }

    @Override
    public MLPredictionStats getMLPredictionStats() {
        return loadMLPredictionsPort.loadMLPredictionStats();
    }
}