package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.gamemetadata.api.GameInfoDto;
import be.kdg.banditgamesbackend.gamemetadata.api.GameLookupService;
import be.kdg.banditgamesbackend.gamemetadata.api.PlayerConfigurationInfo;
import be.kdg.banditgamesbackend.match.adapter.out.messaging.MatchAssignedEvent;
import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.MatchId;
import be.kdg.banditgamesbackend.match.domain.MatchPlayer;
import be.kdg.banditgamesbackend.match.port.in.CreateMatchCommand;
import be.kdg.banditgamesbackend.match.port.in.CreateMatchUseCase;
import be.kdg.banditgamesbackend.match.port.out.SaveMatchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService implements CreateMatchUseCase {

    private final SaveMatchPort saveMatchPort;
    private final GameLookupService gameLookupService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Match createMatch(CreateMatchCommand command) {
        validatePlayerIds(command.playerIds());

        GameInfoDto game = gameLookupService.findGameById(command.gameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found for id " + command.gameId()));

        PlayerConfigurationInfo configuration = game.playerConfiguration();
        List<UUID> distinctPlayerIds = new ArrayList<>(new LinkedHashSet<>(command.playerIds()));
        int playerCount = distinctPlayerIds.size();
        if (playerCount < configuration.minPlayers() || playerCount > configuration.maxPlayers()) {
            throw new IllegalArgumentException("Player count %d outside supported range %d-%d for game %s"
                    .formatted(playerCount, configuration.minPlayers(), configuration.maxPlayers(), game.name()));
        }

        List<MatchPlayer> players = IntStream.range(0, distinctPlayerIds.size())
                .mapToObj(i -> new MatchPlayer(distinctPlayerIds.get(i), i + 1))
                .toList();

        Match match = Match.create(new MatchId(UUID.randomUUID()), command.gameId(), players);
        Match savedMatch = saveMatchPort.save(match);
        publishMatchAssigned(savedMatch, configuration);
        log.info("Created match {} for game {} with {} players", savedMatch.getMatchId(), game.name(), players.size());
        return savedMatch;
    }

    private void publishMatchAssigned(Match match, PlayerConfigurationInfo configuration) {
        List<MatchAssignedEvent.PlayerSeat> seats = match.getPlayers().stream()
                .map(player -> new MatchAssignedEvent.PlayerSeat(player.userId(), player.seatNumber()))
                .toList();

        MatchAssignedEvent event = new MatchAssignedEvent(
                match.getMatchId().value(),
                match.getGameId(),
                seats,
                configuration
        );

        applicationEventPublisher.publishEvent(event);
    }

    private void validatePlayerIds(List<UUID> playerIds) {
        if (playerIds.isEmpty()) {
            throw new IllegalArgumentException("At least one playerId is required");
        }
        Set<UUID> duplicates = new LinkedHashSet<>();
        Set<UUID> seen = new LinkedHashSet<>();
        for (UUID id : playerIds) {
            if (!seen.add(id)) {
                duplicates.add(id);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate playerIds are not allowed: " + duplicates);
        }
    }
}
