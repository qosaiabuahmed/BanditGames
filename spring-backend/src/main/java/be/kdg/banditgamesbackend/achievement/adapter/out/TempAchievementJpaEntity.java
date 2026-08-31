package be.kdg.banditgamesbackend.achievement.adapter.out;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "temp_user_achievements",
    uniqueConstraints = {@UniqueConstraint(
            columnNames = {"guest_user_id", "game_id", "achievement_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempAchievementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tempAchievementId;

    @Column(nullable = false)
    private UUID guestUserId;

    @Column(nullable = false)
    private UUID gameId;

    @Column(nullable = false, length = 100)
    private String achievementCode;

    @Column(nullable = false)
    private UUID matchId;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;
}
