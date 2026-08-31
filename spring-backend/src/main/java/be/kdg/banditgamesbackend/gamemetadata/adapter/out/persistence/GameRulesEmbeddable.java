package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class GameRulesEmbeddable {

    @Column(columnDefinition = "TEXT")
    private String rulesText;

    @Column(length = 500)
    private String rulesUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;
}


