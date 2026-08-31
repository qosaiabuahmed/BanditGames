import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { AchievementDefinition, CreateGameRequest, PlayerType, RenderType } from '../../types/game';
import { GameMetadataService } from '../../services/gameMetadataService';
import {
  Gamepad2,
  ArrowLeft,
  Save,
  Plus,
  Trash2,
  BookOpen,
  Trophy,
  Users,
  Settings,
  Info,
  AlertCircle,
  CheckCircle
} from 'lucide-react';

const renderTypes: RenderType[] = ['NATIVE_TICTACTOE', 'NATIVE_CONNECT4', 'EXTERNAL_IFRAME', 'STATE_BASED'];
const playerTypes: PlayerType[] = ['HUMAN', 'AI'];

export default function RegisterGamePage() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [form, setForm] = useState<CreateGameRequest>({
    name: '',
    description: '',
    rules: { rulesText: '', rulesUrl: '', summary: '' },
    achievements: [],
    playerConfiguration: { minPlayers: 1, maxPlayers: 2, supportedPlayerTypes: ['HUMAN'] },
    renderType: 'NATIVE_TICTACTOE',
    metaData: {
      category: '',
      theme: '',
      designer: '',
      publisher: '',
      releaseYear: new Date().getFullYear(),
      estimatedDurationMinutes: 30,
      complexity: '',
      frontendUrl: '',
      pictureUrl: '',
    },
  });

  const updateAchievement = (index: number, field: keyof AchievementDefinition, value: string) => {
    setForm((prev) => {
      const next = [...prev.achievements];
      next[index] = { ...next[index], [field]: value } as AchievementDefinition;
      return { ...prev, achievements: next };
    });
  };

  const addAchievement = () => {
    setForm((prev) => ({
      ...prev,
      achievements: [
        ...prev.achievements,
        { achievementId: '', name: '', description: '', iconUrl: '', condition: '' },
      ],
    }));
  };

  const removeAchievement = (index: number) => {
    setForm((prev) => {
      const next = [...prev.achievements];
      next.splice(index, 1);
      return { ...prev, achievements: next };
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      const created = await GameMetadataService.registerGame(form);
      setSuccess('Game registered successfully!');
      setTimeout(() => {
        navigate(`/games/${created.id}`);
      }, 1500);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to register game');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePlayerTypeToggle = (type: PlayerType) => {
    setForm((prev) => {
      const setTypes = new Set(prev.playerConfiguration.supportedPlayerTypes);
      if (setTypes.has(type)) {
        setTypes.delete(type);
      } else {
        setTypes.add(type);
      }
      return {
        ...prev,
        playerConfiguration: {
          ...prev.playerConfiguration,
          supportedPlayerTypes: Array.from(setTypes),
        },
      };
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
              onClick={() => navigate('/games')}
              className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
              <span className="hidden sm:inline">Back to Catalog</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-100 mb-2">Register a Game</h1>
          <p className="text-slate-400">Submit all metadata to add a game to the catalog</p>
        </div>

        {/* Success State */}
        {success && (
          <div className="mb-6 bg-green-900/20 border border-green-500/50 rounded-lg p-6 text-center">
            <CheckCircle className="w-12 h-12 text-green-400 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-green-400 mb-2">Success!</h2>
            <p className="text-green-300">{success}</p>
            <p className="text-slate-400 text-sm mt-2">Redirecting to game details...</p>
          </div>
        )}

        {/* Form */}
        {!success && (
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Alert */}
            {error && (
              <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 flex items-start gap-3">
                <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
                <p className="text-red-300">{error}</p>
              </div>
            )}

            {/* Basics Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Info className="w-5 h-5 text-purple-400" />
                <h3 className="text-lg font-semibold text-slate-100">Basics</h3>
              </div>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Game Name</label>
                  <input
                    type="text"
                    required
                    placeholder="Enter game name"
                    value={form.name}
                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Description</label>
                  <textarea
                    required
                    placeholder="Enter game description"
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all min-h-[100px]"
                  />
                </div>
              </div>
            </div>

            {/* Rules Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <BookOpen className="w-5 h-5 text-purple-400" />
                <h3 className="text-lg font-semibold text-slate-100">Rules</h3>
              </div>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Rules Text</label>
                  <textarea
                    required
                    placeholder="Enter detailed game rules"
                    value={form.rules.rulesText}
                    onChange={(e) => setForm({ ...form, rules: { ...form.rules, rulesText: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all min-h-[120px]"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Rules URL</label>
                  <input
                    type="url"
                    required
                    placeholder="https://example.com/rules"
                    value={form.rules.rulesUrl}
                    onChange={(e) => setForm({ ...form, rules: { ...form.rules, rulesUrl: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Summary</label>
                  <input
                    type="text"
                    required
                    placeholder="Brief rules summary"
                    value={form.rules.summary}
                    onChange={(e) => setForm({ ...form, rules: { ...form.rules, summary: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
              </div>
            </div>

            {/* Metadata Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Info className="w-5 h-5 text-purple-400" />
                <h3 className="text-lg font-semibold text-slate-100">Metadata</h3>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Category</label>
                  <input
                    type="text"
                    placeholder="e.g., Strategy"
                    value={form.metaData.category}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, category: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Theme</label>
                  <input
                    type="text"
                    placeholder="e.g., Medieval"
                    value={form.metaData.theme}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, theme: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Designer</label>
                  <input
                    type="text"
                    placeholder="Game designer name"
                    value={form.metaData.designer}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, designer: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Publisher</label>
                  <input
                    type="text"
                    placeholder="Publisher name"
                    value={form.metaData.publisher}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, publisher: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Release Year</label>
                  <input
                    type="number"
                    min={1900}
                    placeholder="2024"
                    value={form.metaData.releaseYear}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, releaseYear: Number(e.target.value) } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Duration (minutes)</label>
                  <input
                    type="number"
                    min={1}
                    placeholder="30"
                    value={form.metaData.estimatedDurationMinutes}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, estimatedDurationMinutes: Number(e.target.value) } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Complexity</label>
                  <input
                    type="text"
                    placeholder="e.g., Easy, Medium, Hard"
                    value={form.metaData.complexity}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, complexity: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Picture URL</label>
                  <input
                    type="url"
                    placeholder="https://example.com/image.png"
                    value={form.metaData.pictureUrl}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, pictureUrl: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-sm font-medium text-slate-300 mb-2">Frontend URL (optional)</label>
                  <input
                    type="url"
                    placeholder="https://example.com/game"
                    value={form.metaData.frontendUrl}
                    onChange={(e) => setForm({ ...form, metaData: { ...form.metaData, frontendUrl: e.target.value } })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
              </div>
            </div>

            {/* Player Configuration Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Users className="w-5 h-5 text-purple-400" />
                <h3 className="text-lg font-semibold text-slate-100">Player Configuration</h3>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Min Players</label>
                  <input
                    type="number"
                    min={1}
                    placeholder="1"
                    value={form.playerConfiguration.minPlayers}
                    onChange={(e) => setForm({
                      ...form,
                      playerConfiguration: { ...form.playerConfiguration, minPlayers: Number(e.target.value) }
                    })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Max Players</label>
                  <input
                    type="number"
                    min={1}
                    placeholder="2"
                    value={form.playerConfiguration.maxPlayers}
                    onChange={(e) => setForm({
                      ...form,
                      playerConfiguration: { ...form.playerConfiguration, maxPlayers: Number(e.target.value) }
                    })}
                    className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Supported Player Types</label>
                <div className="flex gap-3">
                  {playerTypes.map((type) => (
                    <label
                      key={type}
                      className={`flex items-center gap-2 px-4 py-2.5 rounded-lg border cursor-pointer transition-all ${
                        form.playerConfiguration.supportedPlayerTypes.includes(type)
                          ? 'bg-purple-600 border-purple-500 text-white'
                          : 'bg-slate-800 border-slate-700 text-slate-300 hover:border-slate-600'
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={form.playerConfiguration.supportedPlayerTypes.includes(type)}
                        onChange={() => handlePlayerTypeToggle(type)}
                        className="w-4 h-4"
                      />
                      <span className="font-medium">{type}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>

            {/* Render Type Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Settings className="w-5 h-5 text-purple-400" />
                <h3 className="text-lg font-semibold text-slate-100">Render Type</h3>
              </div>
              <select
                value={form.renderType}
                onChange={(e) => setForm({ ...form, renderType: e.target.value as RenderType })}
                className="w-full px-4 py-2.5 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all cursor-pointer"
              >
                {renderTypes.map((type) => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </select>
            </div>

            {/* Achievements Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <Trophy className="w-5 h-5 text-purple-400" />
                  <h3 className="text-lg font-semibold text-slate-100">Achievements</h3>
                </div>
                <button
                  type="button"
                  onClick={addAchievement}
                  className="flex items-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors font-medium"
                >
                  <Plus className="w-4 h-4" />
                  Add Achievement
                </button>
              </div>

              {form.achievements.length === 0 ? (
                <p className="text-slate-500 text-center py-8">No achievements yet. Click "Add Achievement" to create one.</p>
              ) : (
                <div className="space-y-4">
                  {form.achievements.map((ach, index) => (
                    <div key={index} className="bg-slate-800 border border-slate-700 rounded-lg p-4">
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                        <div>
                          <label className="block text-sm font-medium text-slate-300 mb-2">Achievement ID (optional)</label>
                          <input
                            type="text"
                            placeholder="unique-id"
                            value={ach.achievementId}
                            onChange={(e) => updateAchievement(index, 'achievementId', e.target.value)}
                            className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium text-slate-300 mb-2">Name</label>
                          <input
                            type="text"
                            placeholder="Achievement name"
                            value={ach.name}
                            onChange={(e) => updateAchievement(index, 'name', e.target.value)}
                            className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                          />
                        </div>
                        <div className="sm:col-span-2">
                          <label className="block text-sm font-medium text-slate-300 mb-2">Icon URL</label>
                          <input
                            type="url"
                            placeholder="https://example.com/icon.png"
                            value={ach.iconUrl}
                            onChange={(e) => updateAchievement(index, 'iconUrl', e.target.value)}
                            className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                          />
                        </div>
                        <div className="sm:col-span-2">
                          <label className="block text-sm font-medium text-slate-300 mb-2">Condition</label>
                          <input
                            type="text"
                            placeholder="How to unlock this achievement"
                            value={ach.condition}
                            onChange={(e) => updateAchievement(index, 'condition', e.target.value)}
                            className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                          />
                        </div>
                        <div className="sm:col-span-2">
                          <label className="block text-sm font-medium text-slate-300 mb-2">Description</label>
                          <textarea
                            placeholder="Achievement description"
                            value={ach.description}
                            onChange={(e) => updateAchievement(index, 'description', e.target.value)}
                            className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all min-h-[80px]"
                          />
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={() => removeAchievement(index)}
                        className="flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors font-medium"
                      >
                        <Trash2 className="w-4 h-4" />
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-3 pt-4">
              <button
                type="button"
                onClick={() => navigate('/games')}
                disabled={submitting}
                className="flex-1 px-6 py-3 bg-slate-800 hover:bg-slate-700 disabled:bg-slate-800/50 disabled:cursor-not-allowed text-slate-200 rounded-lg transition-colors font-medium border border-slate-700"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 flex items-center justify-center gap-2 px-6 py-3 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-600/50 disabled:cursor-not-allowed text-white rounded-lg transition-colors font-medium"
              >
                {submitting ? (
                  <>
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    Submitting...
                  </>
                ) : (
                  <>
                    <Save className="w-5 h-5" />
                    Register Game
                  </>
                )}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
