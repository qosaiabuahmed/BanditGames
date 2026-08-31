package be.kdg.banditgamesbackend.notification.port.out;

import be.kdg.banditgamesbackend.notification.domain.Notification;

public interface NotificationPublisher {

    void publishToUser(Notification notification);
    void publishToAll(Notification notification);

}
