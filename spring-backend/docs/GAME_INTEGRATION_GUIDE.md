# Game Integration Guide (Platform SDK)

Welcome, game developer! This guide will show you how to integrate your game with the BanditGames platform using our Java SDK.

## Prerequisites

-   A Spring Boot application (Java 17+ recommended).
-   Access to the BanditGames Platform and its RabbitMQ instance.

## Step 1: Add the SDK Dependency

Add the `bandit-games-sdk` to your project's `build.gradle`:

```gradle
dependencies {
    implementation 'be.kdg:bandit-games-sdk:1.0.0'
}
```

## Step 2: Configure Your Game

Add the following properties to your `application.yml` or `application.properties`:

```yaml
banditgames:
  sdk:
    platform-url: http://platform.banditgames.be
    game:
      name: "Super Chess"
      description: "A futuristic chess variant"
      render-type: EXTERNAL_IFRAME
      rules:
        summary: "Standard chess rules with a twist."
        rules-text: "Full rules here..."
      achievements:
        - achievement-id: "first-checkmate"
          name: "Checkmate!"
          description: "Win your first game."
      player-config:
        min-players: 2
        max-players: 2
      meta-data:
        category: "Strategy"
        theme: "Sci-Fi"
```

## Step 3: Publish Events

Inject the `BanditGamesEventPublisher` into your game logic to notify the platform of player actions:

```java
@Service
@RequiredArgsConstructor
public class GameService {
    private final BanditGamesEventPublisher eventPublisher;

    public void makeMove(UUID matchId, String username, Move move) {
        // ... game logic ...
        
        SDKMoveMadeEvent event = SDKMoveMadeEvent.builder()
            .matchId(matchId)
            .playerName(username)
            .moveNumber(move.getNumber())
            .fromSquare(move.getFrom())
            .toSquare(move.getTo())
            .moveNotation(move.getNotation())
            .boardStateAfter(move.getBoardState())
            .build();

        eventPublisher.publishMoveMade(event);
    }
}
```

## Step 4: Run Your Game

Once your application starts, the SDK will automatically send a registration request to the platform. You can check your application logs for:
`Successfully registered game 'Super Chess'`

## Advanced: Customizing Registration

If you prefer to register the game manually or at a different time, you can disable auto-registration:

```yaml
banditgames:
  sdk:
    auto-register: false
```

Then, inject and call `GameRegistrationClient.registerGame()` whenever you are ready.
