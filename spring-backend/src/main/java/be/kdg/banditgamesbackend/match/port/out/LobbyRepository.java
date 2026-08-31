package be.kdg.banditgamesbackend.match.port.out;

import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LobbyRepository {
    Lobby save(Lobby lobby);

    Optional<Lobby> findOpenLobbyByGameId(UUID gameId);

    Optional<Lobby> findById(LobbyId lobbyId);

    Optional<Lobby> findOpenLobbyByUser(UUID userId);

    List<Lobby> findOpenLobbiesByGameId(UUID gameId);
}
