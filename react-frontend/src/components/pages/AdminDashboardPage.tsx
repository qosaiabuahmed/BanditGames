import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { AdminService } from '../../services/adminService';
import type {
  UserStatistics,
  MatchStatistics,
  GameStatistics,
  GamePlayCount,
  MLPredictionStats
} from '../../types/admin';
import MLPredictionPlayground from '../admin/MLPredictionPlayground';

export default function AdminDashboardPage() {
  const navigate = useNavigate();
  const { logout, getUsername } = useAuth();
  const username = getUsername();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [mlError, setMLError] = useState<string | null>(null);

  // Statistics state
  const [userStats, setUserStats] = useState<UserStatistics | null>(null);
  const [matchStats, setMatchStats] = useState<MatchStatistics | null>(null);
  const [gameStats, setGameStats] = useState<GameStatistics | null>(null);
  const [mostPlayedGames, setMostPlayedGames] = useState<GamePlayCount[]>([]);
  const [mlStats, setMLStats] = useState<MLPredictionStats | null>(null);

  useEffect(() => {
    const fetchAllStats = async () => {
      try {
        setLoading(true);
        setError(null);
        setMLError(null);

        // Fetch core stats (required)
        const [users, matches, games, mostPlayed] = await Promise.all([
          AdminService.getUserStatistics(),
          AdminService.getMatchStatistics(),
          AdminService.getGameStatistics(),
          AdminService.getMostPlayedGames(10),
        ]);

        setUserStats(users);
        setMatchStats(matches);
        setGameStats(games);
        setMostPlayedGames(mostPlayed);

        // Fetch ML stats separately (optional - might not exist yet)
        try {
          const ml = await AdminService.getMLPredictionStats();
          setMLStats(ml);
        } catch (mlErr) {
          console.error('Failed to fetch ML statistics:', mlErr);
          setMLError('ML prediction data not available');
        }
      } catch (err) {
        console.error('Failed to fetch admin statistics:', err);
        setError('Failed to load dashboard statistics');
      } finally {
        setLoading(false);
      }
    };

    fetchAllStats();
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block w-12 h-12 border-4 border-slate-700 border-t-purple-600 rounded-full animate-spin"></div>
          <p className="mt-4 text-slate-400">Loading admin dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center p-8">
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-8 max-w-md text-center">
          <h2 className="text-2xl font-bold text-red-400 mb-4">Error Loading Dashboard</h2>
          <p className="text-slate-400 mb-6">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="px-6 py-3 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-semibold transition-colors"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950">
      {/* Header */}
      <header className="bg-gradient-to-r from-purple-600 to-pink-600 border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
            <h1 className="text-3xl font-bold text-white">🎮 Admin Dashboard</h1>
            <div className="flex items-center gap-3">
              <span className="px-3 py-1 bg-yellow-400 text-slate-900 rounded-full text-sm font-semibold">
                Admin
              </span>
              <span className="text-white font-medium">{username}</span>
              <button
                onClick={() => navigate('/dashboard')}
                className="px-4 py-2 bg-white/20 hover:bg-white/30 text-white rounded-lg font-medium transition-colors"
              >
                User View
              </button>
              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Overview Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* Total Users */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-6 hover:border-purple-600 transition-colors">
            <div className="flex items-center gap-4">
              <div className="text-4xl">👥</div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wide">Total Users</h3>
                <p className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400 mt-1">
                  {userStats?.totalUsers || 0}
                </p>
                <span className="text-sm text-slate-500 mt-1 block">
                  {userStats?.onlineUsers || 0} online
                </span>
              </div>
            </div>
          </div>

          {/* Total Matches */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-6 hover:border-purple-600 transition-colors">
            <div className="flex items-center gap-4">
              <div className="text-4xl">🎯</div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wide">Total Matches</h3>
                <p className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400 mt-1">
                  {matchStats?.totalMatches || 0}
                </p>
                <span className="text-sm text-slate-500 mt-1 block">
                  {matchStats?.matchesByStatus?.FINISHED || 0} finished
                </span>
              </div>
            </div>
          </div>

          {/* Total Games */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-6 hover:border-purple-600 transition-colors">
            <div className="flex items-center gap-4">
              <div className="text-4xl">🎲</div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wide">Total Games</h3>
                <p className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400 mt-1">
                  {gameStats?.totalGames || 0}
                </p>
                <span className="text-sm text-slate-500 mt-1 block">
                  {gameStats?.gamesByStatus?.ACTIVE || 0} active
                </span>
              </div>
            </div>
          </div>

          {/* ML Predictions */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-6 hover:border-purple-600 transition-colors">
            <div className="flex items-center gap-4">
              <div className="text-4xl">🤖</div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wide">ML Predictions</h3>
                <p className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400 mt-1">
                  {mlStats?.statsByModel.reduce((sum, stat) => sum + stat.totalPredictions, 0) || 0}
                </p>
                <span className="text-sm text-slate-500 mt-1 block">
                  {mlStats?.statsByModel.length || 0} models active
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* User Analytics */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-slate-100 mb-6 pb-3 border-b border-slate-800">
            📊 User Analytics
          </h2>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* User Status Distribution */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-slate-100 mb-4">User Status Distribution</h3>
              <div className="space-y-4">
                <div className="flex items-center gap-4">
                  <span className="min-w-[80px] text-sm text-slate-400">Online</span>
                  <div className="flex-1 h-6 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-green-500 to-green-600 transition-all duration-500"
                      style={{
                        width: `${userStats ? (userStats.onlineUsers / userStats.totalUsers) * 100 : 0}%`
                      }}
                    ></div>
                  </div>
                  <span className="min-w-[50px] text-right font-semibold text-slate-100">
                    {userStats?.onlineUsers || 0}
                  </span>
                </div>
                <div className="flex items-center gap-4">
                  <span className="min-w-[80px] text-sm text-slate-400">Offline</span>
                  <div className="flex-1 h-6 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-slate-600 to-slate-700 transition-all duration-500"
                      style={{
                        width: `${userStats ? (userStats.offlineUsers / userStats.totalUsers) * 100 : 0}%`
                      }}
                    ></div>
                  </div>
                  <span className="min-w-[50px] text-right font-semibold text-slate-100">
                    {userStats?.offlineUsers || 0}
                  </span>
                </div>
              </div>
            </div>

            {/* Recent Registrations */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-slate-100 mb-4">Recent Registrations (Last 30 Days)</h3>
              <div className="space-y-3">
                {userStats?.registrationTrend.slice(0, 5).map((trend, idx) => (
                  <div key={idx} className="flex items-center gap-3">
                    <span className="min-w-[100px] text-sm text-slate-400">
                      {new Date(trend.date).toLocaleDateString()}
                    </span>
                    <div className="flex-1 h-5 bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-purple-500 to-pink-500 transition-all duration-500"
                        style={{
                          width: `${Math.min((trend.count / 5) * 100, 100)}%`
                        }}
                      ></div>
                    </div>
                    <span className="min-w-[40px] text-right font-semibold text-slate-100">
                      {trend.count}
                    </span>
                  </div>
                ))}
                {(!userStats?.registrationTrend || userStats.registrationTrend.length === 0) && (
                  <p className="text-center py-4 text-slate-500 italic">No registration data available</p>
                )}
              </div>
            </div>
          </div>
        </section>

        {/* Match Analytics */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-slate-100 mb-6 pb-3 border-b border-slate-800">
            🎯 Match Analytics
          </h2>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Match Status Breakdown */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-slate-100 mb-4">Match Status Breakdown</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">In Progress</span>
                  <p className="text-2xl font-bold text-slate-100 mt-2">
                    {matchStats?.matchesByStatus?.IN_PROGRESS || 0}
                  </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Finished</span>
                  <p className="text-2xl font-bold text-green-400 mt-2">
                    {matchStats?.matchesByStatus?.FINISHED || 0}
                  </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Cancelled</span>
                  <p className="text-2xl font-bold text-red-400 mt-2">
                    {matchStats?.matchesByStatus?.CANCELLED || 0}
                  </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Pending</span>
                  <p className="text-2xl font-bold text-slate-100 mt-2">
                    {matchStats?.matchesByStatus?.PENDING_ASSIGNMENT || 0}
                  </p>
                </div>
              </div>
            </div>

            {/* Average Match Duration */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-slate-100 mb-4">Average Match Duration</h3>
              <div className="flex flex-col items-center justify-center h-full py-8">
                <p className="text-5xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400">
                  {matchStats?.averageMatchDurationMinutes
                    ? `${Math.round(matchStats.averageMatchDurationMinutes)}`
                    : 'N/A'}
                </p>
                {matchStats?.averageMatchDurationMinutes && (
                  <span className="text-sm text-slate-400 mt-3">minutes average game time</span>
                )}
              </div>
            </div>
          </div>
        </section>

        {/* Most Played Games */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-slate-100 mb-6 pb-3 border-b border-slate-800">
            🏆 Most Played Games
          </h2>
          <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-slate-800">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">#</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Game ID</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Matches</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Finished Matches</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Completion Rate</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {mostPlayedGames.map((game, idx) => {
                    const completionRate = game.totalMatches > 0
                      ? ((game.finishedMatches / game.totalMatches) * 100).toFixed(1)
                      : '0';

                    return (
                      <tr key={game.gameId} className="hover:bg-slate-800 transition-colors">
                        <td className="px-6 py-4 text-slate-300">{idx + 1}</td>
                        <td className="px-6 py-4 text-slate-400 font-mono text-sm">
                          {game.gameId.substring(0, 8)}...
                        </td>
                        <td className="px-6 py-4 text-slate-300">{game.totalMatches}</td>
                        <td className="px-6 py-4 text-slate-300">{game.finishedMatches}</td>
                        <td className="px-6 py-4">
                          <span className="px-3 py-1 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-full text-sm font-semibold">
                            {completionRate}%
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                  {mostPlayedGames.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-6 py-8 text-center text-slate-500 italic">
                        No match data available
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </section>

        {/* Game Catalog Stats */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-slate-100 mb-6 pb-3 border-b border-slate-800">
            🎲 Game Catalog
          </h2>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Games by Status */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-slate-100 mb-4">Games by Status</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Active</span>
                  <p className="text-2xl font-bold text-green-400 mt-2">
                    {gameStats?.gamesByStatus?.ACTIVE || 0}
                  </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Inactive</span>
                  <p className="text-2xl font-bold text-slate-100 mt-2">
                    {gameStats?.gamesByStatus?.INACTIVE || 0}
                  </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Maintenance</span>
                  <p className="text-2xl font-bold text-yellow-400 mt-2">
                    {gameStats?.gamesByStatus?.MAINTENANCE || 0}
                  </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                  <span className="text-sm text-slate-400">Deprecated</span>
                  <p className="text-2xl font-bold text-red-400 mt-2">
                    {gameStats?.gamesByStatus?.DEPRECATED || 0}
                  </p>
                </div>
              </div>
            </div>

            {/* Recently Registered Games */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-slate-100 mb-4">Recently Registered Games</h3>
              <div className="space-y-3">
                {gameStats?.recentGames.slice(0, 5).map((game) => (
                  <div key={game.gameId} className="flex items-center justify-between bg-slate-800 rounded-lg p-3">
                    <div className="flex-1">
                      <span className="text-slate-100 font-semibold">{game.name}</span>
                      <span className="text-sm text-slate-400 ml-3">
                        {new Date(game.registeredAt).toLocaleDateString()}
                      </span>
                    </div>
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold uppercase ${
                      game.status === 'ACTIVE' ? 'bg-green-500 text-white' :
                      game.status === 'INACTIVE' ? 'bg-slate-600 text-white' :
                      game.status === 'MAINTENANCE' ? 'bg-yellow-500 text-white' :
                      'bg-red-500 text-white'
                    }`}>
                      {game.status}
                    </span>
                  </div>
                ))}
                {(!gameStats?.recentGames || gameStats.recentGames.length === 0) && (
                  <p className="text-center py-4 text-slate-500 italic">No games registered yet</p>
                )}
              </div>
            </div>
          </div>
        </section>

        {/* ML Model Performance */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold text-slate-100 mb-6 pb-3 border-b border-slate-800">
            🤖 ML Model Performance
          </h2>

          {mlError && (
            <div className="bg-yellow-900/20 border border-yellow-600/50 rounded-lg p-4 mb-4">
              <p className="text-yellow-400 text-sm">{mlError}</p>
            </div>
          )}

          <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-slate-800">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Model Type</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Prediction Type</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Predictions</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-slate-400 uppercase tracking-wider">Avg Response Time</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {mlStats?.statsByModel.map((model, idx) => (
                    <tr key={idx} className="hover:bg-slate-800 transition-colors">
                      <td className="px-6 py-4">
                        <span className="px-3 py-1 bg-slate-700 text-green-400 rounded-md text-sm font-semibold uppercase">
                          {model.modelType}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-slate-300">{model.predictionType}</td>
                      <td className="px-6 py-4 text-slate-300">{model.totalPredictions.toLocaleString()}</td>
                      <td className="px-6 py-4 text-slate-300">{model.avgResponseTimeMs.toFixed(2)} ms</td>
                    </tr>
                  ))}
                  {(!mlStats?.statsByModel || mlStats.statsByModel.length === 0) && (
                    <tr>
                      <td colSpan={4} className="px-6 py-8 text-center text-slate-500 italic">
                        No ML prediction data available
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
            {mlStats?.firstPrediction && (
              <div className="px-6 py-4 bg-slate-800 border-t border-slate-700">
                <p className="text-sm text-slate-400 text-center">
                  First prediction: {new Date(mlStats.firstPrediction).toLocaleString()} |
                  Last prediction: {mlStats.lastPrediction ? new Date(mlStats.lastPrediction).toLocaleString() : 'N/A'}
                </p>
              </div>
            )}
          </div>
        </section>

        {/* ML Prediction Playground */}
        <section className="mb-8">
          <MLPredictionPlayground />
        </section>
      </main>
    </div>
  );
}
