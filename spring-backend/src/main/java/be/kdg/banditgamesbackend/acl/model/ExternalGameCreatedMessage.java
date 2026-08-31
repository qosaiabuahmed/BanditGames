package be.kdg.banditgamesbackend.acl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalGameCreatedMessage extends ExternalGameEvent {
    @JsonProperty("gameId")
    private String gameId;

    private List<String> players;

    private String whitePlayerName;
    private UUID whitePlayerId;

    private UUID blackPlayerId;
    private String blackPlayerName;

    private String currentFen;

    @JsonProperty("status")
    private String status;

    /**
     * Extract players in a flexible way - handles different game formats
     * @return List of player names (always 2 for two-player games)
     */
    public List<String> extractPlayers() {
        List<String> extractedPlayers = new ArrayList<>();

        // Strategy 1: If game sent a generic "players" array, use it
        if (players != null && !players.isEmpty()) {
            extractedPlayers.addAll(players);
        }
        // Strategy 2: If game sent Chess-style "whitePlayer"/"blackPlayer", extract them
        else if (whitePlayerName != null && blackPlayerName != null) {
            extractedPlayers.add(whitePlayerName);
            extractedPlayers.add(blackPlayerName);
        }
        // Strategy 3: Handle other naming conventions (extend as needed)

        return extractedPlayers;
    }

    public List<UUID> extractPlayerIds() {
        List<UUID> extractedPlayerIds = new ArrayList<>();
        
        // Strategy 1: If game sent a generic "players" array, use it
        if (players != null && !players.isEmpty()) {
            extractedPlayerIds.addAll(players.stream().map(UUID::fromString).toList());
        }
        // Strategy 2: If game sent Chess-style "whitePlayer"/"blackPlayer", extract them
        else if (whitePlayerId != null && blackPlayerId != null) {
            extractedPlayerIds.add(whitePlayerId);
            extractedPlayerIds.add(blackPlayerId);
        }

        return extractedPlayerIds;
    }
}
