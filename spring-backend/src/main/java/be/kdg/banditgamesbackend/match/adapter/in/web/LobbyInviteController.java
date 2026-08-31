package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.port.in.*;
import be.kdg.banditgamesbackend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
public class LobbyInviteController {

    private final SendLobbyInviteUseCase sendInviteUseCase;
    private final AcceptLobbyInviteUseCase acceptInviteUseCase;
    private final DeclineLobbyInviteUseCase declineInviteUseCase;
    private final GetPendingLobbyInvitesQuery getPendingInvitesQuery;
    private final JwtUtils jwtUtils;

    @PostMapping("/{lobbyId}/invites")
    public ResponseEntity<Void> sendInvite(
            @PathVariable UUID lobbyId,
            @RequestBody SendLobbyInviteRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID fromUserId = jwtUtils.extractUserId(jwt);

        SendLobbyInviteCommand command = new SendLobbyInviteCommand(
                lobbyId,
                fromUserId,
                request.toUserId()
        );

        LobbyInviteId inviteId = sendInviteUseCase.sendInvite(command);

        return ResponseEntity.created(
                URI.create("/api/lobbies/invites/" + inviteId.value())
        ).build();
    }

    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<JoinLobbyResponse> acceptInvite(
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = jwtUtils.extractUserId(jwt);

        AcceptLobbyInviteCommand command = new AcceptLobbyInviteCommand(
                inviteId,
                userId
        );

        LobbyJoinResult result = acceptInviteUseCase.acceptInvite(command);

        JoinLobbyResponse response = JoinLobbyResponse.from(
                result.lobby(),
                result.createdMatch().orElse(null)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invites/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(
            @PathVariable UUID inviteId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = jwtUtils.extractUserId(jwt);

        DeclineLobbyInviteCommand command = new DeclineLobbyInviteCommand(
                inviteId,
                userId
        );

        declineInviteUseCase.declineInvite(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invites/pending")
    public ResponseEntity<List<LobbyInviteResponse>> getPendingInvites(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = jwtUtils.extractUserId(jwt);

        List<LobbyInvite> invites = getPendingInvitesQuery.getPendingInvites(userId);

        List<LobbyInviteResponse> response = invites.stream()
                .map(LobbyInviteResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}