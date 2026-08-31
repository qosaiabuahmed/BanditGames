package be.kdg.banditgamesbackend.user.adapter.in.web;

import be.kdg.banditgamesbackend.user.domain.UserStatistics;
import be.kdg.banditgamesbackend.user.port.in.GetUserStatisticsQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final GetUserStatisticsQuery getUserStatisticsQuery;

    public AdminUserController(GetUserStatisticsQuery getUserStatisticsQuery) {
        this.getUserStatisticsQuery = getUserStatisticsQuery;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatistics> getUserStatistics() {
        log.info("Admin requested user statistics");
        UserStatistics statistics = getUserStatisticsQuery.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}