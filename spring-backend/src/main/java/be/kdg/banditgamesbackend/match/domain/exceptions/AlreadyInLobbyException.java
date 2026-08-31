package be.kdg.banditgamesbackend.match.domain.exceptions;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AlreadyInLobbyException extends RuntimeException {
    private final UUID lobbyId;

    public AlreadyInLobbyException(UUID lobbyId) {
        super("User already in open lobby " + lobbyId);
        this.lobbyId = lobbyId;
    }

}
