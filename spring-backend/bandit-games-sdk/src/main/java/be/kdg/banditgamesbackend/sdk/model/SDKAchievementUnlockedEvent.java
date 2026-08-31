package be.kdg.banditgamesbackend.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SDKAchievementUnlockedEvent {
    private static final String EVENT_TYPE = "ACHIEVEMENT_UNLOCKED";
    private final LocalDateTime timestamp = LocalDateTime.now();
    private UUID gameId;
    private UUID matchId;
    private UUID playerId;
    private String playerName;
    private String achievementCode;
    private String achievementDescription;
    private final LocalDateTime unlockedAt = LocalDateTime.now();
}
