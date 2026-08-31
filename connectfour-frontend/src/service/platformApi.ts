
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
  connectFourGameId: number;
  playerXName: string;
  playerOName: string;
}

interface GameEndedPayload {
  winnerId?: string;
  winnerName?: string;
  outcome: 'WIN' | 'DRAW';
  duration: number;
}

export class PlatformApi {
  /**
   * Get current user information from the platform
   */
  static async getCurrentUser(token: string): Promise<PlatformUser> {
    const response = await fetch(`${PLATFORM_API_URL}/api/users/me`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      const errorText = await response.text().catch(() => 'Unknown error');
      throw new Error(`Failed to get current user from platform (${response.status}): ${errorText}`);
    }

    return await response.json();
  }

  /**
   * Notify the platform that a game has started
   */
  static async notifyGameStarted(
    token: string,
    matchId: string,
    payload: GameStartedPayload
  ): Promise<void> {
    const response = await fetch(`${PLATFORM_API_URL}/api/matches/${matchId}/game-started`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      console.error('Failed to notify platform about game start');
    }
  }

  /**
   * Notify the platform that a game has ended
   */
  static async notifyGameEnded(
    token: string,
    matchId: string,
    payload: GameEndedPayload
  ): Promise<void> {
    const response = await fetch(`${PLATFORM_API_URL}/api/matches/${matchId}/game-ended`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      console.error('Failed to notify platform about game end');
    }
  }

  /**
   * Validate JWT token with the platform
   */
  static async validateToken(token: string): Promise<boolean> {
    try {
      await this.getCurrentUser(token);
      return true;
    } catch (error) {
      return false;
    }
  }
}