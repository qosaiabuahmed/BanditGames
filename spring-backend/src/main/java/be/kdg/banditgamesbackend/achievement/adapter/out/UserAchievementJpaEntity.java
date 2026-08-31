package be.kdg.banditgamesbackend.achievement.adapter.out;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "user_achievements",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "game_id", "achievement_code"})
    }
)
@Getter
@Setter
public class UserAchievementJpaEntity {

    @Id
    private UUID userAchievementId;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private UUID gameId;
    
    @Column(nullable = false, length = 100)
    private String achievementCode;
    
    @Column(nullable = false)
    private UUID matchId;
    
    @Column(nullable = false)
    private LocalDateTime unlockedAt;

}
