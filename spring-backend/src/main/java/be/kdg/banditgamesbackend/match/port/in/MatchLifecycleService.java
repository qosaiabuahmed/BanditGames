package be.kdg.banditgamesbackend.match.port.in;

public interface MatchLifecycleService {
    void start(String gameId, Object payload);
    void finish(String gameId, Object payload);
    void createExternalMatch(CreateExternalMatchCommand command);
}
