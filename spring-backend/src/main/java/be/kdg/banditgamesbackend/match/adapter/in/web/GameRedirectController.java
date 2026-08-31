package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameRedirectController {

    private static final Logger logger = LoggerFactory.getLogger(GameRedirectController.class);

    private final GameLookupService gameLookupService;
    private final RestTemplate restTemplate;

    @Value("${external.game.backend.url}")
    private String chessBackendUrl;

    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameRedirectResponse> startGame(
            @PathVariable String gameId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) StartGameRequest request) {

        UUID gameUuid = UUID.fromString(gameId);

        GameInfoDto gameInfo = gameLookupService.findGameById(gameUuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Game with ID " + gameId + " not found"));

        String frontendUrl = gameLookupService.getFrontendUrl(gameUuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Game " + gameInfo.name() + " has no frontend URL configured"));

        String matchId;
        String mode;
        StringBuilder urlBuilder = new StringBuilder();

        if (request != null && request.matchId() != null && !request.matchId().isEmpty()) {
            matchId = request.matchId();
            mode = "pvp";
        } else {
            matchId = UUID.randomUUID().toString();
            mode = "pve";
        }

        if ("Chess".equalsIgnoreCase(gameInfo.name()) && mode.equals("pvp") && request != null) {
            try {
                createChessGame(matchId, request);
            } catch (Exception e) {
                logger.error("Failed to create Chess game via backend API", e);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to initialize Chess game: " + e.getMessage());
            }
        }

        String token = jwt.getTokenValue();

        if ("Chess".equalsIgnoreCase(gameInfo.name()) && mode.equals("pvp")) {
            urlBuilder.append(String.format("%s/game/%s?token=%s",
                    frontendUrl, matchId, token));
        } else {
            urlBuilder.append(String.format("%s/game/setup?token=%s&matchId=%s&mode=%s",
                    frontendUrl, token, matchId, mode));
            if (request != null && request.player1Id() != null && request.player1Name() != null) {
                urlBuilder.append(String.format("&whitePlayerId=%s&whitePlayerName=%s",
                        request.player1Id(), request.player1Name()));
            }
            if (request != null && request.player2Id() != null && request.player2Name() != null) {
                urlBuilder.append(String.format("&blackPlayerId=%s&blackPlayerName=%s",
                        request.player2Id(), request.player2Name()));
            }
        }

        String redirectUrl = urlBuilder.toString();

        GameRedirectResponse response = new GameRedirectResponse(
                redirectUrl,
                matchId,
                gameInfo.name()
        );

        return ResponseEntity.ok(response);
    }


    private void createChessGame(String matchId, StartGameRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String createGameUrl = chessBackendUrl + "/api/games/" + matchId;

        Map<String, String> gameBody = new HashMap<>();
        gameBody.put("whitePlayerId", request.player1Id());
        gameBody.put("whitePlayerName", request.player1Name() != null ? request.player1Name() : "Player 1");
        gameBody.put("blackPlayerId", request.player2Id());
        gameBody.put("blackPlayerName", request.player2Name() != null ? request.player2Name() : "Player 2");

        logger.info("Creating Chess game via POST {} with players: {}", createGameUrl, gameBody);

        try {
            ResponseEntity<String> createResponse = restTemplate.exchange(
                    createGameUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(gameBody, headers),
                    String.class
            );
            logger.info("Chess game created successfully: {}", createResponse.getStatusCode());
            logger.info("Response body: {}", createResponse.getBody());
        } catch (Exception e) {
            logger.error("Failed to create Chess game", e);
            throw new RuntimeException("Failed to create Chess game in backend", e);
        }
    }

    public record StartGameRequest(
        String matchId,
        String player1Id,
        String player1Name,
        String player2Id,
        String player2Name
    ) {}

    public record GameRedirectResponse(
            String redirectUrl,
            String matchId,
            String gameType
    ) {}
}