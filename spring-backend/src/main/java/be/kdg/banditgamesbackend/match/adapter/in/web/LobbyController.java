package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.port.in.JoinLobbyCommand;
import be.kdg.banditgamesbackend.match.port.in.JoinLobbyUseCase;
import be.kdg.banditgamesbackend.match.port.in.LeaveLobbyCommand;
import be.kdg.banditgamesbackend.match.port.in.LeaveLobbyUseCase;
import be.kdg.banditgamesbackend.match.port.in.LobbyJoinResult;
import be.kdg.banditgamesbackend.match.port.in.LobbyQueryUseCase;
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
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
public class LobbyController {
    private final JoinLobbyUseCase joinLobbyUseCase;
    private final LobbyQueryUseCase lobbyQueryUseCase;
    private final LeaveLobbyUseCase leaveLobbyUseCase;

    @PostMapping("/join")
    public ResponseEntity<JoinLobbyResponse> join(@Valid @RequestBody JoinLobbyRequest request) {
        LobbyJoinResult result = joinLobbyUseCase.joinLobby(new JoinLobbyCommand(request.gameId(), request.userId()));
        return ResponseEntity.status(HttpStatus.OK)
                .body(JoinLobbyResponse.from(result.lobby(), result.createdMatch().orElse(null)));
    }

    @PostMapping("/leave")
    public ResponseEntity<LobbyResponse> leave(@Valid @RequestBody LeaveLobbyRequest request) {
        return ResponseEntity.ok(
                LobbyResponse.from(leaveLobbyUseCase.leaveLobby(new LeaveLobbyCommand(request.lobbyId(), request.userId())))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LobbyResponse> get(@PathVariable UUID id) {
        return lobbyQueryUseCase.getLobby(new LobbyId(id))
                .map(LobbyResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
