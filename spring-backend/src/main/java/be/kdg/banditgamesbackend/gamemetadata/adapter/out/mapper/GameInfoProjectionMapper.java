package be.kdg.banditgamesbackend.gamemetadata.adapter.out.mapper;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameInfoProjection;
import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface GameInfoProjectionMapper {
    GameInfoDto toDto(GameInfoProjection gameInfoProjection);
}