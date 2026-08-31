package be.kdg.banditgamesbackend.mlanalytics.adapter.in.web;

import be.kdg.banditgamesbackend.mlanalytics.domain.MLPredictionStats;
import be.kdg.banditgamesbackend.mlanalytics.port.in.GetMLPredictionStatsQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/ml")
public class AdminMLAnalyticsController {

    private final GetMLPredictionStatsQuery getMLPredictionStatsQuery;

    public AdminMLAnalyticsController(GetMLPredictionStatsQuery getMLPredictionStatsQuery) {
        this.getMLPredictionStatsQuery = getMLPredictionStatsQuery;
    }

    @GetMapping("/predictions/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MLPredictionStats> getMLPredictionStats() {
        log.info("Admin requested ML prediction statistics");
        MLPredictionStats stats = getMLPredictionStatsQuery.getMLPredictionStats();
        return ResponseEntity.ok(stats);
    }
}