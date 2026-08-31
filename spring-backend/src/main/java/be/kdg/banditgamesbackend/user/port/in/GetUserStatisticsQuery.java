package be.kdg.banditgamesbackend.user.port.in;

import be.kdg.banditgamesbackend.user.domain.UserStatistics;

public interface GetUserStatisticsQuery {
    UserStatistics getUserStatistics();
}