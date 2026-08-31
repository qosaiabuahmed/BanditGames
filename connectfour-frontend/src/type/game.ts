export type Player = 'X' | 'O';
export type Cell = '.' | 'X' | 'O';
export type PlayerType = 'human' | 'ai_mcts';

export interface GameState {
  game_id: number;
  player_x_id: number;
  player_o_id: number;
  player_x_username?: string;  // Player X username (available in multiplayer)
  player_o_username?: string;  // Player O username (available in multiplayer)
  current_player: Player;
  current_turn: number;
  score_x: number;
  score_o: number;
  game_over: boolean;
  board: Cell[][];
  valid_moves: number[];
  winner?: Player | null;
}

export interface CreateGameRequest {
  player_x_username: string;
  player_o_username: string;
  player_x_type: PlayerType;
  player_o_type: PlayerType;
  rows: number;
  cols: number;
  connect_length: number;
}

export interface MakeMoveRequest {
  column: number;
  player: Player;
}

export interface MoveResponse {
  move_id: number;
  game_id: number;
  column_played: number;
  row_placed: number;
  player: Player;
  score_x: number;
  score_o: number;
  board_after: Cell[][];
  valid_moves: number[];
  game_over: boolean;
  winner: Player | null;
  ai_recommendation?: AIRecommendation;
}

export interface AIRecommendation {
  column: number;
  confidence: number;
  nodes_explored: number;
  computation_time_ms: number;
  recommended_for_player?: Player;
  player?: Player;
}

export interface MoveHistoryItem {
  move_number: number;
  column_played: number;
  row_placed: number;
  player: Player;
  score_x_before: number;
  score_o_before: number;
  score_x_after: number;
  score_o_after: number;
}
