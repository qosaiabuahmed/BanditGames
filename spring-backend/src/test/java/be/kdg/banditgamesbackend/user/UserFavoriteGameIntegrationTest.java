package be.kdg.banditgamesbackend.user;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameInfoProjectionRepository;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameJpaRepository;
import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.core.RegisterInternalGameUseCaseImpl;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameCommand;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserFavoriteJpaEntity;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserFavoriteJpaRepository;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaEntity;
import be.kdg.banditgamesbackend.user.adapter.out.persistence.UserJpaRepository;
import be.kdg.banditgamesbackend.user.domain.UserStatus;
import be.kdg.banditgamesbackend.user.domain.UserType;
import be.kdg.banditgamesbackend.user.port.in.GetFavoriteGamesQuery;
import be.kdg.banditgamesbackend.user.port.in.MarkFavoriteGameCommand;
import be.kdg.banditgamesbackend.user.port.in.MarkFavoriteGameUseCase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserFavoriteGameIntegrationTest {

    @Autowired
    private RegisterInternalGameUseCaseImpl registerGameUseCase;

    @Autowired
    private MarkFavoriteGameUseCase markFavoriteGameUseCase;

    @Autowired
    private GetFavoriteGamesQuery getFavoriteGamesQuery;

    @Autowired
    private UserFavoriteJpaRepository userFavoriteJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private GameInfoProjectionRepository projectionRepository;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private UUID testUserId;
    private UUID testGameId;

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue("platform.game.registration", false);

        userFavoriteJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        projectionRepository.deleteAll();
        gameJpaRepository.deleteAll();

        await().pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(1, SECONDS)
                .untilAsserted(() -> {
                    assertThat(projectionRepository.count()).isZero();
                    assertThat(gameJpaRepository.count()).isZero();
                });

        testUserId = createTestUser();
        testGameId = createTestGame();

        await().atMost(5, SECONDS)
                .until(() -> projectionRepository.existsById(testGameId));

    }

    @Test
    void whenGameIsFavorited_thenAfterPageReload_gameShouldStillBeFavorited() {
        MarkFavoriteGameCommand command = new MarkFavoriteGameCommand(testUserId, testGameId);
        markFavoriteGameUseCase.markFavoriteGame(command);

        List<UUID> favoriteGameIds = userFavoriteJpaRepository.findGameIdsByUserId(testUserId);
        assertThat(favoriteGameIds)
                .as("Game should be in favorites immediately after marking")
                .hasSize(1)
                .contains(testGameId);

        entityManager.clear();

        List<GameInfoDto> favoriteGames = getFavoriteGamesQuery.getFavoriteGames(testUserId);

        assertThat(favoriteGames)
                .as("Favorite game should persist after page reload")
                .hasSize(1);

        assertThat(favoriteGames.getFirst().gameId())
                .as("Game ID should match")
                .isEqualTo(testGameId);
    }

    @Test
    void whenMultipleGamesAreFavorited_thenAllShouldPersist() {
        UUID gameId2 = createTestGame("Game 2", "Description 2");
        UUID gameId3 = createTestGame("Game 3", "Description 3");

        await().atMost(5, SECONDS)
                .until(() -> projectionRepository.existsById(gameId2)
                        && projectionRepository.existsById(gameId3));

        markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(testUserId, testGameId));
        markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(testUserId, gameId2));
        markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(testUserId, gameId3));

        entityManager.clear();

        List<GameInfoDto> favoriteGames = getFavoriteGamesQuery.getFavoriteGames(testUserId);

        assertThat(favoriteGames)
                .as("All favorited games should persist")
                .hasSize(3)
                .extracting(GameInfoDto::gameId)
                .containsExactlyInAnyOrder(testGameId, gameId2, gameId3);
    }

    @Test
    void whenGameIsUnfavorited_thenAfterPageReload_gameShouldNotBeFavorited() {
        MarkFavoriteGameCommand command = new MarkFavoriteGameCommand(testUserId, testGameId);
        markFavoriteGameUseCase.markFavoriteGame(command);

        assertThat(userFavoriteJpaRepository.findGameIdsByUserId(testUserId))
                .hasSize(1);

        markFavoriteGameUseCase.unmarkFavoriteGame(command);

        entityManager.clear();

        List<GameInfoDto> favoriteGames = getFavoriteGamesQuery.getFavoriteGames(testUserId);

        assertThat(favoriteGames)
                .as("Game should not be in favorites after unfavoriting")
                .isEmpty();
    }

    @Test
    void whenFavoriteIsPersisted_thenFavoritedAtTimestampShouldBeSet() {
        markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(testUserId, testGameId));

        entityManager.clear();

        List<UserFavoriteJpaEntity> favorites = userFavoriteJpaRepository.findAll();

        assertThat(favorites)
                .hasSize(1);

        UserFavoriteJpaEntity favorite = favorites.getFirst();
        assertThat(favorite.getFavoritedAt())
                .as("FavoritedAt timestamp should be set")
                .isNotNull();
        assertThat(favorite.getGameId()).isEqualTo(testGameId);
    }

    @Test
    void whenDifferentUsersLikeSameGame_thenBothFavoritesShouldPersist() {
        UUID secondUserId = createTestUser("seconduser", "second@test.com", "SECOND#0002");

        markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(testUserId, testGameId));
        markFavoriteGameUseCase.markFavoriteGame(new MarkFavoriteGameCommand(secondUserId, testGameId));

        entityManager.clear();

        List<GameInfoDto> user1Favorites = getFavoriteGamesQuery.getFavoriteGames(testUserId);
        List<GameInfoDto> user2Favorites = getFavoriteGamesQuery.getFavoriteGames(secondUserId);

        assertThat(user1Favorites).hasSize(1);
        assertThat(user2Favorites).hasSize(1);
        assertThat(user1Favorites.getFirst().gameId()).isEqualTo(testGameId);
        assertThat(user2Favorites.getFirst().gameId()).isEqualTo(testGameId);
    }

    private UUID createTestUser() {
        return createTestUser("testuser", "test@example.com", "TEST#0001");
    }

    private UUID createTestUser(String username, String email, String playerTag) {
        UUID userId = UUID.randomUUID();

        UserJpaEntity userJpa = new UserJpaEntity(
                userId,
                username,
                email,
                playerTag,
                UserStatus.ONLINE,
                LocalDateTime.now(),
                null,
                UserType.REGISTERED
        );

        userJpaRepository.save(userJpa);
        return userId;
    }

    private UUID createTestGame() {
        return createTestGame("Test Game", "Test Description");
    }

    private UUID createTestGame(String name, String description) {
        RegisterInternalGameCommand command = new RegisterInternalGameCommand(
                name,
                description,
                RenderType.NATIVE_TICTACTOE,
                createTestGameRules(),
                createTestPlayerConfiguration(),
                new ArrayList<>(),
                createTestGameMetaData()
        );

        Game game = registerGameUseCase.registerGame(command);
        return game.getGameId().gameId();
    }

    private GameRules createTestGameRules() {
        return new GameRules(
                "Basic rules for testing",
                "Rule.com",
                "Basic rule description"
        );
    }

    private PlayerConfiguration createTestPlayerConfiguration() {
        return new PlayerConfiguration(
                2,
                2,
                Set.of(PlayerConfiguration.PlayerType.HUMAN)
        );
    }

    private GameMetaData createTestGameMetaData() {
        return new GameMetaData(
                "Strategy",
                "Medieval",
                "John Doe",
                "Test Publisher",
                2024,
                45,
                "Medium",
                "http://localhost:8000",  // Test frontend URL
                "tictactoe.png"
        );
    }
}
