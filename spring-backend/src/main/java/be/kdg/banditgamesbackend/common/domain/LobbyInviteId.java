package be.kdg.banditgamesbackend.common.domain;

import java.util.UUID;

public record LobbyInviteId(UUID value) {
    public LobbyInviteId {
        if (value == null) {
            throw new IllegalArgumentException("LobbyInvite ID cannot be null");
        }
    }

    public static LobbyInviteId of(String uuidString) {
        return new LobbyInviteId(UUID.fromString(uuidString));
    }

    public static LobbyInviteId newId() {
        return new LobbyInviteId(UUID.randomUUID());
    }
}