package be.kdg.banditgamesbackend.match.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Lobby {

    private final LobbyId lobbyId;
    private final UUID gameId;
    private final List<LobbyPlayer> players;
    private LobbyStatus status;
    private final Instant createdAt;
    private Instant matchedAt;
    private UUID matchId;

    private Lobby(LobbyId lobbyId,
                  UUID gameId,
                  List<LobbyPlayer> players,
                  LobbyStatus status,
                  Instant createdAt,
                  Instant matchedAt,
                  UUID matchId) {
        this.lobbyId = Objects.requireNonNull(lobbyId, "lobbyId is required");
        this.gameId = Objects.requireNonNull(gameId, "gameId is required");
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players is required"));
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.matchedAt = matchedAt;
        this.matchId = matchId;
    }

    public static Lobby create(UUID gameId) {
        return new Lobby(new LobbyId(UUID.randomUUID()), gameId, new ArrayList<>(), LobbyStatus.OPEN, Instant.now(), null, null);
    }

    public static Lobby hydrate(LobbyId lobbyId,
                                UUID gameId,
                                List<LobbyPlayer> players,
                                LobbyStatus status,
                                Instant createdAt,
                                Instant matchedAt,
                                UUID matchId) {
        return new Lobby(lobbyId, gameId, players, status, createdAt, matchedAt, matchId);
    }

    public List<LobbyPlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean containsUser(UUID userId) {
        return players.stream().anyMatch(player -> player.userId().equals(userId));
    }

    public boolean isFull(int maxPlayers) {
        return players.size() >= maxPlayers;
    }

    public List<UUID> getPlayerIds() {
        return players.stream().map(LobbyPlayer::userId).toList();
    }

    public void addPlayer(UUID userId, int maxPlayers) {
        if (status != LobbyStatus.OPEN) {
            throw new IllegalStateException("Cannot join a lobby that is not open");
        }
        if (containsUser(userId)) {
            return;
        }
        if (isFull(maxPlayers)) {
            throw new IllegalStateException("Lobby is already full");
        }
        players.add(new LobbyPlayer(userId, Instant.now()));
    }

    public void removePlayer(UUID userId) {
        if (status != LobbyStatus.OPEN) {
            throw new IllegalStateException("Cannot leave a lobby that is not open");
        }
        players.removeIf(player -> player.userId().equals(userId));
        if (players.isEmpty()) {
            this.status = LobbyStatus.CANCELLED;
        }
    }

    public void cancel() {
        if (status == LobbyStatus.MATCHED) {
            throw new IllegalStateException("Cannot cancel a matched lobby");
        }
        this.status = LobbyStatus.CANCELLED;
    }

    public void markMatched(UUID matchId) {
        if (status != LobbyStatus.OPEN) {
            throw new IllegalStateException("Lobby already closed");
        }
        this.status = LobbyStatus.MATCHED;
        this.matchedAt = Instant.now();
        this.matchId = Objects.requireNonNull(matchId, "matchId is required");
    }
}
