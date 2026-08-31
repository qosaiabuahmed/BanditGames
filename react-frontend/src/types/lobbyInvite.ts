// Lobby invite status enum
export type LobbyInviteStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED';

// Lobby invite DTO
export interface LobbyInviteDto {
  inviteId: string;           // UUID
  lobbyId: string;            // UUID
  fromUserId: string;         // UUID of inviter
  toUserId: string;           // UUID of invitee
  status: LobbyInviteStatus;
  createdAt: string;          // ISO 8601 timestamp
  expiresAt: string;          // ISO 8601 timestamp
}

// Request body for sending lobby invites
export interface SendLobbyInviteBody {
  toUserId: string;           // UUID of friend to invite
}

// Response for accepting invite
export interface AcceptInviteResponse {
  lobby: {
    lobbyId: string;
    gameId: string;
    status: 'OPEN' | 'MATCHED';
    players: Array<{
      userId: string;
      joinedAt: string;
    }>;
    createdAt: string;
    matchedAt: string | null;
  };
  match?: {
    matchId: string;
    gameId: string;
    players: Array<{
      userId: string;
      joinedAt: string;
    }>;
    startedAt: string;
  };
}

// Extended invite with user info for UI display
export interface LobbyInviteWithUserInfo extends LobbyInviteDto {
  inviterUsername?: string;
  inviterPlayerTag?: string;
  gameName?: string;
}