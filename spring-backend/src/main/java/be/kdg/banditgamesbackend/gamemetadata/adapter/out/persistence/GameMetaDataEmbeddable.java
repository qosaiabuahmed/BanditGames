package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class GameMetaDataEmbeddable {

    private String category;
    private String theme;
    private String designer;
    private String publisher;
    private int releaseYear;
    private int estimatedDurationMinutes;
    private String complexity;

    @Column(length = 500)
    private String frontendUrl;

    @Column(length = 500)
    private String pictureUrl;
}
