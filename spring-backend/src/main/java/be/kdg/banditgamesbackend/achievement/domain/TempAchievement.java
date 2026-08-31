package be.kdg.banditgamesbackend.achievement.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TempAchievement {
    private final UUID id;
    private final UUID guestUserId;
    private final String achievementCode;
    private final UUID gameId;
    private final UUID matchId;
    private final LocalDateTime unlockedAt;
    
    public TempAchievement(UUID guestUserId, String achievementCode,
                           UUID gameId, UUID matchId, LocalDateTime unlockedAt) {
        this.id = UUID.randomUUID();
        this.guestUserId = guestUserId;
        this.achievementCode = achievementCode;
        this.gameId = gameId;
        this.matchId = matchId;
        this.unlockedAt = unlockedAt;
    }
}
