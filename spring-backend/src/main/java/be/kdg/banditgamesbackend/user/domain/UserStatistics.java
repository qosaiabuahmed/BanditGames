package be.kdg.banditgamesbackend.user.domain;

import java.time.LocalDate;

public record UserStatistics(
    long totalUsers,
    long onlineUsers,
    long offlineUsers,
    java.util.List<UserRegistrationTrend> registrationTrend
) {
    public static class UserRegistrationTrend {
        private final LocalDate date;
        private final long count;

        public UserRegistrationTrend(LocalDate date, long count) {
            this.date = date;
            this.count = count;
        }

        public LocalDate getDate() {
            return date;
        }

        public long getCount() {
            return count;
        }
    }
}