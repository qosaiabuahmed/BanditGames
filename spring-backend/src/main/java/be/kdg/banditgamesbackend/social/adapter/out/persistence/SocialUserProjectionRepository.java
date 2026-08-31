package be.kdg.banditgamesbackend.social.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialUserProjectionRepository extends JpaRepository<SocialUserProjectionJpaEntity, UUID> {

    boolean existsByUserId(UUID userId);
    List<SocialUserProjectionJpaEntity> findByUsernameContainingIgnoreCase(String username);
    List<SocialUserProjectionJpaEntity> findByemailContainingIgnoreCase(String email);
    List<SocialUserProjectionJpaEntity> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    Optional<SocialUserProjectionJpaEntity> findByEmail(String email);
}
