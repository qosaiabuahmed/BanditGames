package be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence;

import be.kdg.banditgamesbackend.gamemetadata.domain.RenderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_info_projection")
@Getter
@Setter
public class GameInfoProjection {

    @Id
    private UUID gameId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;
    private LocalDateTime registeredAt;

    @Column(length = 500)
    private String frontendUrl;

    @Column(length = 500)
    private String pictureUrl;

    @Enumerated(EnumType.STRING)
    private RenderType renderType;
}
