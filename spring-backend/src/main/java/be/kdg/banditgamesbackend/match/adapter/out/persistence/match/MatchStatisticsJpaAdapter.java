package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.GamePlayCount;
import be.kdg.banditgamesbackend.match.domain.MatchStatistics;
import be.kdg.banditgamesbackend.match.domain.MatchStatus;
import be.kdg.banditgamesbackend.match.port.out.LoadMatchStatisticsPort;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MatchStatisticsJpaAdapter implements LoadMatchStatisticsPort {

    private final MatchJpaRepository matchJpaRepository;

    public MatchStatisticsJpaAdapter(MatchJpaRepository matchJpaRepository) {
        this.matchJpaRepository = matchJpaRepository;
    }

    @Override
    public MatchStatistics loadMatchStatistics() {
        long totalMatches = matchJpaRepository.count();

        Map<MatchStatus, Long> matchesByStatus = new EnumMap<>(MatchStatus.class);
        for (MatchStatus status : MatchStatus.values()) {
            matchesByStatus.put(status, matchJpaRepository.countByStatus(status));
        }

        Double averageDuration = matchJpaRepository.findAverageMatchDurationMinutes();

        return new MatchStatistics(
            totalMatches,
            matchesByStatus,
            averageDuration
        );
    }

    @Override
    public List<GamePlayCount> loadMostPlayedGames(int limit) {
        return matchJpaRepository.findMostPlayedGames(limit)
            .stream()
            .map(result -> new GamePlayCount(
                (UUID) result[0],
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue()
            ))
            .toList();
    }
}