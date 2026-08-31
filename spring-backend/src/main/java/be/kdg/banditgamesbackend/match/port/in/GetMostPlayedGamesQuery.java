package be.kdg.banditgamesbackend.match.port.in;

import be.kdg.banditgamesbackend.match.domain.GamePlayCount;

import java.util.List;

public interface GetMostPlayedGamesQuery {
    List<GamePlayCount> getMostPlayedGames(int limit);
}