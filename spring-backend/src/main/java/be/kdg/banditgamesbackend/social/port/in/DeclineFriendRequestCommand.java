package be.kdg.banditgamesbackend.social.port.in;

import java.util.UUID;

public record DeclineFriendRequestCommand(
        UUID friendshipId,
        UUID userId
) {}
