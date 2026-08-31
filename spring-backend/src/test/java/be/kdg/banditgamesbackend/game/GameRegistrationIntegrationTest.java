package be.kdg.banditgamesbackend.game;

import be.kdg.banditgamesbackend.common.events.GameRegisteredEvent;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameInfoProjection;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameInfoProjectionRepository;
import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterInternalGameUseCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;


@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameRegistrationIntegrationTest {

    private static final int ASYNC_TIMEOUT_SECONDS = 5;

    @Autowired
    private RegisterInternalGameUseCase registerInternalGameUseCase;

    @Autowired
    private GameInfoProjectionRepository projectionRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        projectionRepository.deleteAll();
    }

    @Test
    void whenGameIsRegistered_thenEventIsPublished_andProjectionIsCreated() {
        RegisterInternalGameCommand command = createTestGameCommand();

        Game registeredGame = registerInternalGameUseCase.registerGame(command);

        assertThat(registeredGame).isNotNull();
        assertThat(registeredGame.getGameId()).isNotNull();
        assertThat(registeredGame.getName()).isEqualTo(command.name());

        // Registered games are ACTIVE by default - developer account approval flow not implemented yet
        assertThat(registeredGame.getStatus()).isEqualTo(GameStatus.ACTIVE);

        await().atMost(ASYNC_TIMEOUT_SECONDS, SECONDS)
                .untilAsserted(() -> {
                    Optional<GameInfoProjection> projection =
                            projectionRepository.findById(registeredGame.getGameId().gameId());

                    assertThat(projection).isPresent();
                    assertThat(projection.get().getGameId())
                            .isEqualTo(registeredGame.getGameId().gameId());
                    assertThat(projection.get().getName())
                            .isEqualTo(registeredGame.getName());
                    assertThat(projection.get().getDescription())
                            .isEqualTo(registeredGame.getDescription());
                    assertThat(projection.get().getStatus())
                            .isEqualTo(registeredGame.getStatus().name());
                });
    }

    @Test
    void whenMultipleGamesRegistered_thenAllProjectionsCreated() {
        RegisterInternalGameCommand command1 = createTestGameCommand("Game 1", "Description 1");
        RegisterInternalGameCommand command2 = createTestGameCommand("Game 2", "Description 2");

        Game game1 = registerInternalGameUseCase.registerGame(command1);
        Game game2 = registerInternalGameUseCase.registerGame(command2);

        await().atMost(ASYNC_TIMEOUT_SECONDS, SECONDS)
                .untilAsserted(() -> {
                    List<GameInfoProjection> allProjections = projectionRepository.findAll();
                    assertThat(allProjections).hasSize(2);
                    assertThat(allProjections)
                            .extracting(GameInfoProjection::getGameId)
                            .containsExactlyInAnyOrder(
                                    game1.getGameId().gameId(),
                                    game2.getGameId().gameId()
                            );
                });
    }

    @Test
    void whenDuplicateEventReceived_thenProjectionNotDuplicated() {
        RegisterInternalGameCommand command = createTestGameCommand();
        Game game = registerInternalGameUseCase.registerGame(command);

        await().atMost(ASYNC_TIMEOUT_SECONDS, SECONDS)
                .until(() -> projectionRepository.existsById(game.getGameId().gameId()));

        long initialCount = projectionRepository.count();

        GameRegisteredEvent duplicateEvent = new GameRegisteredEvent(
                game.getGameId().gameId(),
                game.getName(),
                game.getDescription(),
                game.getStatus().name(),
                game.getRegisteredAt(),
                String.valueOf(game.getRenderType())
        );

        applicationEventPublisher.publishEvent(duplicateEvent);

        await().atMost(ASYNC_TIMEOUT_SECONDS, SECONDS)
                .during(2, SECONDS)
                .untilAsserted(() -> {
                    long currentCount = projectionRepository.count();
                    assertThat(currentCount).isEqualTo(initialCount);
                    assertThat(currentCount).isEqualTo(1);
                });
    }

    @Test
    void whenGameRegistrationFails_thenNoProjectionCreated() {
        RegisterInternalGameCommand invalidCommand = new RegisterInternalGameCommand(
                null,
                "Description",
                RenderType.NATIVE_TICTACTOE,
                createTestGameRules(),
                createTestPlayerConfiguration(),
                new ArrayList<>(),
                createTestGameMetaData()
        );

        assertThatThrownBy(() -> registerInternalGameUseCase.registerGame(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(projectionRepository.count()).isZero();
    }

    @Test
    void whenGameIsRegistered_thenProjectionContainsAllCorrectData() {
        String expectedName = "Test Game " + UUID.randomUUID();
        String expectedDescription = "Detailed test description";
        RegisterInternalGameCommand command = createTestGameCommand(expectedName, expectedDescription);

        Game registeredGame = registerInternalGameUseCase.registerGame(command);

        await().atMost(ASYNC_TIMEOUT_SECONDS, SECONDS)
                .untilAsserted(() -> {
                    Optional<GameInfoProjection> projection =
                            projectionRepository.findById(registeredGame.getGameId().gameId());

                    assertThat(projection).isPresent();

                    GameInfoProjection proj = projection.get();
                    assertThat(proj.getGameId()).isEqualTo(registeredGame.getGameId().gameId());
                    assertThat(proj.getName()).isEqualTo(expectedName);
                    assertThat(proj.getDescription()).isEqualTo(expectedDescription);
                    assertThat(proj.getStatus()).isEqualTo(registeredGame.getStatus().name());
                    assertThat(proj.getRegisteredAt()).isNotNull();
                });
    }

    private RegisterInternalGameCommand createTestGameCommand() {
        return createTestGameCommand("Test Game" + UUID.randomUUID(), "Test Description");
    }

    private RegisterInternalGameCommand createTestGameCommand(String name, String description) {
        return new RegisterInternalGameCommand(
                name,
                description,
                RenderType.NATIVE_TICTACTOE,
                createTestGameRules(),
                createTestPlayerConfiguration(),
                new ArrayList<>(),
                createTestGameMetaData()
        );
    }

    private GameRules createTestGameRules() {
        return new GameRules(
                "Basic rules for testing",
                "https://rules.com",
                "Basic rule summary");
    }

    private PlayerConfiguration createTestPlayerConfiguration() {
        return new PlayerConfiguration(
                2,
                2,
                Set.of(PlayerConfiguration.PlayerType.HUMAN));
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
                "Test image.png"
        );
    }
}
