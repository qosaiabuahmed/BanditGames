package be.kdg.banditgamesbackend.match.api;

import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface("api")
public record LobbyInfo(
        UUID lobbyId,
        UUID gameId
) {
}