package be.kdg.banditgamesbackend.notification.adapter.in.web;

import be.kdg.banditgamesbackend.notification.adapter.in.response.NotificationCountDto;
import be.kdg.banditgamesbackend.notification.adapter.in.response.NotificationDto;
import be.kdg.banditgamesbackend.notification.domain.Notification;
import be.kdg.banditgamesbackend.notification.domain.NotificationStatus;
import be.kdg.banditgamesbackend.notification.port.in.*;
import be.kdg.banditgamesbackend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsQuery getNotificationsQuery;
    private final MarkAllNotificationsAsReadUseCase markAllNotificationsAsReadUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status) {
        UUID userId = jwtUtils.extractUserId(jwt);

        List<Notification> notifications;

        if (status != null && !status.isBlank()) {
            // Filter by status (unread, read, or deleted)
            NotificationStatus notificationStatus = parseStatus(status);
            notifications = getNotificationsQuery.getNotificationsByStatus(userId, notificationStatus);
        } else {
            // Default: return all (unread + read, excluding deleted)
            notifications = getNotificationsQuery.getNotifications(userId);
        }

        List<NotificationDto> dtos = notifications.stream()
                .map(NotificationDto::from)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    private NotificationStatus parseStatus(String status) {
        try {
            return NotificationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: " + status + ". Valid values are: UNREAD, READ, DELETED"
            );
        }
    }

    @GetMapping("/count")
    public ResponseEntity<NotificationCountDto> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        long count = getNotificationsQuery.getUnreadCount(userId);
        return ResponseEntity.ok(new NotificationCountDto(count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        MarkNotificationAsReadCommand command = new MarkNotificationAsReadCommand(notificationId, userId);
        markNotificationAsReadUseCase.markAsRead(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        markAllNotificationsAsReadUseCase.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId,  @AuthenticationPrincipal Jwt jwt) {
        UUID userId = jwtUtils.extractUserId(jwt);
        DeleteNotificationCommand command = new DeleteNotificationCommand(notificationId, userId);
        deleteNotificationUseCase.deleteNotification(command);
        return ResponseEntity.noContent().build();
    }
}
