package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatistics;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus;
import be.kdg.banditgamesbackend.gamemetadata.port.out.LoadGameStatisticsPort;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class GameStatisticsJpaAdapter implements LoadGameStatisticsPort {

    private final GameJpaRepository gameJpaRepository;

    public GameStatisticsJpaAdapter(GameJpaRepository gameJpaRepository) {
        this.gameJpaRepository = gameJpaRepository;
    }

    @Override
    public GameStatistics loadGameStatistics() {
        long totalGames = gameJpaRepository.count();

        Map<GameStatus, Long> gamesByStatus = new EnumMap<>(GameStatus.class);
        for (GameStatus status : GameStatus.values()) {
            gamesByStatus.put(status, gameJpaRepository.countByStatus(status));
        }

        List<GameStatistics.GameSummary> recentGames = gameJpaRepository.findRecentGames(10)
            .stream()
            .map(entity -> new GameStatistics.GameSummary(
                entity.getGameId(),
                entity.getName(),
                entity.getStatus(),
                entity.getRegisteredAt()
            ))
            .toList();

        return new GameStatistics(
            totalGames,
            gamesByStatus,
            recentGames
        );
    }
}