package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.match.domain.GamePlayCount;
import be.kdg.banditgamesbackend.match.port.in.GetMostPlayedGamesQuery;
import be.kdg.banditgamesbackend.match.port.out.LoadMatchStatisticsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetMostPlayedGamesQueryImpl implements GetMostPlayedGamesQuery {

    private final LoadMatchStatisticsPort loadMatchStatisticsPort;

    public GetMostPlayedGamesQueryImpl(LoadMatchStatisticsPort loadMatchStatisticsPort) {
        this.loadMatchStatisticsPort = loadMatchStatisticsPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GamePlayCount> getMostPlayedGames(int limit) {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        return loadMatchStatisticsPort.loadMostPlayedGames(limit);
    }
}