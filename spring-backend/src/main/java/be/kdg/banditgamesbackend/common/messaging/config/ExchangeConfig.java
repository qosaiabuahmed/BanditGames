package be.kdg.banditgamesbackend.common.messaging.config;

public record ExchangeConfig(
    String platformEvents,
    String gameEvents,
    String platformCommands
) {
    public ExchangeConfig() {
        this(
            "platform.events.exchange",
            "game.events.exchange",
            "platform.commands.exchange"
        );
    }
}
