package be.kdg.banditgamesbackend.match.adapter.out.persistence.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipProjectionJpaRepository extends JpaRepository<FriendshipProjectionJpaEntity, UUID> {

    @Query("SELECT f FROM FriendshipProjectionJpaEntity f " +
           "WHERE (f.userId1 = :user1 AND f.userId2 = :user2) " +
           "OR (f.userId1 = :user2 AND f.userId2 = :user1)")
    Optional<FriendshipProjectionJpaEntity> findFriendship(@Param("user1") UUID userId1,
                                                            @Param("user2") UUID userId2);

    @Modifying
    @Query("DELETE FROM FriendshipProjectionJpaEntity f " +
           "WHERE (f.userId1 = :user1 AND f.userId2 = :user2) " +
           "OR (f.userId1 = :user2 AND f.userId2 = :user1)")
    void deleteFriendship(@Param("user1") UUID userId1, @Param("user2") UUID userId2);
}