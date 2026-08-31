package be.kdg.banditgamesbackend.sdk.config;

import be.kdg.banditgamesbackend.sdk.model.SDKRenderType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "banditgames.sdk")
public class BanditGamesSDKProperties {
    /**
     * Platform backend URL (e.g., http://localhost:8083)
     */
    private String platformUrl;

    /**
     * Whether to automatically register the game on startup
     */
    private boolean autoRegister = true;

    /**
     * Game registration metadata
     */
    @NestedConfigurationProperty
    private GameRegistrationProperties game = new GameRegistrationProperties();

    @Data
    public static class GameRegistrationProperties {
        private String name;
        private String description;
        private SDKRenderType renderType = SDKRenderType.EXTERNAL_IFRAME;
        private GameRulesProperties rules = new GameRulesProperties();
        private List<AchievementProperties> achievements = new ArrayList<>();
        private PlayerConfigProperties playerConfiguration = new PlayerConfigProperties();
        private MetaDataProperties metaData = new MetaDataProperties();
    }

    @Data
    public static class GameRulesProperties {
        private String rulesText;
        private String rulesUrl;
        private String summary;
    }

    @Data
    public static class AchievementProperties {
        private String achievementId;
        private String name;
        private String description;
        private String iconUrl;
        private String condition;
    }

    @Data
    public static class PlayerConfigProperties {
        private int minPlayers = 2;
        private int maxPlayers = 2;
        private Set<String> supportedPlayerTypes = Set.of("HUMAN");
    }

    @Data
    public static class MetaDataProperties {
        private String category;
        private String theme;
        private String designer;
        private String publisher;
        private int releaseYear;
        private int estimatedDurationMinutes;
        private String complexity;
    }
}
