package be.kdg.banditgamesbackend.match.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinLobbyRequest(
        @NotNull UUID gameId,
        @NotNull UUID userId
) {
}
