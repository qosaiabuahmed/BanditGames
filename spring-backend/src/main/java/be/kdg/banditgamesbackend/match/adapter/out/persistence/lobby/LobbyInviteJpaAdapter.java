package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.port.out.LoadLobbyInvitePort;
import be.kdg.banditgamesbackend.match.port.out.SaveLobbyInvitePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class LobbyInviteJpaAdapter implements LoadLobbyInvitePort, SaveLobbyInvitePort {

    private final LobbyInviteJpaRepository repository;
    private final LobbyInviteMapper mapper;

    @Override
    public Optional<LobbyInvite> findById(LobbyInviteId inviteId) {
        return repository.findById(inviteId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<LobbyInvite> findPendingByToUserId(UUID toUserId) {
        return repository.findPendingByToUserId(toUserId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<LobbyInvite> findPendingInvite(LobbyId lobbyId, UUID toUserId) {
        return repository.findPendingInvite(lobbyId.value(), toUserId)
                .map(mapper::toDomain);
    }

    @Override
    public List<LobbyInvite> findByLobbyId(LobbyId lobbyId) {
        return repository.findByLobbyId(lobbyId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public LobbyInvite save(LobbyInvite invite) {
        LobbyInviteJpaEntity entity = mapper.toJpaEntity(invite);
        repository.save(entity);
        return invite;
    }
}