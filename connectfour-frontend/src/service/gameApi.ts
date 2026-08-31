import type {
  GameState,
  CreateGameRequest,
  MakeMoveRequest,
  MoveResponse,
  AIRecommendation,
  MoveHistoryItem,
  Player
} from '../type/game';

const API_BASE_URL = import.meta.env.VITE_GAME_API_URL || 'http://localhost:8000';

export class GameApi {
  static async createGame(
    playerXName: string,
    playerOName: string,
    playerXType: 'human' | 'ai_mcts' = 'human',
    playerOType: 'human' | 'ai_mcts' = 'human'
  ): Promise<GameState> {
    // @ts-ignore
      const request: CreateGameRequest = {
      player_x_username: playerXName,
      player_o_username: playerOName,
      player_x_type: playerXType,
      player_o_type: playerOType,
      rows: 6,
      cols: 7,
      connect_length: 4
    };

    console.log('[API] Creating game with request:', JSON.stringify(request, null, 2));

    const response = await fetch(`${API_BASE_URL}/api/games`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to create game');
    }

    const result = await response.json();
    console.log('[API] Game created, response:', result);
    return result;
  }

  static async createGameWithMatchId(
    playerXName: string,
    playerOName: string,
    matchId: string
  ): Promise<GameState> {
    const request = {
      player_x_username: playerXName,
      player_o_username: playerOName,
      player_x_type: 'human',
      player_o_type: 'human',
      rows: 6,
      cols: 7,
      connect_length: 4,
      match_id: matchId
    };

    console.log('[API] Creating/joining multiplayer game with matchId:', matchId);

    const response = await fetch(`${API_BASE_URL}/api/games`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to create/join game');
    }

    const result = await response.json();
    console.log('[API] Multiplayer game response:', result);
    return result;
  }

  static async makeMove(
    gameId: number,
    column: number,
    player: Player
  ): Promise<MoveResponse> {
    const request: MakeMoveRequest = { column, player };

    console.log('[API] Making move:', JSON.stringify(request, null, 2));

    const response = await fetch(`${API_BASE_URL}/api/games/${gameId}/moves`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to make move');
    }

    const result = await response.json();
    console.log('[API] Move response:', result);
    console.log('[API] Board after move has', result.board_after.flat().filter((c: string) => c !== '.').length, 'pieces');
    return result;
  }

  static async getGameState(gameId: number): Promise<GameState> {
    const response = await fetch(`${API_BASE_URL}/api/games/${gameId}`);

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to get game state');
    }

    return await response.json();
  }

  static async getAIMove(gameId: number): Promise<AIRecommendation> {
    const response = await fetch(`${API_BASE_URL}/api/games/${gameId}/ai-move`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to get AI move');
    }

    return await response.json();
  }

  static async getMoveHistory(gameId: number): Promise<MoveHistoryItem[]> {
    const response = await fetch(`${API_BASE_URL}/api/games/${gameId}/moves`);

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to get move history');
    }

    return await response.json();
  }

  static async deleteGame(gameId: number): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE_URL}/api/games/${gameId}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Failed to delete game');
    }

    return await response.json();
  }
}
