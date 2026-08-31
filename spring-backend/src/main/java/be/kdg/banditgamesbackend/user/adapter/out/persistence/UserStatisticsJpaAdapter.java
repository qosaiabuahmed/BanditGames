package be.kdg.banditgamesbackend.user.adapter.out.persistence;

import be.kdg.banditgamesbackend.user.domain.UserStatistics;
import be.kdg.banditgamesbackend.user.domain.UserStatus;
import be.kdg.banditgamesbackend.user.port.out.LoadUserStatisticsPort;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserStatisticsJpaAdapter implements LoadUserStatisticsPort {

    private final UserJpaRepository userJpaRepository;

    public UserStatisticsJpaAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public UserStatistics loadUserStatistics() {
        long totalUsers = userJpaRepository.count();
        long onlineUsers = userJpaRepository.countByStatus(UserStatus.ONLINE);
        long offlineUsers = userJpaRepository.countByStatus(UserStatus.OFFLINE);

        List<UserStatistics.UserRegistrationTrend> registrationTrend =
            userJpaRepository.findRegistrationTrendLast30Days()
                .stream()
                .map(result -> new UserStatistics.UserRegistrationTrend(
                    ((Date) result[0]).toLocalDate(),
                    ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());

        return new UserStatistics(
            totalUsers,
            onlineUsers,
            offlineUsers,
            registrationTrend
        );
    }
}