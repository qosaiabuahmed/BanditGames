package be.kdg.banditgamesbackend.gamemetadata.adapter.in;

import be.kdg.banditgamesbackend.gamemetadata.adapter.in.mapper.RegisterGameRequestMapper;
import be.kdg.banditgamesbackend.gamemetadata.adapter.in.request.RegisterGameRequest;
import be.kdg.banditgamesbackend.gamemetadata.adapter.in.response.GameResponse;
import be.kdg.banditgamesbackend.gamemetadata.domain.Game;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameId;
import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameFilterCriteria;
import be.kdg.banditgamesbackend.gamemetadata.port.in.GameQueryUseCase;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final RegisterInternalGameUseCase registerInternalGameUseCase;
    private final GameQueryUseCase gameQueryUseCase;
    private final RegisterGameRequestMapper requestMapper;

    @PostMapping
    public ResponseEntity<GameResponse> register(@Valid @RequestBody RegisterGameRequest request) {
        RegisterInternalGameCommand command = requestMapper.toCommand(request);
        Game created = registerInternalGameUseCase.registerGame(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.from(created));
    }

    @GetMapping
    public List<GameResponse> list(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime registeredAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime registeredBefore,
            @RequestParam(required = false) GameStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String designer,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) String complexity
    ) {
        GameFilterCriteria criteria = new GameFilterCriteria(
                name,
                registeredAfter,
                registeredBefore,
                status,
                category,
                theme,
                designer,
                publisher,
                releaseYear,
                minDuration,
                maxDuration,
                complexity
        );

        List<Game> games = criteria.hasFilters()
                ? gameQueryUseCase.getFilteredGames(criteria)
                : gameQueryUseCase.findAll();

        log.info("Found {} games matching filters", games.size());

        return games.stream()
                .map(GameResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> get(@PathVariable UUID id) {
        return gameQueryUseCase.findById(new GameId(id))
                .map(GameResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
