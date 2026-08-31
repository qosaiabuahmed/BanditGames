package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LobbyResponse(
        UUID lobbyId,
        UUID gameId,
        LobbyStatus status,
        List<LobbyPlayerResponse> players,
        Instant createdAt,
        Instant matchedAt,
        UUID matchId
) {
    public static LobbyResponse from(Lobby lobby) {
        List<LobbyPlayerResponse> playerResponses = lobby.getPlayers().stream()
                .map(LobbyPlayerResponse::from)
                .toList();

        return new LobbyResponse(
                lobby.getLobbyId().value(),
                lobby.getGameId(),
                lobby.getStatus(),
                playerResponses,
                lobby.getCreatedAt(),
                lobby.getMatchedAt(),
                lobby.getMatchId()
        );
    }
}
