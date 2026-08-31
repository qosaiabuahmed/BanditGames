package be.kdg.banditgamesbackend.match.adapter.in.web;

import java.util.UUID;

public record SendLobbyInviteRequest(
        UUID toUserId
) {}