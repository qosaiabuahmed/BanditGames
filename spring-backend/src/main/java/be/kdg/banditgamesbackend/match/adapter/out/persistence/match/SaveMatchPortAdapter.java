package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.port.out.SaveMatchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveMatchPortAdapter implements SaveMatchPort {

    private final MatchJpaRepository matchJpaRepository;
    private final MatchJpaMapper mapper;

    @Override
    public Match save(Match match) {
        MatchJpaEntity saved = matchJpaRepository.save(mapper.toEntity(match));
        return mapper.toDomain(saved);
    }
}
