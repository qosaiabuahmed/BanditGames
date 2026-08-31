package be.kdg.banditgamesbackend.user.port.out;

import be.kdg.banditgamesbackend.user.domain.UserStatistics;

public interface LoadUserStatisticsPort {
    UserStatistics loadUserStatistics();
}