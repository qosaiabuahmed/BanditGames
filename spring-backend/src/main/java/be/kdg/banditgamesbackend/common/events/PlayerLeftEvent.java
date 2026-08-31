package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLeftEvent implements PlatformEvent {
    private String eventType = "PLAYER_LEFT";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID matchId;
    private UUID playerId;
    private String reason; // DISCONNECTED, QUIT, TIMEOUT
}
