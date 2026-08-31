package be.kdg.banditgamesbackend.common.messaging.config;

public record DeadLetterConfig(
    String exchange,
    String queue,
    String routingKey,
    int messageTtl,
    int maxRetries
) {
    public DeadLetterConfig() {
        this(
            "platform.dlx",
            "platform.dlq",
            "dead.letter",
            86400000,  // 24 hours in milliseconds
            3
        );
    }
}
