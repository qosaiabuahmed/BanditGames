package be.kdg.banditgamesbackend.gamemetadata.adapter.in.response;

public record GameMetaDataDto(
        String category,
        String theme,
        String designer,
        String publisher,
        int releaseYear,
        int estimatedDurationMinutes,
        String complexity
) {
}
