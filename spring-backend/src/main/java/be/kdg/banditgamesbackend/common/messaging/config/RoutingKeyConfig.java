package be.kdg.banditgamesbackend.common.messaging.config;

public record RoutingKeyConfig(
    String gameCreated,
    String gameStarted,
    String gameEnded,
    String gameInternalRegistered,
    String gameExternalRegistered,
    String matchStateUpdated,
    String moveMade,
    String moveValidated,
    String achievementUnlocked,
    String playerJoined,
    String playerLeft,
    String matchCreated,
    String matchStarted,
    String matchCompleted,
    String lobbyCreated,
    String lobbyReady,
    String userCreated,
    String userUpdated
) {
    public RoutingKeyConfig() {
        this(
            "game.created",
            "game.started",
            "game.ended",
            "game.internal.registered",
            "game.external.registered",
            "match.state.updated",
            "move.made",
            "move.validated",
            "achievement.unlocked",
            "player.joined",
            "player.left",
            "match.created",
            "match.started",
            "match.completed",
            "lobby.created",
            "lobby.ready",
            "user.created",
            "user.updated"
        );
    }

}
