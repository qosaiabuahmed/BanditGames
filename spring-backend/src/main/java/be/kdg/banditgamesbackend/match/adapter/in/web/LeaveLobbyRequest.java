package be.kdg.banditgamesbackend.match.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LeaveLobbyRequest(
        @NotNull UUID lobbyId,
        @NotNull UUID userId
) {
}
