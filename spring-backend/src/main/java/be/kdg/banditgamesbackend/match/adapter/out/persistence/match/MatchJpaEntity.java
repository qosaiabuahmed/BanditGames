package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.MatchStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
public class MatchJpaEntity {
    @Id
    private UUID matchId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Embedded
    private MatchResultEmbeddable result;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_players", joinColumns = @JoinColumn(name = "match_id"))
    @OrderColumn(name = "seat_order")
    private List<MatchPlayerEmbeddable> players = new ArrayList<>();
}
