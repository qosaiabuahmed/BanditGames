package be.kdg.banditgamesbackend.social.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipJpaEntity, UUID> {

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE " +
            "(f.userId = :userId AND f.friendId = :friendId) OR " +
            "(f.userId = :friendId AND f.friendId = :userId)")
    Optional<FriendshipJpaEntity> findByUserAndFriend(
            @Param("userId") UUID userId,
            @Param("friendId") UUID friendId
    );

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE " +
            "(f.userId = :userId OR f.friendId = :userId) AND " +
            "f.status = 'ACCEPTED'")
    List<FriendshipJpaEntity> findAcceptedFriendshipsByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE " +
            "f.friendId = :userId AND f.status = 'PENDING'")
    List<FriendshipJpaEntity> findPendingRequestsForUser(@Param("userId") UUID userId);

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE " +
            "f.userId = :userId AND f.status = 'PENDING'")
    List<FriendshipJpaEntity> findSendRequestsByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM FriendshipJpaEntity f WHERE " +
            "((f.userId = :userId AND f.friendId = :friendId) OR " +
            " (f.userId = :friendId AND f.friendId = :userId)) AND " +
            "f.status = 'ACCEPTED'")
    boolean areFriends(@Param("userId") UUID userId, @Param("friendId") UUID friendId);
}
