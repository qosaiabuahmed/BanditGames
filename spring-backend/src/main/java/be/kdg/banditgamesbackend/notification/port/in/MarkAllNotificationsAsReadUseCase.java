package be.kdg.banditgamesbackend.notification.port.in;

import java.util.UUID;

public interface MarkAllNotificationsAsReadUseCase {
    void markAllAsRead(UUID userId);
}
