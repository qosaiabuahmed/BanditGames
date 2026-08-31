package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.match.domain.MatchStatistics;
import be.kdg.banditgamesbackend.match.port.in.GetMatchStatisticsQuery;
import be.kdg.banditgamesbackend.match.port.out.LoadMatchStatisticsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMatchStatisticsQueryImpl implements GetMatchStatisticsQuery {

    private final LoadMatchStatisticsPort loadMatchStatisticsPort;

    public GetMatchStatisticsQueryImpl(LoadMatchStatisticsPort loadMatchStatisticsPort) {
        this.loadMatchStatisticsPort = loadMatchStatisticsPort;
    }

    @Override
    @Transactional(readOnly = true)
    public MatchStatistics getMatchStatistics() {
        return loadMatchStatisticsPort.loadMatchStatistics();
    }
}