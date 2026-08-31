package be.kdg.banditgamesbackend.match.api;

import org.springframework.modulith.NamedInterface;

import java.util.Optional;
import java.util.UUID;

@NamedInterface("api")
public interface LobbyLookupService {
    Optional<LobbyInfo> findLobbyById(UUID lobbyId);
}