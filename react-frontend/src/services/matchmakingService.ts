import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export interface LobbyPlayer {
  userId: string;
  joinedAt: string;
}

export interface Lobby {
  lobbyId: string;
  gameId: string;
  status: 'OPEN' | 'MATCHED' | 'CANCELLED';
  players: LobbyPlayer[];
  createdAt: string;
  matchedAt: string | null;
  matchId: string | null;
}

export interface MatchPlayer {
  userId: string;
  seatNumber: number;
}

export interface Match {
  matchId: string;
  gameId: string;
  status: string;
  players: MatchPlayer[];
  createdAt: string;
}

export interface JoinLobbyResponse {
  lobby: Lobby;
  match: Match | null;
}

export class MatchmakingService {
  /**
   * Join a lobby for matchmaking
   */
  static async joinLobby(gameId: string, userId: string): Promise<JoinLobbyResponse> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/join`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ gameId, userId }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to join lobby' }));
      throw new Error(error.message || 'Failed to join lobby');
    }

    return response.json();
  }

  /**
   * Get current lobby status (for polling)
   */
  static async getLobbyStatus(lobbyId: string): Promise<Lobby> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/${lobbyId}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to get lobby status' }));
      throw new Error(error.message || 'Failed to get lobby status');
    }

    return response.json();
  }

  /**
   * Leave a lobby
   */
  static async leaveLobby(lobbyId: string, userId: string): Promise<void> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/lobbies/leave`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ lobbyId, userId }),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to leave lobby' }));
      throw new Error(error.message || 'Failed to leave lobby');
    }
  }

  /**
   * Get match by ID
   */
  static async getMatchById(matchId: string): Promise<Match> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/matches/${matchId}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Failed to get match' }));
      throw new Error(error.message || 'Failed to get match');
    }

    return response.json();
  }
}