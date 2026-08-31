package be.kdg.banditgamesbackend.match.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class LobbyTest {

    @Test
    void create_ShouldInitializeOpenLobby() {
        UUID gameId = UUID.randomUUID();
        Lobby lobby = Lobby.create(gameId);

        assertThat(lobby.getLobbyId()).isNotNull();
        assertThat(lobby.getGameId()).isEqualTo(gameId);
        assertThat(lobby.getPlayers()).isEmpty();
        assertThat(lobby.getStatus()).isEqualTo(LobbyStatus.OPEN);
    }

    @Test
    void addPlayer_WhenOpen_ShouldAddPlayer() {
        Lobby lobby = Lobby.create(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        
        lobby.addPlayer(userId, 2);

        assertThat(lobby.getPlayers()).hasSize(1);
        assertThat(lobby.containsUser(userId)).isTrue();
    }

    @Test
    void addPlayer_WhenFull_ShouldThrowException() {
        Lobby lobby = Lobby.create(UUID.randomUUID());
        lobby.addPlayer(UUID.randomUUID(), 1);

        UUID newPLayerId = UUID.randomUUID();
        assertThatThrownBy(() -> lobby.addPlayer(newPLayerId, 1))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removePlayer_ShouldUpdateStatusIfEmpty() {
        Lobby lobby = Lobby.create(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        lobby.addPlayer(userId, 2);
        
        lobby.removePlayer(userId);

        assertThat(lobby.getPlayers()).isEmpty();
        assertThat(lobby.getStatus()).isEqualTo(LobbyStatus.CANCELLED);
    }

    @Test
    void markMatched_ShouldChangeStatusAndSetMatchId() {
        Lobby lobby = Lobby.create(UUID.randomUUID());
        UUID matchId = UUID.randomUUID();
        
        lobby.markMatched(matchId);

        assertThat(lobby.getStatus()).isEqualTo(LobbyStatus.MATCHED);
        assertThat(lobby.getMatchId()).isEqualTo(matchId);
        assertThat(lobby.getMatchedAt()).isNotNull();
    }

    @Test
    void cancel_ShouldChangeStatus() {
        Lobby lobby = Lobby.create(UUID.randomUUID());
        lobby.cancel();
        assertThat(lobby.getStatus()).isEqualTo(LobbyStatus.CANCELLED);
    }
}
