package be.kdg.banditgamesbackend.achievement.adapter.in.web;

import be.kdg.banditgamesbackend.achievement.domain.UserAchievement;
import be.kdg.banditgamesbackend.achievement.port.in.GetUserAchievementsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final GetUserAchievementsUseCase getUserAchievementsUseCase;

    @GetMapping("/my")
    public ResponseEntity<List<AchievementDto>> getMyAchievements(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching achievements for user: {}", userId);

        List<UserAchievement> achievements = getUserAchievementsUseCase.getUserAchievements(userId);
        List<AchievementDto> dtos = achievements.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/my/game/{gameId}")
    public ResponseEntity<List<AchievementDto>> getMyAchievementsByGame(
            @PathVariable UUID gameId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching achievements for user: {} in game: {}", userId, gameId);

        List<UserAchievement> achievements = getUserAchievementsUseCase.getUserAchievementsByGame(userId, gameId);
        List<AchievementDto> dtos = achievements.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AchievementDto>> getUserAchievements(@PathVariable UUID userId) {
        log.info("Fetching achievements for user: {}", userId);

        List<UserAchievement> achievements = getUserAchievementsUseCase.getUserAchievements(userId);
        List<AchievementDto> dtos = achievements.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    private AchievementDto toDto(UserAchievement achievement) {
        return new AchievementDto(
                achievement.getUserAchievementId().id(),
                achievement.getUserId(),
                achievement.getGameId(),
                achievement.getMatchId(),
                achievement.getAchievementCode(),
                achievement.getUnlockedAt()
        );
    }
}
