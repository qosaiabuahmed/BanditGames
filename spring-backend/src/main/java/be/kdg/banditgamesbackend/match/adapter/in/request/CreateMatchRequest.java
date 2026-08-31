package be.kdg.banditgamesbackend.match.adapter.in.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateMatchRequest(
        @NotNull UUID gameId,
        @NotEmpty List<UUID> playerIds
) {
}
