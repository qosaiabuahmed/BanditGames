package be.kdg.banditgamesbackend.match.adapter.in.web;

import be.kdg.banditgamesbackend.match.domain.exceptions.AlreadyInLobbyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackageClasses = LobbyController.class)
public class MatchExceptionHandler {

    @ExceptionHandler(AlreadyInLobbyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleAlreadyInLobby(AlreadyInLobbyException ex) {
        return Map.of(
                "error", "user_already_in_lobby",
                "message", ex.getMessage(),
                "lobbyId", ex.getLobbyId()
        );
    }
}
