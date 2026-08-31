package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.domain.GameStatus;
import be.kdg.banditgamesbackend.gamemetadata.domain.RenderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
public class GameJpaEntity {

    @Id
    private UUID gameId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    private GameRulesEmbeddable rules;

    @ElementCollection
    @CollectionTable(name = "achievements", joinColumns = @JoinColumn(name = "game_id"))
    private List<AchievementDefinitionEmbeddable> achievements;

    private LocalDateTime registeredAt;

    @Embedded
    private GameMetaDataEmbeddable metaData;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Embedded
    private PlayerConfigurationEmbeddable playerConfiguration;

    @Enumerated(EnumType.STRING)
    private RenderType renderType;
}
