package be.kdg.banditgamesbackend.user.adapter.in.web;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.user.adapter.in.dto.UserResponse;
import be.kdg.banditgamesbackend.user.adapter.in.request.ConvertGuestRequest;
import be.kdg.banditgamesbackend.user.adapter.in.request.CreateUserRequest;
import be.kdg.banditgamesbackend.user.adapter.in.request.UpdateUserProfileRequest;
import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.domain.UserStatus;
import be.kdg.banditgamesbackend.user.port.in.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    private final GetAllUsersQuery getAllUsersQuery;
    private final MarkFavoriteGameUseCase markFavoriteGameUseCase;
    private final GetFavoriteGamesQuery getFavoriteGamesQuery;
    private final ConvertGuestUserUseCase convertGuestUserUseCase;
    private final ApplicationEventPublisher eventPublisher;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            UpdateUserStatusUseCase updateUserStatusUseCase,
            GetAllUsersQuery getAllUsersQuery, MarkFavoriteGameUseCase markFavoriteGameUseCase, GetFavoriteGamesQuery getFavoriteGamesQuery, ConvertGuestUserUseCase convertGuestUserUseCase,
            ApplicationEventPublisher eventPublisher) {
        this.createUserUseCase = createUserUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.updateUserStatusUseCase = updateUserStatusUseCase;
        this.getAllUsersQuery = getAllUsersQuery;
        this.markFavoriteGameUseCase = markFavoriteGameUseCase;
        this.getFavoriteGamesQuery = getFavoriteGamesQuery;
        this.convertGuestUserUseCase = convertGuestUserUseCase;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.username(),
                request.email(),
                request.playerTag(),
                request.password()
        );

        User user = createUserUseCase.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.fromDomain(user));
    }

    @PostMapping("/guests/{guestId}/convert")
    public ResponseEntity<Void> convertGuestToUser(@PathVariable UUID guestId, @RequestBody ConvertGuestRequest request) {
        convertGuestUserUseCase.convertToRegistered(new ConvertGuestUserCommand(
                guestId,
                request.username(),
                request.email(),
                request.password()
        ));

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = getAllUsersQuery.getAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        User user = getUserProfileUseCase.getUserProfileByEmail(email);
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable UUID userId) {
        User user = getUserProfileUseCase.getUserProfile(userId);
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserProfileByEmail(@PathVariable String email) {
        User user = getUserProfileUseCase.getUserProfileByEmail(email);
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId,
                request.username(),
                request.email(),
                request.playerTag(),
                request.avatar()
        );

        User user = updateUserProfileUseCase.updateUserProfile(command);
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PostMapping("/me/online")
    public ResponseEntity<Void> markUserOnline(@AuthenticationPrincipal Jwt jwt) {
        User user = getUserProfileUsingEmail(jwt);

        UpdateUserStatusCommand command = new UpdateUserStatusCommand(
                user.getUserId(),
                UserStatus.ONLINE
        );
        updateUserStatusUseCase.updateUserStatus(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/offline")
    public ResponseEntity<Void> markUserOffline(@AuthenticationPrincipal Jwt jwt) {
        User user = getUserProfileUsingEmail(jwt);

        UpdateUserStatusCommand command = new UpdateUserStatusCommand(
                user.getUserId(),
                UserStatus.OFFLINE
        );
        updateUserStatusUseCase.updateUserStatus(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/favorites/{gameId}")
    public ResponseEntity<Void> markFavoriteGame(@PathVariable UUID gameId, @AuthenticationPrincipal Jwt jwt) {
        User user = getUserProfileUsingEmail(jwt);

        var event = markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(user.getUserId(), gameId));
        eventPublisher.publishEvent(event);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/favorites/{gameId}")
    public ResponseEntity<Void> unMarkFavoriteGame(@PathVariable UUID gameId, @AuthenticationPrincipal Jwt jwt) {
        User user = getUserProfileUsingEmail(jwt);

        var event = markFavoriteGameUseCase.unmarkFavoriteGame(new MarkFavoriteGameCommand(user.getUserId(), gameId));
        eventPublisher.publishEvent(event);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<List<GameInfoDto>> getFavoriteGames(@AuthenticationPrincipal Jwt jwt) {
        log.info("Fetching favorite games");
        User user = getUserProfileUsingEmail(jwt);
        List<GameInfoDto> favoriteGames = getFavoriteGamesQuery.getFavoriteGames(user.getUserId());
        log.info("Got favorite games{}", favoriteGames);
        return ResponseEntity.ok(favoriteGames);
    }

    @GetMapping("/me/favorites/ids")
    public ResponseEntity<List<UUID>> getFavoriteGameIds(@AuthenticationPrincipal Jwt jwt) {
        log.info("Fetching favorite game IDs");
        User user = getUserProfileUsingEmail(jwt);
        List<UUID> favoriteGameIds = getFavoriteGamesQuery.getFavoriteGames(user.getUserId())
                .stream()
                .map(GameInfoDto::gameId)
                .toList();
        log.info("Got {} favorite game IDs", favoriteGameIds.size());
        return ResponseEntity.ok(favoriteGameIds);
    }

    private User getUserProfileUsingEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return getUserProfileUseCase.getUserProfileByEmail(email);
    }
}