import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import type { User, UpdateUserRequest } from '../../types/user';
import {
  Gamepad2,
  ArrowLeft,
  Save,
  User as UserIcon,
  Mail,
  Hash,
  Image as ImageIcon,
  CheckCircle,
  AlertCircle
} from 'lucide-react';

export default function EditProfilePage() {
  const navigate = useNavigate();
  const { getEmail } = useAuth();

  const [user, setUser] = useState<User | null>(null);
  const [formData, setFormData] = useState<UpdateUserRequest>({
    username: '',
    email: '',
    playerTag: '',
    avatar: '',
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

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
        setFormData({
          username: userData.username,
          email: userData.email,
          playerTag: userData.playerTag,
          avatar: userData.avatar || '',
        });
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [navigate, getEmail]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!user) {
      setError('Not authenticated');
      navigate('/login');
      return;
    }

    setSaving(true);
    setError(null);

    try {
      const updateData: UpdateUserRequest = {};

      if (formData.username !== user.username) updateData.username = formData.username;
      if (formData.email !== user.email) updateData.email = formData.email;
      if (formData.playerTag !== user.playerTag) updateData.playerTag = formData.playerTag;
      if (formData.avatar !== (user.avatar || '')) updateData.avatar = formData.avatar;

      await UserService.updateUser(user.userId, updateData);
      setSuccess(true);

      setTimeout(() => {
        navigate('/profile');
      }, 1500);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
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

            {/* Back Button */}
            <button
              onClick={() => navigate('/profile')}
              className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
              <span className="hidden sm:inline">Back to Profile</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Loading State */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mb-4"></div>
            <p className="text-slate-400">Loading profile...</p>
          </div>
        )}

        {/* Success State */}
        {success && (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-20 h-20 bg-green-500/20 rounded-full flex items-center justify-center mb-4">
              <CheckCircle className="w-12 h-12 text-green-400" />
            </div>
            <h2 className="text-2xl font-semibold text-slate-100 mb-2">Profile Updated!</h2>
            <p className="text-slate-400">Redirecting to your profile...</p>
          </div>
        )}

        {/* Error State (No User) */}
        {!loading && !user && !success && (
          <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-6 text-center">
            <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-red-400 mb-2">Error</h2>
            <p className="text-red-300">{error || 'Failed to load profile'}</p>
          </div>
        )}

        {/* Edit Form */}
        {!loading && !success && user && (
          <div className="space-y-6">
            {/* Page Header */}
            <div>
              <h1 className="text-3xl font-bold text-slate-100 mb-2">Edit Profile</h1>
              <p className="text-slate-400">Update your profile information</p>
            </div>

            {/* Error Alert */}
            {error && (
              <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 flex items-start gap-3">
                <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
                <p className="text-red-300">{error}</p>
              </div>
            )}

            {/* Form Card */}
            <form onSubmit={handleSubmit} className="bg-slate-900 border border-slate-800 rounded-lg p-6 space-y-6">
              {/* Username Field */}
              <div>
                <label htmlFor="username" className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
                  <UserIcon className="w-4 h-4 text-purple-400" />
                  Username
                </label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="Enter your username"
                />
              </div>

              {/* Email Field */}
              <div>
                <label htmlFor="email" className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
                  <Mail className="w-4 h-4 text-purple-400" />
                  Email
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="your.email@example.com"
                />
              </div>

              {/* Player Tag Field */}
              <div>
                <label htmlFor="playerTag" className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
                  <Hash className="w-4 h-4 text-purple-400" />
                  Player Tag
                </label>
                <input
                  type="text"
                  id="playerTag"
                  name="playerTag"
                  value={formData.playerTag}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="YourTag#1234"
                />
              </div>

              {/* Avatar URL Field */}
              <div>
                <label htmlFor="avatar" className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
                  <ImageIcon className="w-4 h-4 text-purple-400" />
                  Avatar URL
                </label>
                <input
                  type="url"
                  id="avatar"
                  name="avatar"
                  value={formData.avatar}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="https://example.com/avatar.jpg"
                />
                <p className="text-slate-500 text-xs mt-1">
                  Optional - Enter a URL to your profile picture
                </p>
              </div>

              {/* Avatar Preview */}
              {formData.avatar && (
                <div className="bg-slate-800 border border-slate-700 rounded-lg p-4">
                  <p className="text-sm font-medium text-slate-300 mb-3">Avatar Preview</p>
                  <div className="flex justify-center">
                    <img
                      src={formData.avatar}
                      alt="Avatar preview"
                      className="w-24 h-24 rounded-full object-cover border-2 border-purple-500"
                      onError={(e) => {
                        e.currentTarget.style.display = 'none';
                      }}
                    />
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-3 pt-4 border-t border-slate-800">
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 flex items-center justify-center gap-2 px-6 py-3 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-600/50 disabled:cursor-not-allowed text-white rounded-lg transition-colors font-medium"
                >
                  {saving ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" />
                      Save Changes
                    </>
                  )}
                </button>
                <button
                  type="button"
                  onClick={() => navigate('/profile')}
                  disabled={saving}
                  className="flex-1 px-6 py-3 bg-slate-800 hover:bg-slate-700 disabled:bg-slate-800/50 disabled:cursor-not-allowed text-slate-200 rounded-lg transition-colors font-medium"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
