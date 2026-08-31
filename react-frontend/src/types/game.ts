export type RenderType = 'NATIVE_TICTACTOE' | 'NATIVE_CONNECT4' | 'EXTERNAL_IFRAME' | 'STATE_BASED';

export type GameStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'DEPRECATED';

export type PlayerType = 'HUMAN' | 'AI';

export interface PlayerConfiguration {
  minPlayers: number;
  maxPlayers: number;
  supportedPlayerTypes: PlayerType[];
}

export interface GameRules {
  rulesText: string;
  rulesUrl: string;
  summary: string;
}

export interface AchievementDefinition {
  achievementId: string;
  name: string;
  description: string;
  iconUrl: string;
  condition: string;
}

export interface GameMetaData {
  category: string;
  theme: string;
  designer: string;
  publisher: string;
  releaseYear: number;
  estimatedDurationMinutes: number;
  complexity: string;
  frontendUrl?: string;
  pictureUrl?: string;
}

export interface Game {
  id: string;
  name: string;
  description: string;
  rules: GameRules;
  achievements: AchievementDefinition[];
  registeredAt: string;
  metaData: GameMetaData;
  status: GameStatus;
  playerConfiguration: PlayerConfiguration;
  renderType: RenderType;
}

export interface CreateGameRequest {
  name: string;
  description: string;
  rules: GameRules;
  achievements: AchievementDefinition[];
  playerConfiguration: PlayerConfiguration;
  renderType: RenderType;
  metaData: GameMetaData;
}
