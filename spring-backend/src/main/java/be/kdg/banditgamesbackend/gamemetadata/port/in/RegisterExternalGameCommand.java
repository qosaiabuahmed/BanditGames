package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RegisterExternalGameCommand(
        UUID gameId,
        String gameName,
        String gameDescription,
        List<AchievementDefinition> achievements,
        GameRules rules,
        GameMetaData metaData,
        PlayerConfiguration playerConfig,
        LocalDateTime registeredAt,
        RenderType renderType
) {}
