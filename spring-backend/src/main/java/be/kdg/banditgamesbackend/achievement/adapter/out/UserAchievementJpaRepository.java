package be.kdg.banditgamesbackend.achievement.adapter.out;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAchievementJpaRepository extends JpaRepository<UserAchievementJpaEntity, UUID> {

    boolean existsByUserIdAndGameIdAndAchievementCode(
        UUID userId, 
        UUID gameId, 
        String achievementCode
    );
    
    List<UserAchievementJpaEntity> findByUserIdAndGameId(UUID userId, UUID gameId);
    
    List<UserAchievementJpaEntity> findByUserId(UUID userId);
    
    @Query("SELECT COUNT(ua) FROM UserAchievementJpaEntity ua " +
           "WHERE ua.userId = :userId AND ua.gameId = :gameId")
    long countByUserAndGame(@Param("userId") UUID userId, @Param("gameId") UUID gameId);

}
