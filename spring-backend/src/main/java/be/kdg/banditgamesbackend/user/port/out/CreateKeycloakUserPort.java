package be.kdg.banditgamesbackend.user.port.out;

public interface CreateKeycloakUserPort {
    String createUser(String username, String email, String password);
}