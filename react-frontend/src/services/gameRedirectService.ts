import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

interface GameRedirectResponse {
    redirectUrl: string;
    matchId: string;
    gameType: string;
}

export class GameRedirectService {
    /**
     * Start any game by its ID - works for ALL games!
     * @param gameId - The UUID of the game to start
     * @param matchId - Optional matchId for multiplayer mode
     */
    static async startGame(gameId: string, matchId?: string): Promise<void> {
        const headers = await getAuthHeaders();
        const body = matchId ? JSON.stringify({ matchId }) : undefined;

        const response = await fetch(`${API_BASE_URL}/games/${gameId}/start`, {
            method: 'POST',
            headers,
            body,
        });

        if (!response.ok) {
            throw new Error('Failed to start game');
        }

        const data: GameRedirectResponse = await response.json();
        window.location.href = data.redirectUrl;
    }

    /**
     * Start game with player information for PvP matches
     */
    static async startGameWithPlayers(
        gameId: string,
        matchId: string,
        player1Id: string,
        player1Name: string,
        player2Id: string,
        player2Name: string
    ): Promise<void> {
        const headers = await getAuthHeaders();

        const response = await fetch(`${API_BASE_URL}/games/${gameId}/start`, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                matchId,
                player1Id,
                player1Name,
                player2Id,
                player2Name
            }),
        });

        if (!response.ok) {
            throw new Error('Failed to start game');
        }

        const data: GameRedirectResponse = await response.json();
        window.location.href = data.redirectUrl;
    }

    /**
     * Get game URL with player information for PvP matches (without redirecting)
     */
    static async getGameUrlWithPlayers(
        gameId: string,
        matchId: string,
        player1Id: string,
        player1Name: string,
        player2Id: string,
        player2Name: string
    ): Promise<string> {
        const headers = await getAuthHeaders();

        const response = await fetch(`${API_BASE_URL}/games/${gameId}/start`, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                matchId,
                player1Id,
                player1Name,
                player2Id,
                player2Name
            }),
        });

        if (!response.ok) {
            throw new Error('Failed to start game');
        }

        const data: GameRedirectResponse = await response.json();
        return data.redirectUrl;
    }
}