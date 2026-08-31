package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.user.domain.User;

public interface SyncUserFromJwtUseCase {
    User syncUserFromJwt(SyncUserFromJwtCommand command);
}