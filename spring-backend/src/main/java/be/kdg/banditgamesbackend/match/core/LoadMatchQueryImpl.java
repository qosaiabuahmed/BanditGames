package be.kdg.banditgamesbackend.match.core;

import java.util.Optional;

import org.springframework.stereotype.Service;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;
import be.kdg.banditgamesbackend.match.port.in.LoadMatchQuery;
import be.kdg.banditgamesbackend.match.port.out.LoadMatchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadMatchQueryImpl implements LoadMatchQuery{

    private final LoadMatchPort loadMatchPort;
    
    @Override
    public Optional<Match> getMatch(MatchId matchId) {
        return loadMatchPort.findById(matchId);
    }
}
