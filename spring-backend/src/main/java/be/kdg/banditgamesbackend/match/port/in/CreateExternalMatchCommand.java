package be.kdg.banditgamesbackend.match.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateExternalMatchCommand(
    UUID matchId,
    UUID gameId, 
    List<UUID> playerIds,
    List<String> playerNames,
    LocalDateTime startedAt
) {}
