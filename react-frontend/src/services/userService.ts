import type { User, CreateUserRequest, UpdateUserRequest } from '../types/user';
import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export class UserService {
  static async createUser(userData: CreateUserRequest): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create user');
    }

    return response.json();
  }

  static async getUserById(userId: string): Promise<User> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, { headers });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch user');
    }

    return response.json();
  }

  static async getUserByEmail(email: string): Promise<User> {
    const headers = await getAuthHeaders();
    const response = await fetch(
      `${API_BASE_URL}/users/email/${encodeURIComponent(email)}`,
      { headers }
    );

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch user');
    }

    return response.json();
  }

  static async getCurrentUser(): Promise<User> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/me`, { headers });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch current user');
    }

    return response.json();
  }

  static async updateUser(
    userId: string,
    userData: UpdateUserRequest
  ): Promise<User> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
      method: 'PUT',
      headers,
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update user');
    }

    return response.json();
  }

  static async markUserOnline(): Promise<void> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/me/online`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      throw new Error('Failed to mark user as online');
    }
  }

  static async markUserOffline(): Promise<void> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/me/offline`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      throw new Error('Failed to mark user as offline');
    }
  }

  static async getAllUsers(): Promise<User[]> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      throw new Error('Failed to fetch users');
    }

    return response.json();
  }

  static async getFavoriteGameIds(): Promise<string[]> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/me/favorites/ids`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      throw new Error('Failed to fetch favorites!');
    }

    return response.json();
  }

  static async removeFavoriteGame(gameId: string): Promise<void> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/me/favorites/${gameId}`, {
      method: 'DELETE',
      headers,
    });

    if (!response.ok) {
      throw new Error('Failed to unfavorite game!');
    }
  }

  static async addFavoriteGame(gameId: string): Promise<void> {
    const headers = await getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}/users/me/favorites/${gameId}`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      throw new Error('Failed to mark game!');
    }
  }
}