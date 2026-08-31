package be.kdg.banditgamesbackend.match.adapter.out.messaging;

import be.kdg.banditgamesbackend.common.validation.Validators;
import be.kdg.banditgamesbackend.gamemetadata.api.PlayerConfigurationInfo;

import java.util.List;
import java.util.UUID;

public record MatchAssignedEvent(
        UUID matchId,
        UUID gameId,
        List<PlayerSeat> playerSeats,
        PlayerConfigurationInfo configuration
) {
    public MatchAssignedEvent {
        Validators.requireNonNull(matchId, "matchId");
        Validators.requireNonNull(gameId, "gameId");
        Validators.requireNonEmpty(playerSeats, "playerSeats");
        Validators.requireNonNull(configuration, "configuration");
    }

    public record PlayerSeat(UUID userId, int seatNumber) {
        public PlayerSeat {
            Validators.requireNonNull(userId, "userId");
            if (seatNumber < 1) {
                throw new IllegalArgumentException("seatNumber must be at least 1");
            }
        }
    }
}
