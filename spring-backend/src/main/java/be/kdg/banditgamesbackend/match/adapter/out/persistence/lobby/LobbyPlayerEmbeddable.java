package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class LobbyPlayerEmbeddable {
    private UUID userId;
    private Instant joinedAt;
}
