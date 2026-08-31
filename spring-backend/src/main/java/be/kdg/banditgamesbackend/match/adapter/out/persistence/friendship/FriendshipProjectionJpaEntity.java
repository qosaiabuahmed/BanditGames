package be.kdg.banditgamesbackend.match.adapter.out.persistence.friendship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_friendship_projections",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id_1", "user_id_2"}))
@Getter
@Setter
public class FriendshipProjectionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id_1", nullable = false)
    private UUID userId1;

    @Column(name = "user_id_2", nullable = false)
    private UUID userId2;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected FriendshipProjectionJpaEntity() {
    }

    public FriendshipProjectionJpaEntity(UUID userId1, UUID userId2, LocalDateTime createdAt) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.createdAt = createdAt;
    }
}