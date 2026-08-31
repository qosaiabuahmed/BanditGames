package be.kdg.banditgamesbackend.match.domain;

import java.util.Map;

public record MatchStatistics(
    long totalMatches,
    Map<MatchStatus, Long> matchesByStatus,
    Double averageMatchDurationMinutes
) {}
