package be.kdg.banditgamesbackend.gamemetadata.adapter.in.web;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatistics;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GetGameStatisticsQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/games")
public class AdminGameController {

    private final GetGameStatisticsQuery getGameStatisticsQuery;

    public AdminGameController(GetGameStatisticsQuery getGameStatisticsQuery) {
        this.getGameStatisticsQuery = getGameStatisticsQuery;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GameStatistics> getGameStatistics() {
        log.info("Admin requested game statistics");
        GameStatistics statistics = getGameStatisticsQuery.getGameStatistics();
        return ResponseEntity.ok(statistics);
    }
}