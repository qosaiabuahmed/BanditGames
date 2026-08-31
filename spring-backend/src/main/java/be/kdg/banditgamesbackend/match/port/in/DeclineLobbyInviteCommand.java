package be.kdg.banditgamesbackend.match.port.in;

import org.springframework.util.Assert;

import java.util.UUID;

public record DeclineLobbyInviteCommand(
        UUID inviteId,
        UUID userId
) {
    public DeclineLobbyInviteCommand {
        Assert.notNull(inviteId, "Invite ID cannot be null");
        Assert.notNull(userId, "User ID cannot be null");
    }
}