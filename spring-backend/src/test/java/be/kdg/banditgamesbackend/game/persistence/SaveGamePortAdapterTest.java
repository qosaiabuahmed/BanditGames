package be.kdg.banditgamesbackend.game.persistence;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.mapper.GameJpaMapper;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameJpaEntity;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameJpaRepository;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.SaveGamePortAdapter;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveGamePortAdapterTest {

    @Mock
    private GameJpaRepository gameJpaRepository;

    @Mock
    private GameJpaMapper mapper;

    @InjectMocks
    private SaveGamePortAdapter adapter;

    @Test
    void save_ShouldPersistAndReturnDomain() {
        Game game = createTestGame();
        GameJpaEntity entity = new GameJpaEntity();
        
        when(mapper.toEntity(game)).thenReturn(entity);
        when(gameJpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(game);

        Game saved = adapter.save(game);

        assertThat(saved).isEqualTo(game);
        verify(gameJpaRepository).save(entity);
    }

    private Game createTestGame() {
        return Game.register(
                new GameId(UUID.randomUUID()),
                "Test",
                "Desc",
                new GameRules("R", "U", "D"),
                new ArrayList<>(),
                new PlayerConfiguration(1, 2, Set.of(PlayerConfiguration.PlayerType.HUMAN)),
                RenderType.NATIVE_TICTACTOE,
                new GameMetaData("C", "T", "D", "P", 2024, 30, "E", "https:testFrontendUrl.com", "https:testPicUrl.com")
        );
    }
}
