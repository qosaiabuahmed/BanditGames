package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;
import be.kdg.banditgamesbackend.match.domain.MatchPlayer;
import be.kdg.banditgamesbackend.match.domain.MatchResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchJpaMapper {

    public MatchJpaEntity toEntity(Match match) {
        MatchJpaEntity entity = new MatchJpaEntity();
        entity.setMatchId(match.getMatchId().value());
        entity.setGameId(match.getGameId());
        entity.setStatus(match.getStatus());
        entity.setCreatedAt(match.getCreatedAt());
        entity.setStartedAt(match.getStartedAt());
        entity.setFinishedAt(match.getFinishedAt());
        entity.setPlayers(match.getPlayers().stream().map(this::toEmbeddable).toList());
        
        if (match.getResult() != null) {
            MatchResultEmbeddable resultEmbeddable = new MatchResultEmbeddable();
            resultEmbeddable.setOutcome(match.getResult().outcome());
            resultEmbeddable.setWinnerId(match.getResult().winnerId());
            resultEmbeddable.setReason(match.getResult().reason());
            entity.setResult(resultEmbeddable);
        }
        return entity;
    }

    public Match toDomain(MatchJpaEntity entity) {
        List<MatchPlayer> players = entity.getPlayers().stream()
                .map(player -> new MatchPlayer(player.getUserId(), player.getSeatNumber()))
                .toList();

        return Match.hydrate(
                new MatchId(entity.getMatchId()),
                entity.getGameId(),
                players,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                toDomain(entity.getResult())
        );
    }

    private MatchPlayerEmbeddable toEmbeddable(MatchPlayer player) {
        MatchPlayerEmbeddable embeddable = new MatchPlayerEmbeddable();
        embeddable.setUserId(player.userId());
        embeddable.setSeatNumber(player.seatNumber());
        return embeddable;
    }

    private MatchResultEmbeddable toEmbeddable(MatchResult result) {
        if (result == null) {
            return null;
        }
        MatchResultEmbeddable embeddable = new MatchResultEmbeddable();
        embeddable.setOutcome(result.outcome());
        embeddable.setWinnerId(result.winnerId());
        embeddable.setReason(result.reason());
        return embeddable;
    }

    private MatchResult toDomain(MatchResultEmbeddable embeddable) {
        if (embeddable == null || embeddable.getOutcome() == null) {
            return null;
        }
        return new MatchResult(embeddable.getOutcome(), embeddable.getWinnerId(), embeddable.getReason());
    }
}
