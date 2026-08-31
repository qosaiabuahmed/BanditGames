package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.match.domain.LobbyStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lobbies")
@Getter
@Setter
public class LobbyJpaEntity {
    @Id
    private UUID lobbyId;

    private UUID gameId;

    @Enumerated(EnumType.STRING)
    private LobbyStatus status;

    private Instant createdAt;
    private Instant matchedAt;
    private UUID matchId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lobby_players", joinColumns = @JoinColumn(name = "lobby_id"))
    @OrderColumn(name = "join_order")
    private List<LobbyPlayerEmbeddable> players = new ArrayList<>();
}
