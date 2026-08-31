package be.kdg.banditgamesbackend.gamemetadata.domain;

public record GameMetaData(
        String category,
        String theme,
        String designer,
        String publisher,
        int releaseYear,
        int estimatedDurationMinutes,
        String complexity,
        String frontendUrl,
        String pictureUrl
) {}
