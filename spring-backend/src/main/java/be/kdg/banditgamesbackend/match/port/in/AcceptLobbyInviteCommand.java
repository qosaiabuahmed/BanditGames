package be.kdg.banditgamesbackend.match.port.in;

import org.springframework.util.Assert;

import java.util.UUID;

public record AcceptLobbyInviteCommand(
        UUID inviteId,
        UUID userId
) {
    public AcceptLobbyInviteCommand {
        Assert.notNull(inviteId, "Invite ID cannot be null");
        Assert.notNull(userId, "User ID cannot be null");
    }
}