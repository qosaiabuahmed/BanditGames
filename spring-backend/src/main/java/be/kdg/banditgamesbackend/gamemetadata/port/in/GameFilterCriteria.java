package be.kdg.banditgamesbackend.gamemetadata.port.in;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus;

import java.time.LocalDateTime;

public record GameFilterCriteria(
        String name,
        LocalDateTime registeredAfter,
        LocalDateTime registeredBefore,
        GameStatus status,
        String category,
        String theme,
        String designer,
        String publisher,
        Integer releaseYear,
        Integer minDuration,
        Integer maxDuration,
        String complexity
) {
    public boolean hasFilters() {
        return name != null ||
                registeredAfter != null ||
                registeredBefore != null ||
                status != null ||
                category != null ||
                theme != null ||
                designer != null ||
                publisher != null ||
                releaseYear != null ||
                minDuration != null ||
                maxDuration != null ||
                complexity != null;
    }
}
