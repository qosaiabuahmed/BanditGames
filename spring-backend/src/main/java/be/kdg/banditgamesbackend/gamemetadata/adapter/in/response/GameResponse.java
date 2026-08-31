package be.kdg.banditgamesbackend.gamemetadata.adapter.in.response;

import be.kdg.banditgamesbackend.gamemetadata.domain.AchievementDefinition;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameMetaData;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameRules;
import be.kdg.banditgamesbackend.gamemetadata.domain.PlayerConfiguration;
import be.kdg.banditgamesbackend.gamemetadata.domain.RenderType;

import java.time.LocalDateTime;
import java.util.List;

public record GameResponse(
        String id,
        String name,
        String description,
        GameRules rules,
        List<AchievementDefinition> achievements,
        LocalDateTime registeredAt,
        GameMetaData metaData,
        String status,
        PlayerConfiguration playerConfiguration,
        RenderType renderType
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getGameId().gameId().toString(),
                game.getName(),
                game.getDescription(),
                game.getRules(),
                game.getAchievements(),
                game.getRegisteredAt(),
                game.getMetaData(),
                game.getStatus().name(),
                game.getPlayerConfiguration(),
                game.getRenderType()
        );
    }
}
