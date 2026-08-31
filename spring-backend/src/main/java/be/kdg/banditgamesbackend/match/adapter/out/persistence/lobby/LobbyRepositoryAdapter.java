package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.match.domain.Lobby;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.LobbyStatus;
import be.kdg.banditgamesbackend.match.port.out.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LobbyRepositoryAdapter implements LobbyRepository {
    private final LobbyJpaRepository lobbyJpaRepository;
    private final LobbyJpaMapper mapper;

    @Override
    public Lobby save(Lobby lobby) {
        LobbyJpaEntity saved = lobbyJpaRepository.save(mapper.toEntity(lobby));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Lobby> findOpenLobbyByGameId(UUID gameId) {
        return lobbyJpaRepository.findFirstByGameIdAndStatusOrderByCreatedAt(gameId, LobbyStatus.OPEN)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Lobby> findById(LobbyId lobbyId) {
        return lobbyJpaRepository.findById(lobbyId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Lobby> findOpenLobbyByUser(UUID userId) {
        return lobbyJpaRepository.findOpenLobbyByUser(userId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Lobby> findOpenLobbiesByGameId(UUID gameId) {
        return lobbyJpaRepository.findByGameIdAndStatus(gameId, LobbyStatus.OPEN)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
