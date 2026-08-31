package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementUnlockedEvent implements PlatformEvent {
    private String eventType = "ACHIEVEMENT_UNLOCKED";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID gameId;
    private UUID matchId;
    private UUID playerId;
    private String playerName;
    private String achievementCode;
    private String achievementDescription;
    private LocalDateTime unlockedAt;

    public AchievementUnlockedEvent(UUID gameId, UUID matchId, UUID playerId, String playerName, String achievementCode, String achievementDescription, LocalDateTime unlockedAt) {
        this.gameId = gameId;
        this.matchId = matchId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.achievementCode = achievementCode;
        this.achievementDescription = achievementDescription;
        this.unlockedAt = unlockedAt;
    }

}
