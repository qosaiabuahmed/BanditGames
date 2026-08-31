package be.kdg.banditgamesbackend.notification.port.in;

import be.kdg.banditgamesbackend.notification.domain.Notification;

public interface CreateNotificationUseCase {
    Notification createNotification(CreateNotificationCommand command);
}
