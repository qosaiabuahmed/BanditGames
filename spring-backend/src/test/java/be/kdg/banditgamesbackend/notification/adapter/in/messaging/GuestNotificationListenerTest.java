package be.kdg.banditgamesbackend.notification.adapter.in.messaging;

import be.kdg.banditgamesbackend.common.events.GuestPlayersDetectedEvent;
import be.kdg.banditgamesbackend.notification.api.CreateNotificationRequest;
import be.kdg.banditgamesbackend.notification.api.NotificationService;
import be.kdg.banditgamesbackend.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GuestNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GuestNotificationListener listener;

    @Test
    void handleGuestPlayersDetected_WithGuestPlayer_ShouldSendNotification() {
        UUID guestId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        GuestPlayersDetectedEvent event = new GuestPlayersDetectedEvent(
                matchId,
                List.of(guestId)
        );

        listener.handleGuestPlayersDetected(event);

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).sendNotification(captor.capture());
        CreateNotificationRequest request = captor.getValue();
        assertThat(request.recipientId()).isEqualTo(guestId);
        assertThat(request.type()).isEqualTo(NotificationType.GUEST_WELCOME);
    }

    @Test
    void handleGuestPlayersDetected_WithEmptyGuestList_ShouldNotSendNotification() {
        GuestPlayersDetectedEvent event = new GuestPlayersDetectedEvent(
                UUID.randomUUID(),
                List.of()
        );

        listener.handleGuestPlayersDetected(event);

        verifyNoInteractions(notificationService);
    }

}
