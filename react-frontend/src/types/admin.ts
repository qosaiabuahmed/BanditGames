// User Statistics
export interface UserStatistics {
  totalUsers: number;
  onlineUsers: number;
  offlineUsers: number;
  registrationTrend: UserRegistrationTrend[];
}

export interface UserRegistrationTrend {
  date: string;
  count: number;
}

// Match Statistics
export interface MatchStatistics {
  totalMatches: number;
  matchesByStatus: Record<MatchStatus, number>;
  averageMatchDurationMinutes: number | null;
}

export type MatchStatus = 'PENDING_ASSIGNMENT' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';

// Game Statistics
export interface GameStatistics {
  totalGames: number;
  gamesByStatus: Record<GameStatus, number>;
  recentGames: GameSummary[];
}

export type GameStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'DEPRECATED';

export interface GameSummary {
  gameId: string;
  name: string;
  status: GameStatus;
  registeredAt: string;
}

// Most Played Games
export interface GamePlayCount {
  gameId: string;
  totalMatches: number;
  finishedMatches: number;
}

// ML Prediction Statistics
export interface MLPredictionStats {
  statsByModel: ModelStats[];
  firstPrediction: string | null;
  lastPrediction: string | null;
}

export interface ModelStats {
  predictionType: 'policy' | 'value';
  modelType: 'cnn' | 'lr';
  totalPredictions: number;
  avgResponseTimeMs: number;
}