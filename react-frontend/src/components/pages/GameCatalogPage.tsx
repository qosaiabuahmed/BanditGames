import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Game } from '../../types/game';
import { type GameFilters, GameMetadataService } from '../../services/gameMetadataService';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import GameFilterPanel from '../features/GameFilterPanel';
import GameCard from '../features/GameCard';
import {
  Gamepad2,
  ArrowLeft,
  Filter,
  Star,
  Plus,
  Search
} from 'lucide-react';

export default function GameCatalogPage() {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [favoriteGameIds, setFavoriteGameIds] = useState<Set<string>>(new Set());
  const [showFilters, setShowFilters] = useState(false);
  const [, setFilters] = useState<GameFilters>({});
  const [tempFilters, setTempFilters] = useState<GameFilters>({});
  const [showOnlyFavorites, setShowOnlyFavorites] = useState(false);

  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const loadGames = async (filterCriteria?: GameFilters) => {
    setLoading(true);
    try {
      const [allGames, favoriteIds] = await Promise.all([
        GameMetadataService.listGames(filterCriteria),
        isAuthenticated
          ? UserService.getFavoriteGameIds()
          : Promise.resolve([])
      ]);

      setGames(allGames);
      setFavoriteGameIds(new Set(favoriteIds));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load games');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGames();
  }, []);

  const displayedGames = showOnlyFavorites
    ? games.filter(game => favoriteGameIds.has(game.id))
    : games;

  const applyFilters = () => {
    setFilters(tempFilters);
    loadGames(tempFilters);
    setShowFilters(false);
  };

  const clearFilters = () => {
    setTempFilters({});
    setFilters({});
    setShowOnlyFavorites(false);
    loadGames();
  };

  const toggleFavorite = async (gameId: string) => {
    if (!isAuthenticated) {
      alert('Please log in to mark games.')
      return;
    }

    const isFavorite = favoriteGameIds.has(gameId);
    try {
      if (isFavorite) {
        await UserService.removeFavoriteGame(gameId);
        const newSet = new Set(favoriteGameIds);
        newSet.delete(gameId);
        setFavoriteGameIds(newSet);
      } else {
        await UserService.addFavoriteGame(gameId);
        setFavoriteGameIds(new Set(favoriteGameIds).add(gameId));
      }
    } catch {
      alert('Failed to update favorite status.')
    }
  }

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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-100 mb-2">Game Catalog</h1>
          <p className="text-slate-400">Browse all available games and find your next favorite</p>
        </div>

        {/* Filters & Actions Bar */}
        <div className="flex flex-wrap items-center gap-3 mb-6">
          {/* Filter Toggle */}
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors font-medium ${
              showFilters
                ? 'bg-purple-600 hover:bg-purple-700 text-white'
                : 'bg-slate-800 hover:bg-slate-700 text-slate-200'
            }`}
          >
            <Filter className="w-4 h-4" />
            {showFilters ? 'Hide Filters' : 'Filters'}
          </button>

          {/* Show Favorites Toggle */}
          {isAuthenticated && favoriteGameIds.size > 0 && (
            <button
              onClick={() => setShowOnlyFavorites(!showOnlyFavorites)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors font-medium ${
                showOnlyFavorites
                  ? 'bg-yellow-600 hover:bg-yellow-700 text-white'
                  : 'bg-slate-800 hover:bg-slate-700 text-slate-200'
              }`}
            >
              <Star className={`w-4 h-4 ${showOnlyFavorites ? 'fill-white' : ''}`} />
              Favorites
              {showOnlyFavorites && (
                <span className="px-2 py-0.5 bg-white/20 rounded-full text-xs font-bold">
                  {displayedGames.length}
                </span>
              )}
            </button>
          )}

          {/* Register Game Button */}
          {isAuthenticated && (
            <button
              onClick={() => navigate('/games/new')}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors font-medium"
            >
              <Plus className="w-4 h-4" />
              <span className="hidden sm:inline">Register Game</span>
            </button>
          )}

          {/* Game Count */}
          <div className="ml-auto text-slate-400 text-sm">
            {showOnlyFavorites && displayedGames.length !== games.length ? (
              <span>Showing <strong className="text-slate-200">{displayedGames.length}</strong> of <strong className="text-slate-200">{games.length}</strong> games</span>
            ) : (
              <span>Found <strong className="text-slate-200">{games.length}</strong> game{games.length !== 1 ? 's' : ''}</span>
            )}
          </div>
        </div>

        {/* Filter Panel */}
        {showFilters && (
          <div className="mb-6">
            <GameFilterPanel
              filters={tempFilters}
              onFiltersChange={setTempFilters}
              onApply={applyFilters}
              onClear={clearFilters}
            />
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mb-4"></div>
            <p className="text-slate-400">Loading games...</p>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 mb-6">
            <p className="text-red-400">
              <strong>Error:</strong> {error}
            </p>
          </div>
        )}

        {/* Games Grid */}
        {!loading && !error && (
          <>
            {displayedGames.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {displayedGames.map((game) => (
                  <GameCard
                    key={game.id}
                    game={game}
                    isFavorite={favoriteGameIds.has(game.id)}
                    onToggleFavorite={toggleFavorite}
                    onViewDetails={(gameId) => navigate(`/games/${gameId}`)}
                  />
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-20">
                <div className="w-20 h-20 bg-slate-800 rounded-full flex items-center justify-center mb-4">
                  {showOnlyFavorites ? (
                    <Star className="w-10 h-10 text-slate-600" />
                  ) : (
                    <Search className="w-10 h-10 text-slate-600" />
                  )}
                </div>
                <h3 className="text-xl font-semibold text-slate-300 mb-2">
                  {showOnlyFavorites ? 'No favorites yet' : 'No games found'}
                </h3>
                <p className="text-slate-500 text-center max-w-md">
                  {showOnlyFavorites
                    ? "You haven't favorited any games yet. Click the star icon on games to add them to your favorites!"
                    : "No games match your current filters. Try adjusting your search criteria."}
                </p>
                {showOnlyFavorites && (
                  <button
                    onClick={() => setShowOnlyFavorites(false)}
                    className="mt-6 px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium"
                  >
                    View All Games
                  </button>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}