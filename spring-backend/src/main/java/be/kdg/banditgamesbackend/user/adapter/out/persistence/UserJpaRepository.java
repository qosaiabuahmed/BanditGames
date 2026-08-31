package be.kdg.banditgamesbackend.user.adapter.out.persistence;

import be.kdg.banditgamesbackend.user.domain.UserStatus;
import be.kdg.banditgamesbackend.user.domain.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByUsername(String username);

    boolean existsByUserTypeContains(UserType userType);

    List<UserJpaEntity> findByGuestExpiresAtAfter(LocalDateTime guestExpiresAtAfter);

    boolean existsByUserTypeIs(UserType userType);

    long countByStatus(UserStatus status);

    @Query(value = """
        SELECT CAST(u.registered_at AS DATE) as date, COUNT(*) as count
        FROM users u
        WHERE u.registered_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY CAST(u.registered_at AS DATE)
        ORDER BY date DESC
        """, nativeQuery = true)
    List<Object[]> findRegistrationTrendLast30Days();
}