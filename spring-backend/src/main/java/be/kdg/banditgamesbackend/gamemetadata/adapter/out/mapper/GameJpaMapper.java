package be.kdg.banditgamesbackend.gamemetadata.adapter.out.mapper;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.AchievementDefinitionEmbeddable;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameJpaEntity;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameMetaDataEmbeddable;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameRulesEmbeddable;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.PlayerConfigurationEmbeddable;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import be.kdg.banditgamesbackend.gamemetadata.domain.PlayerConfiguration.PlayerType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;


@Mapper(componentModel = "spring")
public interface GameJpaMapper {

    @Mapping(target = "gameId", expression = "java(game.getGameId().gameId())")
    GameJpaEntity toEntity(Game game);

    default Game toDomain(GameJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Game.hydrate(
                new GameId(entity.getGameId()),
                entity.getName(),
                entity.getDescription(),
                toDomain(entity.getRules()),
                toDomainList(entity.getAchievements()),
                entity.getRegisteredAt(),
                toDomain(entity.getMetaData()),
                entity.getStatus(),
                toDomain(entity.getPlayerConfiguration()),
                entity.getRenderType()
        );
    }

    // ========== GAME RULES MAPPINGS ==========

    GameRulesEmbeddable toEntity(GameRules rules);
    GameRules toDomain(GameRulesEmbeddable embeddable);

    // ========== GAME METADATA MAPPINGS ==========

    GameMetaDataEmbeddable toEntity(GameMetaData metaData);
    GameMetaData toDomain(GameMetaDataEmbeddable embeddable);

    // ========== PLAYER CONFIGURATION MAPPINGS ==========

    PlayerConfigurationEmbeddable toEntity(PlayerConfiguration configuration);
    PlayerConfiguration toDomain(PlayerConfigurationEmbeddable embeddable);

    default PlayerConfigurationEmbeddable.PlayerTypeEmbeddable map(PlayerType type) {
        return type == null ? null :
                PlayerConfigurationEmbeddable.PlayerTypeEmbeddable.valueOf(type.name());
    }

    default PlayerType map(PlayerConfigurationEmbeddable.PlayerTypeEmbeddable type) {
        return type == null ? null : PlayerType.valueOf(type.name());
    }

    // ========== ACHIEVEMENT DEFINITION MAPPINGS ==========

    AchievementDefinitionEmbeddable toEntity(AchievementDefinition definition);
    AchievementDefinition toDomain(AchievementDefinitionEmbeddable embeddable);

    List<AchievementDefinitionEmbeddable> toEntityList(List<AchievementDefinition> definitions);
    List<AchievementDefinition> toDomainList(List<AchievementDefinitionEmbeddable> embeddables);

    // ========== VALUE OBJECT CONVERSIONS ==========

    default UUID map(GameId gameId) {
        return gameId != null ? gameId.gameId() : null;
    }

    default GameId map(UUID uuid) {
        return uuid != null ? new GameId(uuid) : null;
    }

    default String mapAchievementId(AchievementId achievementId) {
        return achievementId != null ? achievementId.achievementId() : null;
    }

    default AchievementId mapAchievementId(String achievementId) {
        return achievementId != null ? new AchievementId(achievementId) : null;
    }
}
