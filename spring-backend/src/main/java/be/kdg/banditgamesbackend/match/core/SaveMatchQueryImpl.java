package be.kdg.banditgamesbackend.match.core;

import org.springframework.stereotype.Service;

import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.port.in.SaveMatchQuery;
import be.kdg.banditgamesbackend.match.port.out.SaveMatchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveMatchQueryImpl implements SaveMatchQuery {

    private final SaveMatchPort saveMatchPort;
	@Override
	public void saveMatch(Match match) {
        log.info("Saving match: {}", match);
        saveMatchPort.save(match);
	}
}
