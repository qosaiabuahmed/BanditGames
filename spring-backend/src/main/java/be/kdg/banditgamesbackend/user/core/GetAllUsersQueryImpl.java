package be.kdg.banditgamesbackend.user.core;

import be.kdg.banditgamesbackend.user.domain.User;
import be.kdg.banditgamesbackend.user.port.in.GetAllUsersQuery;
import be.kdg.banditgamesbackend.user.port.out.LoadUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetAllUsersQueryImpl implements GetAllUsersQuery {

    private final LoadUserPort loadUserPort;

    public GetAllUsersQueryImpl(LoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return loadUserPort.loadAll();
    }
}