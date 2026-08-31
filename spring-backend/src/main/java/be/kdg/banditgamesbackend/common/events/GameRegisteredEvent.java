package be.kdg.banditgamesbackend.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRegisteredEvent implements PlatformEvent {
    private String eventType = "GAME_REGISTERED";
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private UUID gameId;
    private String name;
    private String description;
    private String status;
    private LocalDateTime registeredAt;

    private List<AchievementInfo> achievements;
    private MetaDataInfo metaData;
    private GameRulesInfo rules;
    private PlayerConfigurationInfo playerConfigurations;
    private String renderType;


    /**
    * For developers to register their games internally on the platform
    * */
    public GameRegisteredEvent(UUID gameId, String name, String description, String status, LocalDateTime registeredAt, String renderType) {
        this.gameId = gameId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.registeredAt = registeredAt;
        this.renderType = renderType;
    }

    /**
     * For external games to register on the platform
     * */
    public GameRegisteredEvent(
            UUID gameId,
            String name,
            String description,
            List<AchievementInfo> achievements,
            MetaDataInfo metaDataInfo,
            GameRulesInfo gameRulesInfo,
            PlayerConfigurationInfo playerConfigurationInfo,
            LocalDateTime registeredAt,
            String renderType
    ) {
        this.gameId = gameId;
        this.name = name;
        this.description = description;
        this.achievements = achievements;
        this.metaData = metaDataInfo;
        this.rules = gameRulesInfo;
        this.playerConfigurations = playerConfigurationInfo;
        this.registeredAt = registeredAt;
        this.renderType = renderType;
    }

    public record AchievementInfo(
            String code,
            String description
    ) {}

    public record MetaDataInfo(
            String category,
            String theme,
            String designer,
            String publisher,
            int releaseYear,
            int estimatedDurationMinutes,
            String complexity,
            String frontendUrl,
            String pictureUrl
    ) {}

    public record GameRulesInfo(
            String rulesText,
            String rulesUrl,
            String summary
    ) {}

    public record PlayerConfigurationInfo(
            int minPlayers,
            int maxPlayers,
            Set<String> supportedPlayerTypes
    ) {}
}
