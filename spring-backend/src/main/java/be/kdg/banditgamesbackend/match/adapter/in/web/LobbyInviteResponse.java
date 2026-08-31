package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.domain.LobbyInvite;

import java.util.UUID;

public record LobbyInviteResponse(
        UUID inviteId,
        UUID lobbyId,
        UUID fromUserId,
        UUID toUserId,
        String status,
        String createdAt,
        String expiresAt
) {
    public static LobbyInviteResponse from(LobbyInvite invite) {
        return new LobbyInviteResponse(
                invite.getInviteId().value(),
                invite.getLobbyId().value(),
                invite.getFromUserId(),
                invite.getToUserId(),
                invite.getStatus().name(),
                invite.getCreatedAt().toString(),
                invite.getExpiresAt().toString()
        );
    }
}