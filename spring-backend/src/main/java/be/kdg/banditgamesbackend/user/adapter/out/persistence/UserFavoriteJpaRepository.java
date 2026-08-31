package be.kdg.banditgamesbackend.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserFavoriteJpaRepository extends JpaRepository<UserFavoriteJpaEntity, UUID> {
    @Modifying
    @Query("INSERT INTO UserFavoriteJpaEntity(favoritedAt, user, gameId) " +
            "VALUES (CURRENT_TIMESTAMP, " +
            "      (SELECT u FROM UserJpaEntity u WHERE u.userId = :userId), :gameId)")
    void addFavorite(UUID userId, UUID gameId);

    @Modifying
    void removeUserFavoriteJpaEntityByUser_UserIdAndGameId(UUID userUserId, UUID gameId);

    @Query("SELECT uf.gameId FROM UserFavoriteJpaEntity uf WHERE uf.user.userId = :userId")
    List<UUID> findGameIdsByUserId(@Param("userId") UUID userId);
}
