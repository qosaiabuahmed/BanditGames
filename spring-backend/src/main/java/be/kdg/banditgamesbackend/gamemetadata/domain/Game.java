package be.kdg.banditgamesbackend.gamemetadata.domain;

import be.kdg.banditgamesbackend.common.events.GameRegisteredEvent;
import be.kdg.banditgamesbackend.common.validation.Validators;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Game {
    private final GameId gameId;
    private final String description;
    private final List<AchievementDefinition> achievements;
    private final LocalDateTime registeredAt;
    private final RenderType renderType;
    private final List<Object> domainEvents = new ArrayList<>();
    private String name;
    private GameRules rules;
    private GameMetaData metaData;
    private GameStatus status;
    private PlayerConfiguration playerConfiguration;

    private Game(GameId gameId,
                 String name,
                 String description,
                 GameRules rules,
                 List<AchievementDefinition> achievements,
                 LocalDateTime registeredAt,
                 GameMetaData metaData,
                 GameStatus status,
                 PlayerConfiguration playerConfiguration,
                 RenderType renderType) {

        this.gameId = Objects.requireNonNull(gameId);
        this.name = name;
        this.description = description;
        this.rules = rules;
        this.achievements = achievements == null ? new ArrayList<>() : new ArrayList<>(achievements);
        this.registeredAt = Objects.requireNonNull(registeredAt);
        this.metaData = metaData;
        this.status = Objects.requireNonNull(status);
        this.playerConfiguration = playerConfiguration;
        this.renderType = renderType;
    }

    public static Game register(GameId gameId,
                                String name,
                                String description,
                                GameRules rules,
                                List<AchievementDefinition> achievementDefinition,
                                PlayerConfiguration configuration,
                                RenderType renderType,
                                GameMetaData metaData) {
        LocalDateTime now = LocalDateTime.now();
        Game game = new Game(
                gameId,
                validateName(name),
                description,
                rules,
                achievementDefinition,
                now,
                metaData,
                GameStatus.ACTIVE,
                configuration,
                renderType
        );

        // Add domain event
        GameRegisteredEvent event = new GameRegisteredEvent(
                gameId.gameId(),
                name,
                description,
                achievementDefinition.stream()
                        .map(a -> new GameRegisteredEvent.AchievementInfo(
                                a.achievementId().achievementId(),
                                a.description()
                        ))
                        .toList(),
                new GameRegisteredEvent.MetaDataInfo(
                        metaData.category(),
                        metaData.theme(),
                        metaData.designer(),
                        metaData.publisher(),
                        metaData.releaseYear(),
                        metaData.estimatedDurationMinutes(),
                        metaData.complexity(),
                        metaData.frontendUrl(),
                        metaData.pictureUrl()
                ),
                new GameRegisteredEvent.GameRulesInfo(
                        rules.rulesText(),
                        rules.rulesUrl(),
                        rules.summary()
                ),
                new GameRegisteredEvent.PlayerConfigurationInfo(
                        configuration.minPlayers(),
                        configuration.maxPlayers(),
                        configuration.supportedPlayerTypes().stream()
                                .map(Enum::name)
                                .collect(java.util.stream.Collectors.toSet())
                ),
                now,
                String.valueOf(renderType)
        );
        event.setStatus(GameStatus.ACTIVE.name());
        game.domainEvents.add(event);

        return game;
    }

    // Used for creating the external chess game
    public static Game registerExternal(GameId gameId,
                                        String name,
                                        String description,
                                        GameRules rules,
                                        List<AchievementDefinition> achievementDefinition,
                                        PlayerConfiguration configuration,
                                        GameMetaData metaData,
                                        LocalDateTime registeredAt,
                                        RenderType renderType) {

        Game game = new Game(
                gameId,
                name,
                description,
                rules,
                achievementDefinition,
                registeredAt,
                metaData,
                GameStatus.ACTIVE,
                configuration,
                renderType
        );

        // Add domain event
        GameRegisteredEvent event = new GameRegisteredEvent(
                gameId.gameId(),
                name,
                description,
                achievementDefinition.stream()
                        .map(a -> new GameRegisteredEvent.AchievementInfo(
                                a.achievementId().achievementId(),
                                a.description()
                        ))
                        .toList(),
                new GameRegisteredEvent.MetaDataInfo(
                        metaData.category(),
                        metaData.theme(),
                        metaData.designer(),
                        metaData.publisher(),
                        metaData.releaseYear(),
                        metaData.estimatedDurationMinutes(),
                        metaData.complexity(),
                        metaData.frontendUrl(),
                        metaData.pictureUrl()
                ),
                new GameRegisteredEvent.GameRulesInfo(
                        rules.rulesText(),
                        rules.rulesUrl(),
                        rules.summary()
                ),
                new GameRegisteredEvent.PlayerConfigurationInfo(
                        configuration.minPlayers(),
                        configuration.maxPlayers(),
                        configuration.supportedPlayerTypes().stream()
                                .map(Enum::name)
                                .collect(java.util.stream.Collectors.toSet())
                ),
                registeredAt,
                String.valueOf(renderType)
        );
        event.setStatus(GameStatus.ACTIVE.name());
        game.domainEvents.add(event);

        return game;
    }

    public static Game hydrate(GameId id,
                               String name,
                               String description,
                               GameRules rules,
                               List<AchievementDefinition> achievements,
                               LocalDateTime registeredAt,
                               GameMetaData metaData,
                               GameStatus status,
                               PlayerConfiguration configuration,
                               RenderType renderType) {
        return new Game(id, name, description, rules, achievements, registeredAt, metaData, status,
                configuration, renderType);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("name is too long");
        }
        return name;
    }

    public boolean isExternallyHosted() {
        return metaData.frontendUrl() != null && renderType.equals(RenderType.EXTERNAL_IFRAME);
    }

    public void activate() {
        if (status == GameStatus.ACTIVE) {
            return;
        }
        if (status == GameStatus.DEPRECATED) {
            throw new IllegalStateException("Cannot activate a deprecated game");
        }
        this.status = GameStatus.ACTIVE;
    }

    public void deactivate() {
        if (status == GameStatus.DEPRECATED) {
            throw new IllegalStateException("Cannot deactivate a deprecated game");
        }
        this.status = GameStatus.INACTIVE;
    }

    public void deprecate() {
        this.status = GameStatus.DEPRECATED;
    }

    public void addAchievement(AchievementDefinition achievementDefinition) {
        Validators.requireNonNull(achievementDefinition, "achievementDefinition is required");
        boolean alreadyPresent = achievements.stream()
                .anyMatch(existing -> existing.achievementId().equals(achievementDefinition.achievementId()));
        if (alreadyPresent) {
            throw new IllegalArgumentException("Achievement already registered for this game");
        }
        achievements.add(achievementDefinition);
    }

    public void updateRules(GameRules updatedRules) {
        Validators.requireNonNull(updatedRules, "rules");
        this.rules = updatedRules;
    }

    public void updateMetadata(GameMetaData updatedMetaData) {
        Validators.requireNonNull(updatedMetaData, "meta data");
        this.metaData = updatedMetaData;
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void updatePlayerConfiguration(PlayerConfiguration newConfig) {
        Validators.requireNonNull(newConfig, "PlayerConfiguration");
        this.playerConfiguration = newConfig;
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
