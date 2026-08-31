package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.adapter.in.request.CreateMatchRequest;
import be.kdg.banditgamesbackend.match.adapter.in.response.MatchResponse;
import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;
import be.kdg.banditgamesbackend.match.port.in.CreateMatchCommand;
import be.kdg.banditgamesbackend.match.port.in.CreateMatchUseCase;
import be.kdg.banditgamesbackend.match.port.in.LoadMatchQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final CreateMatchUseCase createMatchUseCase;
    private final LoadMatchQuery matchQueryUseCase;

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request) {
        Match match = createMatchUseCase.createMatch(new CreateMatchCommand(request.gameId(), request.playerIds()));
        return ResponseEntity.status(HttpStatus.CREATED).body(MatchResponse.from(match));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> get(@PathVariable UUID id) {
        return matchQueryUseCase.getMatch(new MatchId(id))
                .map(MatchResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
