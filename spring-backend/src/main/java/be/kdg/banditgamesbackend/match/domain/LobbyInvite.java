package be.kdg.banditgamesbackend.match.domain;

import be.kdg.banditgamesbackend.common.domain.LobbyInviteId;
import be.kdg.banditgamesbackend.common.events.LobbyInviteAcceptedEvent;
import be.kdg.banditgamesbackend.common.events.LobbyInviteDeclinedEvent;
import be.kdg.banditgamesbackend.common.events.LobbyInviteSentEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Getter
public class LobbyInvite {

    private final LobbyInviteId inviteId;
    private final LobbyId lobbyId;
    private final UUID fromUserId;
    private final UUID toUserId;
    private LobbyInviteStatus status;
    private final Instant createdAt;
    private Instant respondedAt;
    private final Instant expiresAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private LobbyInvite(LobbyInviteId inviteId,
                        LobbyId lobbyId,
                        UUID fromUserId,
                        UUID toUserId,
                        LobbyInviteStatus status,
                        Instant createdAt,
                        Instant respondedAt,
                        Instant expiresAt) {
        this.inviteId = Objects.requireNonNull(inviteId, "inviteId is required");
        this.lobbyId = Objects.requireNonNull(lobbyId, "lobbyId is required");
        this.fromUserId = Objects.requireNonNull(fromUserId, "fromUserId is required");
        this.toUserId = Objects.requireNonNull(toUserId, "toUserId is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.respondedAt = respondedAt;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
    }

    public static LobbyInvite createNew(LobbyId lobbyId,
                                        UUID fromUserId,
                                        UUID toUserId,
                                        UUID gameId,
                                        String gameName) {
        LobbyInviteId id = LobbyInviteId.newId();
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(600);

        LobbyInvite invite = new LobbyInvite(
                id,
                lobbyId,
                fromUserId,
                toUserId,
                LobbyInviteStatus.PENDING,
                now,
                null,
                expires
        );

        invite.domainEvents.add(new LobbyInviteSentEvent(
                id.value(),
                lobbyId.value(),
                gameId,
                gameName,
                fromUserId,
                toUserId,
                now
        ));

        return invite;
    }

    public static LobbyInvite hydrate(LobbyInviteId inviteId,
                                      LobbyId lobbyId,
                                      UUID fromUserId,
                                      UUID toUserId,
                                      LobbyInviteStatus status,
                                      Instant createdAt,
                                      Instant respondedAt,
                                      Instant expiresAt) {
        return new LobbyInvite(inviteId, lobbyId, fromUserId, toUserId,
                status, createdAt, respondedAt, expiresAt);
    }

    public void accept() {
        if (status != LobbyInviteStatus.PENDING) {
            throw new IllegalStateException("Can only accept pending invites");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot accept expired invite");
        }
        this.status = LobbyInviteStatus.ACCEPTED;
        this.respondedAt = Instant.now();

        String gameName = null;

        domainEvents.add(new LobbyInviteAcceptedEvent(
                inviteId.value(),
                lobbyId.value(),
                gameName,
                fromUserId,
                toUserId,
                respondedAt
        ));
    }

    public void decline() {
        if (status != LobbyInviteStatus.PENDING) {
            throw new IllegalStateException("Can only decline pending invites");
        }
        this.status = LobbyInviteStatus.DECLINED;
        this.respondedAt = Instant.now();

        domainEvents.add(new LobbyInviteDeclinedEvent(
                inviteId.value(),
                lobbyId.value(),
                fromUserId,
                toUserId,
                respondedAt
        ));
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == LobbyInviteStatus.PENDING && !isExpired();
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}