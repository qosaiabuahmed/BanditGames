package be.kdg.banditgamesbackend.gamemetadata.adapter.in.mapper;

import be.kdg.banditgamesbackend.gamemetadata.adapter.in.request.RegisterGameRequest;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameCommand;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface RegisterGameRequestMapper {

    RegisterInternalGameCommand toCommand(RegisterGameRequest request);

    GameRules toGameRules(RegisterGameRequest.GameRulesDto dto);

    GameMetaData toGameMetaData(RegisterGameRequest.GameMetaDataDto dto);

    PlayerConfiguration toPlayerConfiguration(RegisterGameRequest.PlayerConfigurationDto dto);

    default AchievementDefinition toAchievementDefinition(RegisterGameRequest.AchievementDefinitionDto dto) {
        if (dto == null) {
            return null;
        }
        String achievementId = dto.achievementId();
        return new AchievementDefinition(
                new AchievementId(achievementId),
                dto.name(),
                dto.description(),
                dto.iconUrl()
        );
    }
}
