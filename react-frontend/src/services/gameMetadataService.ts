import type { CreateGameRequest, Game } from '../types/game';
import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export interface GameFilters {
    name?: string;
    registeredAfter?: string;
    registeredBefore?: string;
    status?: string;
    category?: string;
    theme?: string;
    designer?: string;
    publisher?: string;
    releaseYear?: number;
    minDuration?: number;
    maxDuration?: number;
    complexity?: string;
}

export class GameMetadataService {
  private static buildQueryString(filters?: GameFilters, userId?: string): string {
      const params = new URLSearchParams();

      if (userId) {
          params.append('userId', userId);
      }

      if (filters) {
          Object.entries(filters).forEach(([key, value]) => {
              if (value !== undefined && value !== null && value !== '') {
                  params.append(key, value.toString());
              }
          });
      }

      const queryString = params.toString();
      return queryString ? `?${queryString}` : '';
  }

  static async listGames(
      filters?: GameFilters,
      userId?: string
  ): Promise<Game[]> {
      const queryString = this.buildQueryString(filters, userId);
      // Don't require auth for listing games (public endpoint)
      const headers = await getAuthHeaders(false);

    const response = await fetch(`${API_BASE_URL}/games${queryString}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Failed to load games');
    }

    return response.json();
  }

  static async getGame(id: string): Promise<Game> {
    // Don't require auth for getting game details (public endpoint)
    const headers = await getAuthHeaders(false);
    const response = await fetch(`${API_BASE_URL}/games/${id}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Failed to load game');
    }

    return response.json();
  }

  static async registerGame(payload: CreateGameRequest): Promise<Game> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/games`, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Failed to register game');
    }

    return response.json();
  }
}
