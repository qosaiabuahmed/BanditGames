package be.kdg.banditgamesbackend.game.domain;

import be.kdg.banditgamesbackend.gamemetadata.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @Test
    void register_WithValidData_ShouldCreateActiveGame() {
        GameId gameId = new GameId(UUID.randomUUID());
        String name = "Test Game";
        String description = "Description";
        RenderType renderType = RenderType.NATIVE_TICTACTOE;
        GameRules rules = new GameRules("Rules", "url", "desc");
        PlayerConfiguration playerConfig = new PlayerConfiguration(2, 2, Set.of(PlayerConfiguration.PlayerType.HUMAN));
        GameMetaData metaData = new GameMetaData("Cat", "Theme", "Dev", "Pub", 2024, 60, "Easy", null, null);

        Game game = Game.register(gameId, name, description, rules, new ArrayList<>(), playerConfig, renderType, metaData);

        assertThat(game.getGameId()).isEqualTo(gameId);
        assertThat(game.getName()).isEqualTo(name);
        assertThat(game.getStatus()).isEqualTo(GameStatus.ACTIVE);
    }

    @Test
    void registerExternal_ShouldCreateActiveExternalGame() {
        GameId gameId = new GameId(UUID.randomUUID());
        String frontendUrl = "http://chess.com";
        String name = "Chess Game";
        String description = "Description";
        RenderType renderType = RenderType.EXTERNAL_IFRAME;
        GameRules rules = new GameRules("Rules", "url", "desc");
        PlayerConfiguration playerConfig = new PlayerConfiguration(2, 2, Set.of(PlayerConfiguration.PlayerType.HUMAN));
        GameMetaData metaData = new GameMetaData("Cat", "Theme", "Dev", "Pub", 2024, 60, "Easy", frontendUrl, null);
        Game game = Game.registerExternal(gameId, name, description, rules, new ArrayList<>(), playerConfig, metaData, LocalDateTime.now(), renderType);

        assertThat(game.getGameId()).isEqualTo(gameId);
        assertThat(game.getMetaData().frontendUrl()).isEqualTo(frontendUrl);
        assertThat(game.isExternallyHosted()).isTrue();
        assertThat(game.getStatus()).isEqualTo(GameStatus.ACTIVE);
    }

    @Test
    void deactivate_ShouldChangeStatusToInactive() {
        Game game = createTestGame();
        game.deactivate();
        assertThat(game.getStatus()).isEqualTo(GameStatus.INACTIVE);
    }

    @Test
    void deprecate_ShouldChangeStatusToDeprecated() {
        Game game = createTestGame();
        game.deprecate();
        assertThat(game.getStatus()).isEqualTo(GameStatus.DEPRECATED);
    }

    @Test
    void activate_WhenInactive_ShouldWork() {
        Game game = createTestGame();
        game.deactivate();
        game.activate();
        assertThat(game.getStatus()).isEqualTo(GameStatus.ACTIVE);
    }

    @Test
    void activate_WhenDeprecated_ShouldThrowException() {
        Game game = createTestGame();
        game.deprecate();
        assertThatThrownBy(game::activate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rename_WithValidName_ShouldUpdateName() {
        Game game = createTestGame();
        game.rename("New Name");
        assertThat(game.getName()).isEqualTo("New Name");
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
