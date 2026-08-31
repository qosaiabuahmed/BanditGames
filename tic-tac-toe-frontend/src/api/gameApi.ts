import axios, { AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_GAME_API_URL || 'http://localhost:8189/api';

interface GameResponse {
  game_id: string;
  board: string[][];
  current_player: string;
  size: number;
  winner?: string | null;
  is_draw?: boolean;
  player_x_username?: string;
  player_o_username?: string;
}

interface MoveResponse {
  success: boolean;
  board: string[][];
  current_player: string;
  winner?: string | null;
  is_draw?: boolean;
  message: string;
}

interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

interface ErrorResponse {
  detail?: string;
}

interface NewGameRequest {
  size?: number;
  player_x_username?: string;
  player_o_username?: string;
  game_mode?: 'pvp' | 'vs_ai' | 'pve';
  ai_difficulty?: 1 | 2 | 3;
  ai_player?: 'x' | 'o';
  match_id?: string;
}

interface PlayerAnalytics {
  player: {
    player_id: number;
    username: string;
    total_games: number;
    wins: number;
    losses: number;
    draws: number;
    win_rate: number;
  };
  recent_games: Array<{
    game_id: number;
    player_x_username: string;
    player_o_username: string;
    winner: string;
    played_as: string;
    result: string;
  }>;
}

interface GameMove {
  move_id: number;
  player_username: string;
  move_number: number;
  row: number;
  col: number;
  board_state_before: string[][];
  board_state_after: string[][];
  legal_moves: Array<{ row: number; col: number }>;
  move_timestamp: string;
}

interface LeaderboardEntry {
  username: string;
  total_games: number;
  wins: number;
  losses: number;
  draws: number;
  win_rate: number;
  rank: number;
}

class GameApi {
  async createGame(request: NewGameRequest = {}): Promise<ApiResponse<GameResponse>> {
    try {
      const response = await axios.post<GameResponse>(`${API_BASE_URL}/game/new`, {
        size: request.size || 3,
        player_x_username: request.player_x_username || 'Guest_X',
        player_o_username: request.player_o_username || 'Guest_O',
        game_mode: request.game_mode || 'pvp',
        ai_difficulty: request.ai_difficulty || 2,
        ai_player: request.ai_player || 'o',
        match_id: request.match_id
      });
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async createGameWithMatchId(playerXName: string, matchId: string): Promise<GameResponse> {
    const response = await axios.post<GameResponse>(`${API_BASE_URL}/game/new`, {
      player_x_username: playerXName,
      player_o_username: 'Opponent',
      game_mode: 'pvp',
      match_id: matchId
    });
    return response.data;
  }

  async getGameState(gameId: string): Promise<ApiResponse<GameResponse>> {
    try {
      const response = await axios.get<GameResponse>(`${API_BASE_URL}/game/${gameId}`);
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async makeMove(gameId: string, row: number, col: number): Promise<ApiResponse<MoveResponse>> {
    try {
      const response = await axios.post<MoveResponse>(
        `${API_BASE_URL}/game/${gameId}/move`,
        { row, col }
      );
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async deleteGame(gameId: string): Promise<ApiResponse<{ message: string }>> {
    try {
      const response = await axios.delete<{ message: string }>(`${API_BASE_URL}/game/${gameId}`);
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async listGames(): Promise<ApiResponse<string[]>> {
    try {
      const response = await axios.get<string[]>(`${API_BASE_URL}/games`);
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async makeAIMove(gameId: string): Promise<ApiResponse<MoveResponse>> {
    try {
      const response = await axios.post<MoveResponse>(`${API_BASE_URL}/game/${gameId}/ai-move`);
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async getPlayerAnalytics(username: string): Promise<ApiResponse<PlayerAnalytics>> {
    try {
      const response = await axios.get<PlayerAnalytics>(`${API_BASE_URL}/analytics/player/${username}`);
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async getGameMoves(gameId: string): Promise<ApiResponse<{ moves: GameMove[] }>> {
    try {
      const response = await axios.get<{ moves: GameMove[] }>(`${API_BASE_URL}/analytics/game/${gameId}/moves`);
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }

  async getLeaderboard(limit: number = 10): Promise<ApiResponse<{ leaderboard: LeaderboardEntry[] }>> {
    try {
      const response = await axios.get<{ leaderboard: LeaderboardEntry[] }>(
        `${API_BASE_URL}/analytics/leaderboard?limit=${limit}`
      );
      return { success: true, data: response.data };
    } catch (error) {
      const axiosError = error as AxiosError<ErrorResponse>;
      return {
        success: false,
        error: axiosError.response?.data?.detail || axiosError.message
      };
    }
  }
}

export default new GameApi();
export type { GameResponse, MoveResponse, NewGameRequest, PlayerAnalytics, GameMove, LeaderboardEntry };
