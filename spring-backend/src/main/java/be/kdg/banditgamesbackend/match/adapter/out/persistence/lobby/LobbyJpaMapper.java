package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.LobbyPlayer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LobbyJpaMapper {
    public LobbyJpaEntity toEntity(Lobby lobby) {
        LobbyJpaEntity entity = new LobbyJpaEntity();
        entity.setLobbyId(lobby.getLobbyId().value());
        entity.setGameId(lobby.getGameId());
        entity.setStatus(lobby.getStatus());
        entity.setCreatedAt(lobby.getCreatedAt());
        entity.setMatchedAt(lobby.getMatchedAt());
        entity.setMatchId(lobby.getMatchId());
        entity.setPlayers(lobby.getPlayers().stream().map(this::toEmbeddable).toList());
        return entity;
    }

    public Lobby toDomain(LobbyJpaEntity entity) {
        List<LobbyPlayer> players = entity.getPlayers().stream()
                .map(player -> new LobbyPlayer(player.getUserId(), player.getJoinedAt()))
                .toList();

        return Lobby.hydrate(
                new LobbyId(entity.getLobbyId()),
                entity.getGameId(),
                players,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getMatchedAt(),
                entity.getMatchId()
        );
    }

    private LobbyPlayerEmbeddable toEmbeddable(LobbyPlayer player) {
        LobbyPlayerEmbeddable embeddable = new LobbyPlayerEmbeddable();
        embeddable.setUserId(player.userId());
        embeddable.setJoinedAt(player.joinedAt());
        return embeddable;
    }
}
