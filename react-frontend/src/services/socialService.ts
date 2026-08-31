import type { FriendDto, SendFriendRequestBody, MessageResponse } from '../types/social';
import { getAuthHeaders } from '../utils/tokenUtils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export class SocialService {
  /**
   * Send a friend request to another user
   * @param toUserId - UUID of the user to send the request to
   */
  static async sendFriendRequest(toUserId: string): Promise<MessageResponse> {
    const body: SendFriendRequestBody = { toUserId };
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/social/friendships/requests`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to send friend request');
    }

    return response.json();
  }

  /**
   * Accept a friend request
   * @param friendshipId - UUID of the friendship
   */
  static async acceptFriendRequest(friendshipId: string): Promise<MessageResponse> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/social/friendships/${friendshipId}/accept`, {
      method: 'PUT',
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to accept friend request');
    }

    return response.json();
  }

  /**
   * Decline a friend request
   * @param friendshipId - UUID of the friendship
   */
  static async declineFriendRequest(friendshipId: string): Promise<MessageResponse> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/social/friendships/${friendshipId}/decline`, {
      method: 'PUT',
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to decline friend request');
    }

    return response.json();
  }

  /**
   * Get the list of accepted friends
   */
  static async getFriends(): Promise<FriendDto[]> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/social/friendships/friends`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch friends');
    }

    return response.json();
  }

  /**
   * Get pending friend requests (requests received from others)
   */
  static async getPendingRequests(): Promise<FriendDto[]> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/social/friendships/requests/pending`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch pending requests');
    }

    return response.json();
  }

  /**
   * Get sent friend requests (requests sent to others)
   */
  static async getSentRequests(): Promise<FriendDto[]> {
    const headers = await getAuthHeaders();

    const response = await fetch(`${API_BASE_URL}/social/friendships/requests/sent`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch sent requests');
    }

    return response.json();
  }
}
