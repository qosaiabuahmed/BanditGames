// Friendship status enum
export type FriendshipStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'BLOCKED';

// Friend DTO - represents a friend or friend request
export interface FriendDto {
  friendshipId: string;        // UUID
  userId: string;              // UUID of the friend
  username: string;            // Friend's username
  playerTag: string;           // Friend's player tag (e.g., "#1234")
  status: FriendshipStatus;    // Current friendship status
  createdAt: string;           // ISO 8601 timestamp
  acceptedAt: string | null;   // ISO 8601 timestamp or null if pending
}

// Request body for sending friend requests
export interface SendFriendRequestBody {
  toUserId: string;            // UUID of user to send request to
}

// Generic message response from API
export interface MessageResponse {
  message: string;             // Success or error message
}