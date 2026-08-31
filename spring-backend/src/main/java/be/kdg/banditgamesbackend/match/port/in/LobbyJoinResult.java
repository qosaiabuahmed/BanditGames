package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.Lobby;

import java.util.Objects;
import java.util.Optional;

public record LobbyJoinResult(Lobby lobby, Optional<Match> createdMatch) {
    public LobbyJoinResult {
        Objects.requireNonNull(lobby, "lobby is required");
        createdMatch = createdMatch == null ? Optional.empty() : createdMatch;
    }
}
