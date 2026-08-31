package be.kdg.banditgamesbackend.match.adapter.out.persistence.match;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;
import be.kdg.banditgamesbackend.match.port.out.LoadMatchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class LoadMatchPortAdapter implements LoadMatchPort {

    private final MatchJpaRepository matchJpaRepository;
    private final MatchJpaMapper mapper;

    @Override
    public Optional<Match> findById(MatchId matchId) {
        return matchJpaRepository.findById(matchId.value())
                .map(mapper::toDomain);
    }
}
