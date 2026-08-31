package be.kdg.banditgamesbackend.social.adapter.in.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendFriendRequestDto(
        @NotNull(message = "Target user Id is required")
        UUID toUserId
) {}
