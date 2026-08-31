package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.user.domain.User;

import java.util.List;

public interface GetAllUsersQuery {
    List<User> getAllUsers();
}