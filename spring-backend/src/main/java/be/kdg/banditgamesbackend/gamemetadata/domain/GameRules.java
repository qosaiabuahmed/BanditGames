package be.kdg.banditgamesbackend.gamemetadata.domain;

public record GameRules(
        String rulesText,
        String rulesUrl,
        String summary
) {
    public GameRules {
        if (rulesText == null || rulesUrl == null || summary == null) {
            throw new IllegalArgumentException("At least one rule needs to be provided");
        }
    }
}
