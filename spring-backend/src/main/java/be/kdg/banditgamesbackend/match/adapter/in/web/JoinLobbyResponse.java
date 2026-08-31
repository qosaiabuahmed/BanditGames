package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.adapter.in.response.MatchResponse;
import be.kdg.banditgamesbackend.match.domain.Match;
import be.kdg.banditgamesbackend.match.domain.Lobby;

public record JoinLobbyResponse(
        LobbyResponse lobby,
        MatchResponse match
) {
    public static JoinLobbyResponse from(Lobby lobby, Match match) {
        return new JoinLobbyResponse(
                LobbyResponse.from(lobby),
                match != null ? MatchResponse.from(match) : null
        );
    }
}
