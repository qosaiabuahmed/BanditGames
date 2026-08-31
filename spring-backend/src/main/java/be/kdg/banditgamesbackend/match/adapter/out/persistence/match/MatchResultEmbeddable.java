package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.MatchOutcome;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
public class MatchResultEmbeddable {
    @Enumerated(EnumType.STRING)
    private MatchOutcome outcome;
    private UUID winnerId;
    private String reason;
}
