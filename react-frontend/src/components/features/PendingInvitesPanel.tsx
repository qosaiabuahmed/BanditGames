import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LobbyInviteService } from '../../services/lobbyInviteService';
import { UserService } from '../../services/userService';
import { GameMetadataService } from '../../services/gameMetadataService';
import { MatchmakingService } from '../../services/matchmakingService';
import type { LobbyInviteWithUserInfo } from '../../types/lobbyInvite';

export default function PendingInvitesPanel() {
  const navigate = useNavigate();
  const [invites, setInvites] = useState<LobbyInviteWithUserInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingInvite, setProcessingInvite] = useState<string | null>(null);

  useEffect(() => {
    fetchPendingInvites();

    // Refresh every 30 seconds
    const interval = setInterval(fetchPendingInvites, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchPendingInvites = async () => {
    try {
      const pendingInvites = await LobbyInviteService.getPendingInvites();

      // Enrich invites with user and game information
      const enrichedInvites = await Promise.all(
        pendingInvites.map(async (invite) => {
          try {
            // Fetch inviter info
            const inviter = await UserService.getUserById(invite.fromUserId);

            // Fetch lobby to get game ID
            const lobby = await MatchmakingService.getLobbyStatus(invite.lobbyId);

            // Fetch game info
            const game = await GameMetadataService.getGame(lobby.gameId);

            return {
              ...invite,
              inviterUsername: inviter.username,
              inviterPlayerTag: inviter.playerTag,
              gameName: game.name,
            };
          } catch (err) {
            console.error('Failed to enrich invite:', err);
            return {
              ...invite,
              inviterUsername: 'Unknown',
              gameName: 'Unknown Game',
            };
          }
        })
      );

      // Filter out expired invites
      const activeInvites = enrichedInvites.filter(
        (invite) => new Date(invite.expiresAt) > new Date()
      );

      setInvites(activeInvites);
      setLoading(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load invites');
      setLoading(false);
    }
  };

  const handleAcceptInvite = async (inviteId: string) => {
    setProcessingInvite(inviteId);
    setError(null);

    try {
      const response = await LobbyInviteService.acceptInvite(inviteId);

      // Remove invite from list
      setInvites((prev) => prev.filter((inv) => inv.inviteId !== inviteId));

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
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to accept invite');
      setProcessingInvite(null);
    }
  };

  const handleDeclineInvite = async (inviteId: string) => {
    setProcessingInvite(inviteId);
    setError(null);

    try {
      await LobbyInviteService.declineInvite(inviteId);

      // Remove invite from list
      setInvites((prev) => prev.filter((inv) => inv.inviteId !== inviteId));
      setProcessingInvite(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to decline invite');
      setProcessingInvite(null);
    }
  };

  const getTimeRemaining = (expiresAt: string): string => {
    const now = new Date();
    const expiry = new Date(expiresAt);
    const diff = expiry.getTime() - now.getTime();

    if (diff <= 0) return 'Expired';

    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);

    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
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
        <p style={{ marginTop: '1rem', color: '#666' }}>Loading invites...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <h1 style={{ color: '#333', fontSize: '2rem', fontWeight: '700', marginBottom: '1.5rem' }}>
        Pending Lobby Invites
      </h1>

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

      {invites.length === 0 && (
        <div
          style={{
            textAlign: 'center',
            padding: '3rem',
            backgroundColor: '#f8f9fa',
            borderRadius: '12px',
          }}
        >
          <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>📭</div>
          <h3 style={{ color: '#666', marginBottom: '0.5rem' }}>No pending invites</h3>
          <p style={{ color: '#999', fontSize: '0.9rem' }}>
            When friends invite you to games, they'll appear here.
          </p>
        </div>
      )}

      {invites.map((invite) => (
        <div
          key={invite.inviteId}
          style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '1.5rem',
            marginBottom: '1rem',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
            border: '2px solid #e9ecef',
            transition: 'all 0.2s ease',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
            {/* Game Icon */}
            <div
              style={{
                width: '60px',
                height: '60px',
                borderRadius: '12px',
                backgroundColor: '#667eea',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '2rem',
                marginRight: '1rem',
                flexShrink: 0,
              }}
            >
              🎮
            </div>

            {/* Invite Info */}
            <div style={{ flex: 1, minWidth: 0 }}>
              <h3 style={{ color: '#333', fontSize: '1.2rem', fontWeight: '600', margin: 0, marginBottom: '0.25rem' }}>
                {invite.gameName || 'Unknown Game'}
              </h3>
              <p style={{ color: '#666', margin: 0, fontSize: '0.9rem' }}>
                Invited by <strong>{invite.inviterUsername || 'Unknown'}</strong>
                {invite.inviterPlayerTag && (
                  <span style={{ color: '#999' }}> {invite.inviterPlayerTag}</span>
                )}
              </p>
            </div>

            {/* Timer */}
            <div
              style={{
                textAlign: 'center',
                padding: '0.5rem 1rem',
                backgroundColor: '#fff3cd',
                borderRadius: '8px',
                border: '1px solid #ffc107',
                flexShrink: 0,
              }}
            >
              <div style={{ fontSize: '0.75rem', color: '#856404', fontWeight: '600' }}>
                EXPIRES IN
              </div>
              <div style={{ fontSize: '1.1rem', fontWeight: '700', color: '#856404' }}>
                {getTimeRemaining(invite.expiresAt)}
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button
              onClick={() => handleAcceptInvite(invite.inviteId)}
              disabled={processingInvite === invite.inviteId}
              style={{
                flex: 1,
                padding: '0.75rem',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: processingInvite === invite.inviteId ? 'not-allowed' : 'pointer',
                fontWeight: '600',
                fontSize: '1rem',
                transition: 'all 0.2s ease',
                opacity: processingInvite === invite.inviteId ? 0.7 : 1,
              }}
              onMouseOver={(e) => {
                if (processingInvite !== invite.inviteId) {
                  e.currentTarget.style.backgroundColor = '#218838';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                }
              }}
              onMouseOut={(e) => {
                if (processingInvite !== invite.inviteId) {
                  e.currentTarget.style.backgroundColor = '#28a745';
                  e.currentTarget.style.transform = 'translateY(0)';
                }
              }}
            >
              {processingInvite === invite.inviteId ? 'Accepting...' : '✓ Accept'}
            </button>

            <button
              onClick={() => handleDeclineInvite(invite.inviteId)}
              disabled={processingInvite === invite.inviteId}
              style={{
                flex: 1,
                padding: '0.75rem',
                backgroundColor: '#dc3545',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: processingInvite === invite.inviteId ? 'not-allowed' : 'pointer',
                fontWeight: '600',
                fontSize: '1rem',
                transition: 'all 0.2s ease',
                opacity: processingInvite === invite.inviteId ? 0.7 : 1,
              }}
              onMouseOver={(e) => {
                if (processingInvite !== invite.inviteId) {
                  e.currentTarget.style.backgroundColor = '#c82333';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                }
              }}
              onMouseOut={(e) => {
                if (processingInvite !== invite.inviteId) {
                  e.currentTarget.style.backgroundColor = '#dc3545';
                  e.currentTarget.style.transform = 'translateY(0)';
                }
              }}
            >
              {processingInvite === invite.inviteId ? 'Declining...' : '✕ Decline'}
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}