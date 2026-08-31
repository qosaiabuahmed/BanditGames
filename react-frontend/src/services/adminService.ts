import type {
  UserStatistics,
  MatchStatistics,
  GameStatistics,
  GamePlayCount,
  MLPredictionStats
} from '../types/admin';
import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export class AdminService {
  // User Analytics
  static async getUserStatistics(): Promise<UserStatistics> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/admin/users/statistics`, { headers });

    if (!response.ok) {
      throw new Error('Failed to fetch user statistics');
    }

    return response.json();
  }

  // Match Analytics
  static async getMatchStatistics(): Promise<MatchStatistics> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/admin/matches/statistics`, { headers });

    if (!response.ok) {
      throw new Error('Failed to fetch match statistics');
    }

    return response.json();
  }

  static async getMostPlayedGames(limit: number = 10): Promise<GamePlayCount[]> {
    const headers = await getAuthHeaders();
    const response = await fetch(
      `${API_BASE_URL}/admin/matches/most-played-games?limit=${limit}`,
      { headers }
    );

    if (!response.ok) {
      throw new Error('Failed to fetch most played games');
    }

    return response.json();
  }

  // Game Analytics
  static async getGameStatistics(): Promise<GameStatistics> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/admin/games/statistics`, { headers });

    if (!response.ok) {
      throw new Error('Failed to fetch game statistics');
    }

    return response.json();
  }

  // ML Analytics
  static async getMLPredictionStats(): Promise<MLPredictionStats> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/admin/ml/predictions/stats`, { headers });

    if (!response.ok) {
      throw new Error('Failed to fetch ML prediction statistics');
    }

    return response.json();
  }
}