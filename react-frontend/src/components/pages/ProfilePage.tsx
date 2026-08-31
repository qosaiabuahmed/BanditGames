import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import type { User } from '../../types/user';
import {
  Gamepad2,
  ArrowLeft,
  LogOut,
  Mail,
  User as UserIcon,
  Hash,
  Clock,
  Calendar,
  Edit,
  Circle
} from 'lucide-react';

export default function ProfilePage() {
  const navigate = useNavigate();
  const { logout, getEmail } = useAuth();

  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      const email = getEmail();

      if (!email) {
        setError('Not authenticated');
        setLoading(false);
        navigate('/login');
        return;
      }

      try {
        const userData = await UserService.getUserByEmail(email);
        setUser(userData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [navigate, getEmail]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ONLINE':
        return 'bg-green-500/20 text-green-400 border-green-500/50';
      case 'IN_GAME':
        return 'bg-blue-500/20 text-blue-400 border-blue-500/50';
      case 'AWAY':
        return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50';
      default:
        return 'bg-slate-500/20 text-slate-400 border-slate-500/50';
    }
  };

  const getStatusDotColor = (status: string) => {
    switch (status) {
      case 'ONLINE':
        return 'text-green-500';
      case 'IN_GAME':
        return 'text-blue-500';
      case 'AWAY':
        return 'text-yellow-500';
      default:
        return 'text-slate-500';
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
                onClick={() => navigate('/dashboard')}
                className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors"
              >
                <ArrowLeft className="w-4 h-4" />
                <span className="hidden sm:inline">Dashboard</span>
              </button>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">Logout</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Loading State */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mb-4"></div>
            <p className="text-slate-400">Loading profile...</p>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-6 text-center">
            <h2 className="text-xl font-semibold text-red-400 mb-2">Error</h2>
            <p className="text-red-300 mb-4">{error}</p>
            <button
              onClick={handleLogout}
              className="px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
            >
              Logout
            </button>
          </div>
        )}

        {/* Profile Content */}
        {!loading && !error && user && (
          <div className="space-y-6">
            {/* Profile Header Card */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
              {/* Gradient Banner */}
              <div className="h-32 bg-gradient-to-r from-purple-600 to-pink-600"></div>

              {/* Profile Info */}
              <div className="px-6 pb-6">
                {/* Avatar */}
                <div className="flex flex-col sm:flex-row sm:items-end gap-4 -mt-16 mb-6">
                  <div className="relative">
                    {user.avatar ? (
                      <img
                        src={user.avatar}
                        alt="User avatar"
                        className="w-32 h-32 rounded-full border-4 border-slate-900 object-cover"
                      />
                    ) : (
                      <div className="w-32 h-32 rounded-full border-4 border-slate-900 bg-slate-800 flex items-center justify-center">
                        <UserIcon className="w-16 h-16 text-slate-600" />
                      </div>
                    )}
                    {/* Status Dot */}
                    <div className="absolute bottom-2 right-2">
                      <Circle className={`w-6 h-6 ${getStatusDotColor(user.status)} fill-current`} />
                    </div>
                  </div>

                  <div className="flex-1">
                    <h1 className="text-3xl font-bold text-slate-100">{user.username}</h1>
                    <p className="text-slate-400">{user.playerTag}</p>
                  </div>

                  {/* Status Badge */}
                  <div>
                    <span className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(user.status)}`}>
                      <Circle className="w-2 h-2 fill-current" />
                      {user.status}
                    </span>
                  </div>
                </div>

                {/* Edit Profile Button */}
                <button
                  onClick={() => navigate('/edit-profile')}
                  className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium"
                >
                  <Edit className="w-4 h-4" />
                  Edit Profile
                </button>
              </div>
            </div>

            {/* Profile Details Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Contact Information */}
              <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
                <h2 className="text-lg font-semibold text-slate-100 mb-4">Contact Information</h2>
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center">
                      <UserIcon className="w-5 h-5 text-purple-400" />
                    </div>
                    <div>
                      <p className="text-xs text-slate-500">Username</p>
                      <p className="text-slate-200 font-medium">{user.username}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center">
                      <Mail className="w-5 h-5 text-purple-400" />
                    </div>
                    <div>
                      <p className="text-xs text-slate-500">Email</p>
                      <p className="text-slate-200 font-medium">{user.email}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center">
                      <Hash className="w-5 h-5 text-purple-400" />
                    </div>
                    <div>
                      <p className="text-xs text-slate-500">Player Tag</p>
                      <p className="text-slate-200 font-medium">{user.playerTag}</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Account Activity */}
              <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
                <h2 className="text-lg font-semibold text-slate-100 mb-4">Account Activity</h2>
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center">
                      <Calendar className="w-5 h-5 text-purple-400" />
                    </div>
                    <div>
                      <p className="text-xs text-slate-500">Registered</p>
                      <p className="text-slate-200 font-medium">
                        {new Date(user.registeredAt).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric'
                        })}
                      </p>
                    </div>
                  </div>

                  {user.lastLoginAt && (
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center">
                        <Clock className="w-5 h-5 text-purple-400" />
                      </div>
                      <div>
                        <p className="text-xs text-slate-500">Last Login</p>
                        <p className="text-slate-200 font-medium">
                          {new Date(user.lastLoginAt).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </p>
                      </div>
                    </div>
                  )}

                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center">
                      <Circle className={`w-5 h-5 ${getStatusDotColor(user.status)} fill-current`} />
                    </div>
                    <div>
                      <p className="text-xs text-slate-500">Current Status</p>
                      <p className="text-slate-200 font-medium">{user.status}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Statistics Placeholder */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-slate-100 mb-4">Gaming Statistics</h2>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="bg-slate-800 border border-slate-700 rounded-lg p-4 text-center">
                  <p className="text-3xl font-bold text-purple-400 mb-1">-</p>
                  <p className="text-slate-400 text-sm">Games Played</p>
                </div>
                <div className="bg-slate-800 border border-slate-700 rounded-lg p-4 text-center">
                  <p className="text-3xl font-bold text-green-400 mb-1">-</p>
                  <p className="text-slate-400 text-sm">Wins</p>
                </div>
                <div className="bg-slate-800 border border-slate-700 rounded-lg p-4 text-center">
                  <p className="text-3xl font-bold text-yellow-400 mb-1">-</p>
                  <p className="text-slate-400 text-sm">Achievements</p>
                </div>
              </div>
              <p className="text-center text-slate-500 text-sm mt-4">Statistics will be available soon</p>
            </div>
          </div>
        )}

        {/* No User State */}
        {!loading && !error && !user && (
          <div className="text-center py-20">
            <div className="w-20 h-20 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
              <UserIcon className="w-10 h-10 text-slate-600" />
            </div>
            <h2 className="text-xl font-semibold text-slate-300 mb-2">No profile found</h2>
            <p className="text-slate-500">Please register an account first.</p>
          </div>
        )}
      </div>
    </div>
  );
}
