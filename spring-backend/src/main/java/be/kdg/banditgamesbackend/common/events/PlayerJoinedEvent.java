package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerJoinedEvent implements PlatformEvent {
    private String eventType = "PLAYER_JOINED";
    private LocalDateTime timestamp = LocalDateTime.now();

    private UUID matchId;
    private UUID playerId;
    private String playerName;
}
