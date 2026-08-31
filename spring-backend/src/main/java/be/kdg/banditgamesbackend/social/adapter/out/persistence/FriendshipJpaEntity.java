package be.kdg.banditgamesbackend.social.adapter.out.persistence;

import be.kdg.banditgamesbackend.social.domain.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class FriendshipJpaEntity {

    @Id
    private UUID friendshipId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID friendId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;
}
