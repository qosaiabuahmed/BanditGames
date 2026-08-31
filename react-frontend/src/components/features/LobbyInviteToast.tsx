import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../notifications/NotificationContent';
import { LobbyInviteService } from '../../services/lobbyInviteService';
import { NotificationType } from '../../types/notification';
import type { NotificationDto } from '../../types/notification';

export default function LobbyInviteToast() {
  const navigate = useNavigate();
  const { notifications, markAsRead } = useNotifications();
  const [visibleInvites, setVisibleInvites] = useState<NotificationDto[]>([]);
  const [processing, setProcessing] = useState<string | null>(null);

  useEffect(() => {
    // Filter for unread game invitation notifications
    const inviteNotifications = notifications
      .filter(
        (n) =>
          n.status === 'UNREAD' &&
          n.type === NotificationType.GAME_INVITATION_RECEIVED
      )
      .slice(0, 2); // Show max 2 at a time

    setVisibleInvites(inviteNotifications);

    // Auto-hide after 15 seconds (longer than regular notifications because of actions)
    if (inviteNotifications.length > 0) {
      const timer = setTimeout(() => {
        setVisibleInvites([]);
      }, 15000);

      return () => clearTimeout(timer);
    }
  }, [notifications]);

  const handleAccept = async (notification: NotificationDto) => {
    setProcessing(notification.notificationId);

    try {
      // Get invite ID from notification metadata
      let inviteId = notification.metadata.inviteId;

      // Fallback: if no inviteId, fetch pending invites and match by lobbyId
      if (!inviteId && notification.metadata.lobbyId) {
        const pendingInvites = await LobbyInviteService.getPendingInvites();
        const matchingInvite = pendingInvites.find(
          (inv) => inv.lobbyId === notification.metadata.lobbyId
        );
        if (matchingInvite) {
          inviteId = matchingInvite.inviteId;
        }
      }

      if (!inviteId) {
        console.error('Could not find invite ID');
        return;
      }

      const response = await LobbyInviteService.acceptInvite(inviteId);

      // Mark notification as read
      await markAsRead(notification.notificationId);

      // Remove from visible toasts
      setVisibleInvites((prev) =>
        prev.filter((n) => n.notificationId !== notification.notificationId)
      );

      // Debug: log the full response
      console.log('Accept invite response:', response);

      // Navigate based on response
      if (response.match) {
        // Match started - navigate with match info
        console.log('Match found! MatchID:', response.match.matchId, 'GameID:', response.match.gameId);
        navigate('/dashboard', {
          replace: true,
          state: {
            matchId: response.match.matchId,
            gameId: response.match.gameId,
            showMatchFound: true
          }
        });
      } else {
        // Joined lobby - navigate with lobby info
        console.log('Joined lobby:', response.lobby);
        navigate('/dashboard', {
          replace: true,
          state: {
            lobby: response.lobby,
            showLobbyWaiting: true
          }
        });
      }
    } catch (error) {
      console.error('Failed to accept invite:', error);
      // Don't remove from toasts on error
    } finally {
      setProcessing(null);
    }
  };

  const handleDecline = async (notification: NotificationDto) => {
    setProcessing(notification.notificationId);

    try {
      // Get invite ID from notification metadata
      let inviteId = notification.metadata.inviteId;

      // Fallback: if no inviteId, fetch pending invites and match by lobbyId
      if (!inviteId && notification.metadata.lobbyId) {
        const pendingInvites = await LobbyInviteService.getPendingInvites();
        const matchingInvite = pendingInvites.find(
          (inv) => inv.lobbyId === notification.metadata.lobbyId
        );
        if (matchingInvite) {
          inviteId = matchingInvite.inviteId;
        }
      }

      if (!inviteId) {
        console.error('Could not find invite ID');
        return;
      }

      await LobbyInviteService.declineInvite(inviteId);

      // Mark notification as read
      await markAsRead(notification.notificationId);

      // Remove from visible toasts
      setVisibleInvites((prev) =>
        prev.filter((n) => n.notificationId !== notification.notificationId)
      );
    } catch (error) {
      console.error('Failed to decline invite:', error);
      // Don't remove from toasts on error
    } finally {
      setProcessing(null);
    }
  };

  const handleDismiss = async (notification: NotificationDto) => {
    // Mark as read and remove
    await markAsRead(notification.notificationId);
    setVisibleInvites((prev) =>
      prev.filter((n) => n.notificationId !== notification.notificationId)
    );
  };

  if (visibleInvites.length === 0) return null;

  return (
    <div
      style={{
        position: 'fixed',
        bottom: '1rem',
        right: '1rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem',
        zIndex: 10000,
      }}
    >
      {visibleInvites.map((notification) => (
        <div
          key={notification.notificationId}
          className="animate-slide-in-right"
          style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
            border: '2px solid #667eea',
            padding: '1rem',
            minWidth: '320px',
            maxWidth: '400px',
          }}
        >
          {/* Header */}
          <div style={{ display: 'flex', alignItems: 'start', marginBottom: '1rem' }}>
            <div
              style={{
                flexShrink: 0,
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.25rem',
                marginRight: '0.75rem',
                backgroundColor: '#667eea',
              }}
            >
              🎮
            </div>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: '0.875rem', fontWeight: 'bold', color: '#111827', margin: 0 }}>
                {notification.title}
              </p>
              <p style={{ marginTop: '0.25rem', fontSize: '0.875rem', color: '#4b5563', margin: 0 }}>
                {notification.message}
              </p>
            </div>
            <button
              onClick={() => handleDismiss(notification)}
              style={{
                marginLeft: '0.5rem',
                color: '#9ca3af',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                padding: '0.25rem',
              }}
            >
              <svg
                style={{ width: '20px', height: '20px' }}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              onClick={() => handleAccept(notification)}
              disabled={processing === notification.notificationId}
              style={{
                flex: 1,
                padding: '0.5rem 0.75rem',
                borderRadius: '8px',
                fontWeight: '600',
                fontSize: '0.875rem',
                transition: 'all 0.2s',
                backgroundColor:
                  processing === notification.notificationId ? '#ccc' : '#28a745',
                color: 'white',
                border: 'none',
                cursor:
                  processing === notification.notificationId ? 'not-allowed' : 'pointer',
              }}
            >
              {processing === notification.notificationId ? 'Accepting...' : '✓ Accept'}
            </button>
            <button
              onClick={() => handleDecline(notification)}
              disabled={processing === notification.notificationId}
              style={{
                flex: 1,
                padding: '0.5rem 0.75rem',
                borderRadius: '8px',
                fontWeight: '600',
                fontSize: '0.875rem',
                transition: 'all 0.2s',
                backgroundColor:
                  processing === notification.notificationId ? '#ccc' : '#dc3545',
                color: 'white',
                border: 'none',
                cursor:
                  processing === notification.notificationId ? 'not-allowed' : 'pointer',
              }}
            >
              {processing === notification.notificationId ? 'Declining...' : '✕ Decline'}
            </button>
          </div>

          {/* Time indicator */}
          {notification.metadata.expiresIn && (
            <div
              style={{
                marginTop: '0.5rem',
                fontSize: '0.75rem',
                textAlign: 'center',
                color: '#6b7280',
              }}
            >
              Expires in {notification.metadata.expiresIn}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}