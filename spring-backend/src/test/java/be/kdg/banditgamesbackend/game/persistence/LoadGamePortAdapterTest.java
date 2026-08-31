package be.kdg.banditgamesbackend.game.persistence;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.mapper.GameJpaMapper;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameJpaEntity;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameJpaRepository;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.LoadGamePortAdapter;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadGamePortAdapterTest {

    @Mock
    private GameJpaRepository gameJpaRepository;

    @Mock
    private GameJpaMapper mapper;

    @InjectMocks
    private LoadGamePortAdapter adapter;

    @Test
    void findById_WhenExists_ShouldReturnDomain() {
        GameId gameId = new GameId(UUID.randomUUID());
        GameJpaEntity entity = new GameJpaEntity();
        Game game = createTestGame();

        when(gameJpaRepository.findById(gameId.gameId())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(game);

        Optional<Game> result = adapter.findById(gameId);

        assertThat(result).isPresent().contains(game);
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
