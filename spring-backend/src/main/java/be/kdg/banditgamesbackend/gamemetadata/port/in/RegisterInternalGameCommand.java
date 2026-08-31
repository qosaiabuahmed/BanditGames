package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterInternalGameCommand(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 500) String description,
        @NotNull RenderType renderType,
        @NotNull @Valid GameRules rules,
        @NotNull @Valid PlayerConfiguration playerConfiguration,
        List<@Valid AchievementDefinition> achievements,
        @NotNull GameMetaData metaData
) {
}
