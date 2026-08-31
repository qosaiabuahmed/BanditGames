package be.kdg.banditgamesbackend.gamemetadata.core;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatistics;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GetGameStatisticsQuery;
import be.kdg.banditgamesbackend.gamemetadata.port.out.LoadGameStatisticsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetGameStatisticsQueryImpl implements GetGameStatisticsQuery {

    private final LoadGameStatisticsPort loadGameStatisticsPort;

    public GetGameStatisticsQueryImpl(LoadGameStatisticsPort loadGameStatisticsPort) {
        this.loadGameStatisticsPort = loadGameStatisticsPort;
    }

    @Override
    @Transactional(readOnly = true)
    public GameStatistics getGameStatistics() {
        return loadGameStatisticsPort.loadGameStatistics();
    }
}