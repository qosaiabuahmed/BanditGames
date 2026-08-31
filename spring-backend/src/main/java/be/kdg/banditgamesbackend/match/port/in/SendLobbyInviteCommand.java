package be.kdg.banditgamesbackend.match.port.in;

import org.springframework.util.Assert;

import java.util.UUID;

public record SendLobbyInviteCommand(
        UUID lobbyId,
        UUID fromUserId,
        UUID toUserId
) {
    public SendLobbyInviteCommand {
        Assert.notNull(lobbyId, "Lobby ID cannot be null");
        Assert.notNull(fromUserId, "From user ID cannot be null");
        Assert.notNull(toUserId, "To user ID cannot be null");
        Assert.isTrue(!fromUserId.equals(toUserId), "Cannot invite yourself");
    }
}