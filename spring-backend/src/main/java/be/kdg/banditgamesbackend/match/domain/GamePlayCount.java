package be.kdg.banditgamesbackend.match.domain;

import java.util.UUID;

public record GamePlayCount(
    UUID gameId,
    long totalMatches,
    long finishedMatches
) {}