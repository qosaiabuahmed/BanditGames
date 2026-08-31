package be.kdg.banditgamesbackend.notification.port.in;

public interface DeleteNotificationUseCase {
    void deleteNotification(DeleteNotificationCommand command);
}
