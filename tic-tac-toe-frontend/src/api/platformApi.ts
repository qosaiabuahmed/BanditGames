import axios from 'axios';

const PLATFORM_API_URL = import.meta.env.VITE_PLATFORM_API_URL || 'http://localhost:8083';

interface PlatformUser {
  userId: string;
  username: string;
  email: string;
  playerTag: string;
  avatar?: string;
  status: string;
}

interface GameStartedPayload {
  ticTacToeGameId: string;
  playerXName: string;
  playerOName: string;
}

export class PlatformApi {
  static async getCurrentUser(token: string): Promise<PlatformUser> {
    const response = await axios.get(`${PLATFORM_API_URL}/api/users/me`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    return response.data;
  }

  static async notifyGameStarted(
    token: string,
    matchId: string,
    payload: GameStartedPayload
  ): Promise<void> {
    try {
      await axios.post(
        `${PLATFORM_API_URL}/api/matches/${matchId}/game-started`,
        payload,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
    } catch (error) {
      console.error('Failed to notify platform about game start:', error);
    }
  }

  static async validateToken(token: string): Promise<boolean> {
    try {
      await this.getCurrentUser(token);
      return true;
    } catch (error) {
      return false;
    }
  }
}

export type { PlatformUser, GameStartedPayload };