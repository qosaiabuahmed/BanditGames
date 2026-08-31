package be.kdg.banditgamesbackend.match.core;

import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import be.kdg.banditgamesbackend.match.port.in.GetPendingLobbyInvitesQuery;
import be.kdg.banditgamesbackend.match.port.out.LoadLobbyInvitePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPendingLobbyInvitesQueryImpl implements GetPendingLobbyInvitesQuery {

    private final LoadLobbyInvitePort loadLobbyInvitePort;

    @Override
    public List<LobbyInvite> getPendingInvites(UUID userId) {
        return loadLobbyInvitePort.findPendingByToUserId(userId).stream()
                .filter(LobbyInvite::isPending)
                .toList();
    }
}