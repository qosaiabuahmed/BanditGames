package be.kdg.banditgamesbackend.match.adapter.in.response;

import be.kdg.banditgamesbackend.match.domain.MatchPlayer;

import java.util.UUID;

public record MatchPlayerResponse(UUID userId, int seatNumber) {
    public static MatchPlayerResponse from(MatchPlayer player) {
        return new MatchPlayerResponse(player.userId(), player.seatNumber());
    }
}
