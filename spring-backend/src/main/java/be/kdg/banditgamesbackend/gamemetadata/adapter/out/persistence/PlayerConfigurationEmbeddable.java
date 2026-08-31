package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PlayerConfigurationEmbeddable {
    private int minPlayers;
    private int maxPlayers;
    private static final int DEFAULT_MAX_PLAYERS = 2;

    @ElementCollection(targetClass = PlayerTypeEmbeddable.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "player_type")
    private Set<PlayerTypeEmbeddable> supportedPlayerTypes = new HashSet<>();

    public enum PlayerTypeEmbeddable {
        HUMAN,
        AI
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers != null ? maxPlayers : DEFAULT_MAX_PLAYERS;
    }
}
