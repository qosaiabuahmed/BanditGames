package be.kdg.banditgamesbackend.achievement.adapter.out;

import be.kdg.banditgamesbackend.achievement.domain.TempAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TempAchievementMapper {
    TempAchievement toDomain(TempAchievementJpaEntity tempAchievementJpaEntity);
}