package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.UserStatistics;
import be.kdg.banditgamesbackend.user.port.in.GetUserStatisticsQuery;
import be.kdg.banditgamesbackend.user.port.out.LoadUserStatisticsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserStatisticsQueryImpl implements GetUserStatisticsQuery {

    private final LoadUserStatisticsPort loadUserStatisticsPort;

    public GetUserStatisticsQueryImpl(LoadUserStatisticsPort loadUserStatisticsPort) {
        this.loadUserStatisticsPort = loadUserStatisticsPort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        return loadUserStatisticsPort.loadUserStatistics();
    }
}