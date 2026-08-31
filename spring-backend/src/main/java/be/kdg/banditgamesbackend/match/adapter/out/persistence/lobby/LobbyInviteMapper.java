package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.match.domain.LobbyId;
import be.kdg.banditgamesbackend.match.domain.LobbyInvite;
import org.springframework.stereotype.Component;

@Component
public class LobbyInviteMapper {

    public LobbyInviteJpaEntity toJpaEntity(LobbyInvite invite) {
        return new LobbyInviteJpaEntity(
                invite.getInviteId().value(),
                invite.getLobbyId().value(),
                invite.getFromUserId(),
                invite.getToUserId(),
                invite.getStatus(),
                invite.getCreatedAt(),
                invite.getRespondedAt(),
                invite.getExpiresAt()
        );
    }

    public LobbyInvite toDomain(LobbyInviteJpaEntity entity) {
        return LobbyInvite.hydrate(
                new LobbyInviteId(entity.getInviteId()),
                new LobbyId(entity.getLobbyId()),
                entity.getFromUserId(),
                entity.getToUserId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt(),
                entity.getExpiresAt()
        );
    }
}