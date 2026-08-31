import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import { SocialService } from '../../services/socialService';
import type { User } from '../../types/user';
import { getStatusColor, getStatusBadgeColor } from '../../utils/userStatusUtils';
import {
  Gamepad2,
  ArrowLeft,
  UserPlus,
  Check,
  X,
  Users,
  Circle,
  Clock,
  Hash,
  AlertCircle,
  CheckCircle2
} from 'lucide-react';

type FriendshipState = 'none' | 'friends' | 'pending_sent' | 'pending_received';

interface UserWithFriendship extends User {
  friendshipState: FriendshipState;
  friendshipId?: string;
}

export default function FindFriendsPage() {
  const navigate = useNavigate();
  const { getEmail } = useAuth();
  const [users, setUsers] = useState<UserWithFriendship[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const currentUserEmail = getEmail();

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    if (!currentUserEmail) {
      navigate('/login');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const [allUsers, friends, pendingRequests, sentRequests] = await Promise.all([
        UserService.getAllUsers(),
        SocialService.getFriends(),
        SocialService.getPendingRequests(),
        SocialService.getSentRequests(),
      ]);

      const friendsMap = new Map(friends.map(f => [f.userId, f]));
      const pendingMap = new Map(pendingRequests.map(f => [f.userId, f]));
      const sentMap = new Map(sentRequests.map(f => [f.userId, f]));

      const usersWithFriendship: UserWithFriendship[] = allUsers.map(user => {
        let friendshipState: FriendshipState = 'none';
        let friendshipId: string | undefined;

        if (friendsMap.has(user.userId)) {
          friendshipState = 'friends';
          friendshipId = friendsMap.get(user.userId)?.friendshipId;
        } else if (sentMap.has(user.userId)) {
          friendshipState = 'pending_sent';
          friendshipId = sentMap.get(user.userId)?.friendshipId;
        } else if (pendingMap.has(user.userId)) {
          friendshipState = 'pending_received';
          friendshipId = pendingMap.get(user.userId)?.friendshipId;
        }

        return {
          ...user,
          friendshipState,
          friendshipId,
        };
      });

      setUsers(usersWithFriendship);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleSendFriendRequest = async (userId: string, username: string) => {
    try {
      setActionLoading(userId);
      setError(null);
      setSuccess(null);

      await SocialService.sendFriendRequest(userId);
      setSuccess(`Friend request sent to ${username}!`);
      await fetchAllData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send friend request');
    } finally {
      setActionLoading(null);
    }
  };

  const handleAcceptRequest = async (friendshipId: string, username: string) => {
    try {
      setActionLoading(friendshipId);
      setError(null);
      setSuccess(null);

      await SocialService.acceptFriendRequest(friendshipId);
      setSuccess(`You are now friends with ${username}!`);
      await fetchAllData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to accept request');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeclineRequest = async (friendshipId: string, username: string) => {
    try {
      setActionLoading(friendshipId);
      setError(null);
      setSuccess(null);

      await SocialService.declineFriendRequest(friendshipId);
      setSuccess(`Declined friend request from ${username}`);
      await fetchAllData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to decline request');
    } finally {
      setActionLoading(null);
    }
  };

  const renderActionButton = (user: UserWithFriendship) => {
    if (user.email === currentUserEmail) {
      return null;
    }

    const isLoading = actionLoading === user.userId || actionLoading === user.friendshipId;

    switch (user.friendshipState) {
      case 'friends':
        return (
          <button
            disabled
            className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-green-600/50 text-green-300 rounded-lg cursor-not-allowed"
          >
            <Check className="w-4 h-4" />
            Friends
          </button>
        );

      case 'pending_sent':
        return (
          <button
            disabled
            className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-slate-700 text-slate-400 rounded-lg cursor-not-allowed"
          >
            <Clock className="w-4 h-4" />
            Request Sent
          </button>
        );

      case 'pending_received':
        return (
          <div className="flex gap-2">
            <button
              onClick={() => handleAcceptRequest(user.friendshipId!, user.username)}
              disabled={isLoading}
              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-slate-700 text-white rounded-lg transition-colors disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  <Check className="w-4 h-4" />
                  Accept
                </>
              )}
            </button>
            <button
              onClick={() => handleDeclineRequest(user.friendshipId!, user.username)}
              disabled={isLoading}
              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 disabled:bg-slate-700 text-white rounded-lg transition-colors disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  <X className="w-4 h-4" />
                  Decline
                </>
              )}
            </button>
          </div>
        );

      case 'none':
      default:
        return (
          <button
            onClick={() => handleSendFriendRequest(user.userId, user.username)}
            disabled={isLoading}
            className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-slate-700 text-white rounded-lg transition-colors disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                Sending...
              </>
            ) : (
              <>
                <UserPlus className="w-4 h-4" />
                Add Friend
              </>
            )}
          </button>
        );
    }
  };

  return (
    <div className="min-h-screen bg-slate-950">
      {/* Header */}
      <div className="border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-2">
              <Gamepad2 className="w-8 h-8 text-purple-400" />
              <span className="text-xl font-semibold text-slate-100">BanditGames</span>
            </div>

            <button
              onClick={() => navigate('/dashboard')}
              className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
              <span className="hidden sm:inline">Dashboard</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Loading State */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mb-4"></div>
            <p className="text-slate-400">Loading users...</p>
          </div>
        )}

        {/* Error State */}
        {!loading && error && !users.length && (
          <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-6 text-center">
            <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-red-400 mb-2">Error</h2>
            <p className="text-red-300 mb-4">{error}</p>
            <button
              onClick={() => navigate('/dashboard')}
              className="px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
            >
              Back to Dashboard
            </button>
          </div>
        )}

        {/* Content */}
        {!loading && (
          <>
            {/* Page Header */}
            <div className="mb-8">
              <h1 className="text-3xl font-bold text-slate-100 mb-2">Find Friends</h1>
              <p className="text-slate-400">Connect with other players</p>
            </div>

            {/* Alert Messages */}
            {error && (
              <div className="mb-6 bg-red-900/20 border border-red-500/50 rounded-lg p-4 flex items-start gap-3">
                <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
                <p className="text-red-300 flex-1">{error}</p>
                <button
                  onClick={() => setError(null)}
                  className="text-red-400 hover:text-red-300"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            )}

            {success && (
              <div className="mb-6 bg-green-900/20 border border-green-500/50 rounded-lg p-4 flex items-start gap-3">
                <CheckCircle2 className="w-5 h-5 text-green-400 flex-shrink-0 mt-0.5" />
                <p className="text-green-300 flex-1">{success}</p>
                <button
                  onClick={() => setSuccess(null)}
                  className="text-green-400 hover:text-green-300"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            )}

            {/* Info Bar */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-4 mb-6 flex flex-wrap items-center justify-between gap-4">
              <p className="text-slate-400">
                Found <strong className="text-slate-200">{users.length}</strong> user{users.length !== 1 ? 's' : ''}
              </p>
              <button
                onClick={() => navigate('/friends')}
                className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
              >
                <Users className="w-4 h-4" />
                View Friends
              </button>
            </div>

            {/* Users Grid */}
            {users.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {users.map((user) => (
                  <div
                    key={user.userId}
                    className={`bg-slate-900 border rounded-lg p-6 relative ${
                      user.email === currentUserEmail
                        ? 'border-purple-500'
                        : 'border-slate-800'
                    }`}
                  >
                    {/* Status Indicator */}
                    <div className="absolute top-4 right-4">
                      <Circle className={`w-3 h-3 ${getStatusColor(user.status)} fill-current`} />
                    </div>

                    {/* Avatar */}
                    <div className="flex justify-center mb-4">
                      {user.avatar ? (
                        <div className="relative">
                          <img
                            src={user.avatar}
                            alt={`${user.username}'s avatar`}
                            className="w-20 h-20 rounded-full object-cover border-2 border-slate-800"
                          />
                        </div>
                      ) : (
                        <div className="w-20 h-20 rounded-full bg-slate-800 flex items-center justify-center border-2 border-slate-700">
                          <span className="text-2xl text-slate-400">
                            {user.username.charAt(0).toUpperCase()}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* User Info */}
                    <div className="text-center space-y-2">
                      <h3 className="text-lg font-semibold text-slate-100">
                        {user.username}
                        {user.email === currentUserEmail && (
                          <span className="ml-2 text-xs text-purple-400 font-normal">(You)</span>
                        )}
                      </h3>

                      <div className="flex items-center justify-center gap-1 text-slate-400 text-sm">
                        <Hash className="w-3 h-3" />
                        <span>{user.playerTag}</span>
                      </div>

                      <div className="flex justify-center">
                        <span className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium border ${getStatusBadgeColor(user.status)}`}>
                          <Circle className="w-1.5 h-1.5 fill-current" />
                          {user.status}
                        </span>
                      </div>

                      {user.lastLoginAt && (
                        <p className="text-slate-500 text-xs flex items-center justify-center gap-1">
                          <Clock className="w-3 h-3" />
                          {new Date(user.lastLoginAt).toLocaleDateString()}
                        </p>
                      )}
                    </div>

                    {/* Action Button */}
                    <div className="mt-4">
                      {renderActionButton(user)}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-20">
                <div className="w-20 h-20 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Users className="w-10 h-10 text-slate-600" />
                </div>
                <h2 className="text-xl font-semibold text-slate-300 mb-2">No users found</h2>
                <p className="text-slate-500">Be the first to join BanditGames!</p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
