import { useState, useEffect } from 'react';
import { SocialService } from '../../services/socialService';
import { LobbyInviteService } from '../../services/lobbyInviteService';
import type { FriendDto } from '../../types/social';

interface InviteFriendModalProps {
  lobbyId: string;
  onClose: () => void;
}

export default function InviteFriendModal({ lobbyId, onClose }: InviteFriendModalProps) {
  const [friends, setFriends] = useState<FriendDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [selectedFriendId, setSelectedFriendId] = useState<string | null>(null);

  useEffect(() => {
    fetchFriends();
  }, []);

  const fetchFriends = async () => {
    try {
      const friendsList = await SocialService.getFriends();
      setFriends(friendsList);
      setLoading(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load friends');
      setLoading(false);
    }
  };

  const handleSendInvite = async (friendUserId: string) => {
    setSending(true);
    setError(null);
    setSuccess(null);

    try {
      await LobbyInviteService.sendInvite(lobbyId, friendUserId);
      setSuccess('Invite sent successfully!');
      setSelectedFriendId(friendUserId);

      // Close modal after 1.5 seconds
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send invite');
    } finally {
      setSending(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.7)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 9999,
        padding: '1rem',
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: 'white',
          borderRadius: '16px',
          padding: '2rem',
          maxWidth: '500px',
          width: '100%',
          maxHeight: '80vh',
          overflow: 'auto',
          boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ color: '#333', fontSize: '1.5rem', fontWeight: '700', margin: 0 }}>
            Invite Friend to Lobby
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '1.5rem',
              color: '#999',
              cursor: 'pointer',
              padding: '0.5rem',
              lineHeight: 1,
            }}
          >
            ×
          </button>
        </div>

        {/* Success Message */}
        {success && (
          <div
            style={{
              backgroundColor: '#d4edda',
              color: '#155724',
              padding: '1rem',
              borderRadius: '8px',
              marginBottom: '1rem',
              border: '1px solid #c3e6cb',
            }}
          >
            ✓ {success}
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div
            style={{
              backgroundColor: '#f8d7da',
              color: '#721c24',
              padding: '1rem',
              borderRadius: '8px',
              marginBottom: '1rem',
              border: '1px solid #f5c6cb',
            }}
          >
            ⚠ {error}
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div style={{ textAlign: 'center', padding: '2rem' }}>
            <div
              className="animate-spin"
              style={{
                width: '40px',
                height: '40px',
                border: '4px solid #e9ecef',
                borderTop: '4px solid #667eea',
                borderRadius: '50%',
                margin: '0 auto',
              }}
            />
            <p style={{ marginTop: '1rem', color: '#666' }}>Loading friends...</p>
          </div>
        )}

        {/* Friends List */}
        {!loading && friends.length === 0 && (
          <div style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>👥</div>
            <p>You don't have any friends yet.</p>
            <p style={{ fontSize: '0.9rem', marginTop: '0.5rem' }}>
              Add friends to invite them to your lobby!
            </p>
          </div>
        )}

        {!loading && friends.length > 0 && (
          <div style={{ marginTop: '1rem' }}>
            {friends.map((friend) => (
              <div
                key={friend.userId}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  padding: '1rem',
                  backgroundColor: selectedFriendId === friend.userId ? '#f0f9ff' : '#f8f9fa',
                  borderRadius: '12px',
                  marginBottom: '0.75rem',
                  border: selectedFriendId === friend.userId ? '2px solid #667eea' : '2px solid transparent',
                  transition: 'all 0.2s ease',
                }}
              >
                {/* Avatar */}
                <div
                  style={{
                    width: '50px',
                    height: '50px',
                    borderRadius: '50%',
                    backgroundColor: '#667eea',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: '700',
                    fontSize: '1.2rem',
                    marginRight: '1rem',
                    flexShrink: 0,
                  }}
                >
                  {friend.username.charAt(0).toUpperCase()}
                </div>

                {/* Friend Info */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontWeight: '600', color: '#333', marginBottom: '0.25rem' }}>
                    {friend.username}
                  </div>
                  <div style={{ fontSize: '0.85rem', color: '#666' }}>
                    {friend.playerTag}
                  </div>
                </div>

                {/* Invite Button */}
                <button
                  onClick={() => handleSendInvite(friend.userId)}
                  disabled={sending || selectedFriendId === friend.userId}
                  style={{
                    padding: '0.5rem 1.25rem',
                    backgroundColor: selectedFriendId === friend.userId ? '#28a745' : '#667eea',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: sending || selectedFriendId === friend.userId ? 'not-allowed' : 'pointer',
                    fontWeight: '600',
                    fontSize: '0.9rem',
                    transition: 'all 0.2s ease',
                    opacity: sending || selectedFriendId === friend.userId ? 0.7 : 1,
                    flexShrink: 0,
                  }}
                  onMouseOver={(e) => {
                    if (!sending && selectedFriendId !== friend.userId) {
                      e.currentTarget.style.backgroundColor = '#5568d3';
                      e.currentTarget.style.transform = 'translateY(-2px)';
                    }
                  }}
                  onMouseOut={(e) => {
                    if (!sending && selectedFriendId !== friend.userId) {
                      e.currentTarget.style.backgroundColor = '#667eea';
                      e.currentTarget.style.transform = 'translateY(0)';
                    }
                  }}
                >
                  {selectedFriendId === friend.userId ? '✓ Invited' : sending ? 'Sending...' : 'Invite'}
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Close Button */}
        <button
          onClick={onClose}
          style={{
            width: '100%',
            padding: '0.75rem',
            marginTop: '1.5rem',
            backgroundColor: '#6c757d',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: '600',
            transition: 'all 0.2s ease',
          }}
          onMouseOver={(e) => {
            e.currentTarget.style.backgroundColor = '#5a6268';
          }}
          onMouseOut={(e) => {
            e.currentTarget.style.backgroundColor = '#6c757d';
          }}
        >
          Close
        </button>
      </div>
    </div>
  );
}