package be.kdg.banditgamesbackend.gamemetadata.adapter.in.request;

import be.kdg.banditgamesbackend.gamemetadata.domain.PlayerConfiguration.PlayerType;
import be.kdg.banditgamesbackend.gamemetadata.domain.RenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record RegisterGameRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 500)
        String description,

        @Valid @NotNull
        GameRulesDto rules,

        @Valid @NotNull
        List<AchievementDefinitionDto> achievements,

        @Valid @NotNull
        PlayerConfigurationDto playerConfiguration,

        @NotNull
        RenderType renderType,

        @NotNull
        GameMetaDataDto metaData
) {

    public record GameRulesDto(String rulesText, String rulesUrl, String summary) {
    }

    public record AchievementDefinitionDto(
            String achievementId,
            String name,
            String description,
            String iconUrl,
            String condition
    ) {
    }

    public record PlayerConfigurationDto(
            int minPlayers,
            int maxPlayers,
            Set<PlayerType> supportedPlayerTypes
    ) {
    }

    public record GameMetaDataDto(
            String category,
            String theme,
            String designer,
            String publisher,
            int releaseYear,
            int estimatedDurationMinutes,
            String complexity,
            String frontendUrl,
            String pictureUrl
    ) {
    }
}
