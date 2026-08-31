import type {
  LobbyInviteDto,
  SendLobbyInviteBody,
  AcceptInviteResponse
} from '../types/lobbyInvite';
import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export class LobbyInviteService {
  /**
   * Send a lobby invite to a friend
   * @param lobbyId - UUID of the lobby
   * @param toUserId - UUID of the friend to invite
   */
  static async sendInvite(
    lobbyId: string,
    toUserId: string
  ): Promise<void> {
    const body: SendLobbyInviteBody = { toUserId };
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/${lobbyId}/invites`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to send invite' }));
      throw new Error(error.message || 'Failed to send invite');
    }
  }

  /**
   * Get all pending lobby invites for the current user
   */
  static async getPendingInvites(): Promise<LobbyInviteDto[]> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/invites/pending`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to fetch invites' }));
      throw new Error(error.message || 'Failed to fetch pending invites');
    }

    return response.json();
  }

  /**
   * Accept a lobby invite
   * @param inviteId - UUID of the invite
   */
  static async acceptInvite(inviteId: string): Promise<AcceptInviteResponse> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/invites/${inviteId}/accept`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to accept invite' }));
      throw new Error(error.message || 'Failed to accept invite');
    }

    return response.json();
  }

  /**
   * Decline a lobby invite
   * @param inviteId - UUID of the invite
   */
  static async declineInvite(inviteId: string): Promise<void> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/invites/${inviteId}/decline`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to decline invite' }));
      throw new Error(error.message || 'Failed to decline invite');
    }
  }
}