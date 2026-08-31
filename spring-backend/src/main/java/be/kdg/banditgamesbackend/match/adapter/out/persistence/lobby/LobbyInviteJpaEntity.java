package be.kdg.banditgamesbackend.match.adapter.out.persistence.lobby;

import be.kdg.banditgamesbackend.match.domain.LobbyInviteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lobby_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LobbyInviteJpaEntity {

    @Id
    private UUID inviteId;

    private UUID lobbyId;
    private UUID fromUserId;
    private UUID toUserId;

    @Enumerated(EnumType.STRING)
    private LobbyInviteStatus status;

    private Instant createdAt;
    private Instant respondedAt;
    private Instant expiresAt;
}