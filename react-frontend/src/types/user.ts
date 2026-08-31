export type UserStatus = 'ONLINE' | 'OFFLINE' | 'IN_GAME' | 'AWAY';

export interface User {
  userId: string;
  username: string;
  email: string;
  playerTag: string;
  avatar: string | null;
  status: UserStatus;
  registeredAt: string;
  lastLoginAt: string | null;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  playerTag: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  playerTag?: string;
  avatar?: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}