package be.kdg.banditgamesbackend.common.messaging.config;

public record QueueConfig(
    String gameLifecycle,
    String gameRegistration,
    String playerActions,
    String achievements,
    String matchEvents,
    String lobbyEvents,
    String userEvents
) {
    public QueueConfig() {
        this(
            "platform.game.lifecycle",
            "platform.game.registration",
            "platform.player.actions",
            "platform.achievements",
            "platform.match.events",
            "platform.lobby.events",
            "platform.user.events"
        );
    }
}
