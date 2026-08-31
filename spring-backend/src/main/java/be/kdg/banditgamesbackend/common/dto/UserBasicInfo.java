package be.kdg.banditgamesbackend.common.dto;

import java.util.UUID;

public record UserBasicInfo(
        UUID userId,
        String username,
        String playerTag
//        String avatar,
//        String status
) {}
