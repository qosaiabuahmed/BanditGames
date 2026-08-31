package be.kdg.banditgamesbackend.achievement.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TempAchievementJpaRepository extends JpaRepository<TempAchievementJpaEntity, UUID> {

    void deleteByGuestUserId(UUID guestUserId);

    List<TempAchievementJpaEntity> findByGuestUserId(UUID guestUserId);

    List<TempAchievementJpaEntity> findByGuestUserIdAndGameId(UUID guestUserId, UUID gameId);
}
