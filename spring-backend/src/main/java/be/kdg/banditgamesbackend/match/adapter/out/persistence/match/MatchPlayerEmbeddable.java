package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
public class MatchPlayerEmbeddable {
    private UUID userId;
    private int seatNumber;
}
