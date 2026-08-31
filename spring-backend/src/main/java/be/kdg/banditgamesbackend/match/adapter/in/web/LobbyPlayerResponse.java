package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.domain.LobbyPlayer;

import java.time.Instant;
import java.util.UUID;

public record LobbyPlayerResponse(UUID userId, Instant joinedAt) {
    public static LobbyPlayerResponse from(LobbyPlayer player) {
        return new LobbyPlayerResponse(player.userId(), player.joinedAt());
    }
}
