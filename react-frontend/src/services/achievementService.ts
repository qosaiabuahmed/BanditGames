import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api';

export interface AchievementDto {
    achievementId: string;
    userId: string;
    gameId: string;
    matchId: string;
    achievementCode: string;
    unlockedAt: string;
}

export class AchievementService {
    /**
     * Get all achievements for the current user
     */
    static async getMyAchievements(): Promise<AchievementDto[]> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/achievements/my`, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            throw new Error('Failed to fetch achievements');
        }

        return response.json();
    }

    /**
     * Get achievements for the current user by game
     */
    static async getMyAchievementsByGame(gameId: string): Promise<AchievementDto[]> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/achievements/my/game/${gameId}`, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            throw new Error('Failed to fetch achievements by game');
        }

        return response.json();
    }

    /**
     * Get achievements for a specific user (public)
     */
    static async getUserAchievements(userId: string): Promise<AchievementDto[]> {
        const headers = await getAuthHeaders();
        const response = await fetch(`${API_BASE_URL}/achievements/user/${userId}`, {
            method: 'GET',
            headers,
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user achievements');
        }

        return response.json();
    }
}
