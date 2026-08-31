package be.kdg.banditgamesbackend.social.adapter.in.web;

import be.kdg.banditgamesbackend.security.JwtUtils;
import be.kdg.banditgamesbackend.social.adapter.in.dto.FriendDto;
import be.kdg.banditgamesbackend.social.adapter.in.dto.SendFriendRequestDto;
import be.kdg.banditgamesbackend.social.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final SendFriendRequestUseCase sendFriendRequestUseCase;
    private final AcceptFriendRequestUseCase acceptFriendRequestUseCase;
    private final DeclineFriendRequestUseCase declineFriendRequestUseCase;
    private final GetFriendsQuery getFriendsQuery;
    private final JwtUtils jwtUtils;

    @PostMapping("/requests")
    public ResponseEntity<MessageResponse> sendFriendRequest(
            @Valid @RequestBody SendFriendRequestDto requestDto,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);

        try {
            SendFriendRequestCommand command = new SendFriendRequestCommand(
                    userId,
                    requestDto.toUserId()
            );

            sendFriendRequestUseCase.sendFriendRequest(command);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MessageResponse("Friend request sent successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<MessageResponse> acceptSendFriendRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);

        try {
            AcceptFriendRequestCommand command = new AcceptFriendRequestCommand(
                    friendshipId,
                    userId
            );

            acceptFriendRequestUseCase.acceptFriendRequest(command);

            return ResponseEntity
                    .ok(new MessageResponse("Friend request accepted"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{friendshipId}/decline")
    public ResponseEntity<MessageResponse> declineFriendRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);

        try {
            DeclineFriendRequestCommand command = new DeclineFriendRequestCommand(
                    friendshipId,
                    userId
            );

            declineFriendRequestUseCase.declineFriendRequest(command);

            return ResponseEntity
                    .ok(new MessageResponse("Friend request declined"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        }
    }


    @GetMapping("/friends")
    public ResponseEntity<List<FriendDto>> getFriends(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        List<FriendDto> friends = getFriendsQuery.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendDto>> getPendingRequests(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        List<FriendDto> pendingRequests = getFriendsQuery.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendDto>> getSentRequests(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        List<FriendDto> sent = getFriendsQuery.getSentRequests(userId);
        return ResponseEntity.ok(sent);
    }
}
