package be.kdg.banditgamesbackend.notification.port.in;

public interface MarkNotificationAsReadUseCase {
    void markAsRead(MarkNotificationAsReadCommand command);
}
