package be.kdg.banditgamesbackend.match.adapter.in;

import be.kdg.banditgamesbackend.match.api.LobbyInfo;
import be.kdg.banditgamesbackend.match.api.LobbyLookupService;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.port.out.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LobbyLookupServiceAdapter implements LobbyLookupService {

    private final LobbyRepository lobbyRepository;

    @Override
    public Optional<LobbyInfo> findLobbyById(UUID lobbyId) {
        return lobbyRepository.findById(new LobbyId(lobbyId))
                .map(lobby -> new LobbyInfo(
                        lobby.getLobbyId().value(),
                        lobby.getGameId()
                ));
    }
}