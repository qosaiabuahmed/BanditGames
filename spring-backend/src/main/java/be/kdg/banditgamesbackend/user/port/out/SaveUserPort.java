package be.kdg.banditgamesbackend.user.port.out;

import be.kdg.banditgamesbackend.user.domain.User;

public interface SaveUserPort {
    User save(User user);
}