package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.domain.GamePlayCount;
import be.kdg.banditgamesbackend.match.domain.MatchStatistics;
import be.kdg.banditgamesbackend.match.port.in.GetMatchStatisticsQuery;
import be.kdg.banditgamesbackend.match.port.in.GetMostPlayedGamesQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/matches")
public class AdminMatchController {

    private final GetMatchStatisticsQuery getMatchStatisticsQuery;
    private final GetMostPlayedGamesQuery getMostPlayedGamesQuery;

    public AdminMatchController(
            GetMatchStatisticsQuery getMatchStatisticsQuery,
            GetMostPlayedGamesQuery getMostPlayedGamesQuery) {
        this.getMatchStatisticsQuery = getMatchStatisticsQuery;
        this.getMostPlayedGamesQuery = getMostPlayedGamesQuery;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchStatistics> getMatchStatistics() {
        log.info("Admin requested match statistics");
        MatchStatistics statistics = getMatchStatisticsQuery.getMatchStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/most-played-games")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GamePlayCount>> getMostPlayedGames(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Admin requested most played games with limit: {}", limit);
        List<GamePlayCount> mostPlayed = getMostPlayedGamesQuery.getMostPlayedGames(limit);
        return ResponseEntity.ok(mostPlayed);
    }
}