package be.kdg.banditgamesbackend.social.adapter.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendDto(
        UUID friendshipId,
        UUID userId,
        String username,
        String playerTag,
//        String avatar,
        String status,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt
) {}
