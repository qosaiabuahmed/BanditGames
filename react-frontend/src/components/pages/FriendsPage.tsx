import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { SocialService } from '../../services/socialService';
import type { FriendDto } from '../../types/social';
import { formatDate } from '../../utils/dateUtils';
import {
  Gamepad2,
  ArrowLeft,
  UserPlus,
  Users,
  Clock,
  Check,
  X,
  Circle,
  Hash,
  AlertCircle,
  Calendar
} from 'lucide-react';

type TabType = 'friends' | 'pending' | 'sent';

export default function FriendsPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<TabType>('friends');
  const [friends, setFriends] = useState<FriendDto[]>([]);
  const [pendingRequests, setPendingRequests] = useState<FriendDto[]>([]);
  const [sentRequests, setSentRequests] = useState<FriendDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [friendsData, pendingData, sentData] = await Promise.all([
        SocialService.getFriends(),
        SocialService.getPendingRequests(),
        SocialService.getSentRequests(),
      ]);

      setFriends(friendsData);
      setPendingRequests(pendingData);
      setSentRequests(sentData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleAcceptRequest = async (friendshipId: string) => {
    try {
      setActionLoading(friendshipId);
      setError(null);

      await SocialService.acceptFriendRequest(friendshipId);
      await fetchAllData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to accept request');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeclineRequest = async (friendshipId: string) => {
    try {
      setActionLoading(friendshipId);
      setError(null);

      await SocialService.declineFriendRequest(friendshipId);
      await fetchAllData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to decline request');
    } finally {
      setActionLoading(null);
    }
  };

  const renderFriendCard = (friend: FriendDto, showActions: boolean = false) => {
    const isActionLoading = actionLoading === friend.friendshipId;

    return (
      <div
        key={friend.friendshipId}
        className="bg-slate-900 border border-slate-800 rounded-lg p-6 hover:border-purple-500/50 transition-all"
      >
        <div className="flex items-center gap-4">
          {/* Avatar */}
          <div className="relative flex-shrink-0">
            <div className="w-16 h-16 rounded-full bg-slate-800 flex items-center justify-center border-2 border-slate-700">
              <span className="text-xl text-slate-400">
                {friend.username.slice(0, 2).toUpperCase()}
              </span>
            </div>
            {/* Status Dot (show green for friends) */}
            {friend.status === 'ACCEPTED' && (
              <div className="absolute bottom-0 right-0">
                <Circle className="w-5 h-5 text-green-500 fill-current" />
              </div>
            )}
          </div>

          {/* User Info */}
          <div className="flex-1 min-w-0">
            <h3 className="text-lg font-semibold text-slate-100 mb-1">
              {friend.username}
            </h3>
            <div className="flex items-center gap-1 text-slate-400 text-sm mb-2">
              <Hash className="w-3 h-3" />
              <span>{friend.playerTag}</span>
            </div>
            <div className="flex items-center gap-1 text-slate-500 text-xs">
              <Calendar className="w-3 h-3" />
              <span>
                {friend.acceptedAt
                  ? `Friends since ${formatDate(friend.acceptedAt)}`
                  : `Requested ${formatDate(friend.createdAt)}`
                }
              </span>
            </div>
          </div>

          {/* Status Badge or Actions */}
          <div className="flex-shrink-0">
            {showActions ? (
              <div className="flex gap-2">
                <button
                  onClick={() => handleAcceptRequest(friend.friendshipId)}
                  disabled={isActionLoading}
                  className="flex items-center justify-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-slate-700 text-white rounded-lg transition-colors disabled:cursor-not-allowed"
                >
                  {isActionLoading ? (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    <>
                      <Check className="w-4 h-4" />
                      Accept
                    </>
                  )}
                </button>
                <button
                  onClick={() => handleDeclineRequest(friend.friendshipId)}
                  disabled={isActionLoading}
                  className="flex items-center justify-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 disabled:bg-slate-700 text-white rounded-lg transition-colors disabled:cursor-not-allowed"
                >
                  {isActionLoading ? (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    <>
                      <X className="w-4 h-4" />
                      Decline
                    </>
                  )}
                </button>
              </div>
            ) : (
              <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium border ${
                friend.status === 'ACCEPTED'
                  ? 'bg-green-500/20 text-green-400 border-green-500/50'
                  : 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50'
              }`}>
                <Circle className="w-1.5 h-1.5 fill-current" />
                {friend.status === 'ACCEPTED' ? 'Friends' : 'Pending'}
              </span>
            )}
          </div>
        </div>
      </div>
    );
  };

  const renderContent = () => {
    if (loading) {
      return (
        <div className="flex flex-col items-center justify-center py-20">
          <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mb-4"></div>
          <p className="text-slate-400">Loading...</p>
        </div>
      );
    }

    let data: FriendDto[] = [];
    let emptyMessage = '';
    let showActions = false;

    switch (activeTab) {
      case 'friends':
        data = friends;
        emptyMessage = "You don't have any friends yet. Start by finding people to connect with!";
        break;
      case 'pending':
        data = pendingRequests;
        emptyMessage = "No pending friend requests. You're all caught up!";
        showActions = true;
        break;
      case 'sent':
        data = sentRequests;
        emptyMessage = "You haven't sent any friend requests yet.";
        break;
    }

    if (data.length === 0) {
      return (
        <div className="flex flex-col items-center justify-center py-20">
          <div className="w-20 h-20 bg-slate-800 rounded-full flex items-center justify-center mb-4">
            <Users className="w-10 h-10 text-slate-600" />
          </div>
          <h3 className="text-xl font-semibold text-slate-300 mb-2">
            {activeTab === 'friends' ? 'No friends yet' : activeTab === 'pending' ? 'No pending requests' : 'No sent requests'}
          </h3>
          <p className="text-slate-500 text-center max-w-md mb-6">
            {emptyMessage}
          </p>
          {activeTab === 'friends' && (
            <button
              onClick={() => navigate('/find-friends')}
              className="flex items-center gap-2 px-6 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium"
            >
              <UserPlus className="w-4 h-4" />
              Find Friends
            </button>
          )}
        </div>
      );
    }

    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {data.map((friend) => renderFriendCard(friend, showActions))}
      </div>
    );
  };

  const getTabCount = (tab: TabType) => {
    switch (tab) {
      case 'friends':
        return friends.length;
      case 'pending':
        return pendingRequests.length;
      case 'sent':
        return sentRequests.length;
    }
  };

  return (
    <div className="min-h-screen bg-slate-950">
      {/* Header */}
      <div className="border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <div className="flex items-center gap-2">
              <Gamepad2 className="w-8 h-8 text-purple-400" />
              <span className="text-xl font-semibold text-slate-100">BanditGames</span>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-3">
              <button
                onClick={() => navigate('/find-friends')}
                className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
              >
                <UserPlus className="w-4 h-4" />
                <span className="hidden sm:inline">Find Friends</span>
              </button>
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
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-100 mb-2">Friends</h1>
          <p className="text-slate-400">Manage your friend connections</p>
        </div>

        {/* Error Message */}
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

        {/* Tabs */}
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-2 mb-6 flex flex-wrap gap-2">
          <button
            onClick={() => setActiveTab('friends')}
            className={`flex-1 min-w-[140px] flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg transition-all font-medium ${
              activeTab === 'friends'
                ? 'bg-purple-600 text-white'
                : 'bg-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Friends</span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${
              activeTab === 'friends'
                ? 'bg-white/20 text-white'
                : 'bg-slate-800 text-slate-400'
            }`}>
              {getTabCount('friends')}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('pending')}
            className={`flex-1 min-w-[140px] flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg transition-all font-medium ${
              activeTab === 'pending'
                ? 'bg-purple-600 text-white'
                : 'bg-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Clock className="w-4 h-4" />
            <span>Pending</span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${
              activeTab === 'pending'
                ? 'bg-white/20 text-white'
                : 'bg-slate-800 text-slate-400'
            }`}>
              {getTabCount('pending')}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('sent')}
            className={`flex-1 min-w-[140px] flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg transition-all font-medium ${
              activeTab === 'sent'
                ? 'bg-purple-600 text-white'
                : 'bg-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <UserPlus className="w-4 h-4" />
            <span>Sent</span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${
              activeTab === 'sent'
                ? 'bg-white/20 text-white'
                : 'bg-slate-800 text-slate-400'
            }`}>
              {getTabCount('sent')}
            </span>
          </button>
        </div>

        {/* Content */}
        {renderContent()}
      </div>
    </div>
  );
}
