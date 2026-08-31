import { useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import { GameRedirectService } from '../../services/gameRedirectService';
import { GameMetadataService } from '../../services/gameMetadataService';
import { MatchmakingService } from '../../services/matchmakingService';
import type { Lobby } from '../../services/matchmakingService';
import type { Game } from '../../types/game';
import LobbyWaiting from '../common/LobbyWaiting';
import MatchFoundScreen from '../features/MatchFoundScreen';
import NotificationBell from '../notifications/NotificationBell';
import {
  Gamepad2,
  Play,
  Users,
  Trophy,
  Star,
  LogOut,
  UserCircle,
  UserPlus,
  Activity,
  Sparkles
} from 'lucide-react';

export default function DashboardPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { logout, getUsername, getEmail } = useAuth();
    const username = getUsername();
    const [loadingGame, setLoadingGame] = useState(false);
    const [inLobby, setInLobby] = useState(false);
    const [lobby, setLobby] = useState<Lobby | null>(null);
    const [games, setGames] = useState<Game[]>([]);
    const [matchFound, setMatchFound] = useState<{ matchId: string; gameId: string } | null>(null);
    const [loadingGames, setLoadingGames] = useState(true);
    const [gamesError, setGamesError] = useState<string | null>(null);

    // Handle location state for match/lobby from invite acceptance
    useEffect(() => {
        const locationState = location.state as any;

        if (locationState?.showMatchFound && locationState?.matchId && locationState?.gameId) {
            setMatchFound({
                matchId: locationState.matchId,
                gameId: locationState.gameId
            });
            navigate(location.pathname, { replace: true, state: {} });
        } else if (locationState?.showLobbyWaiting && locationState?.lobby) {
            setLobby(locationState.lobby);
            setInLobby(true);
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location, navigate]);

    // Fetch games on mount
    useEffect(() => {
        const fetchGames = async () => {
            try {
                setLoadingGames(true);
                setGamesError(null);

                const fetchedGames = await GameMetadataService.listGames();

                if (!fetchedGames || fetchedGames.length === 0) {
                    setGamesError('No games available');
                    setLoadingGames(false);
                    return;
                }

                setGames(fetchedGames);
                setLoadingGames(false);
            } catch (error) {
                console.error('Failed to fetch games:', error);
                setGamesError(`Failed to load games: ${error instanceof Error ? error.message : 'Unknown error'}`);
                setLoadingGames(false);
            }
        };

        fetchGames();
    }, []);

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const handlePlaySolo = async (gameId: string, gameName: string) => {
        setLoadingGame(true);
        try {
            await GameRedirectService.startGame(gameId);
        } catch (error) {
            console.error(`Failed to start ${gameName}:`, error);
            alert(`Failed to start ${gameName}. Please try again.`);
            setLoadingGame(false);
        }
    };

    const handleFindMatch = async (gameId: string, gameName: string) => {
        setLoadingGame(true);

        try {
            const email = getEmail();

            if (!email) {
                alert('Authentication error. Please log in again.');
                navigate('/login');
                return;
            }

            const user = await UserService.getUserByEmail(email);
            const result = await MatchmakingService.joinLobby(gameId, user.userId);

            if (result.match) {
                setMatchFound({ matchId: result.match.matchId, gameId: result.match.gameId });
            } else {
                setLobby(result.lobby);
                setInLobby(true);
            }
        } catch (error) {
            console.error(`Failed to join matchmaking for ${gameName}:`, error);
            alert(`Failed to join matchmaking. Please try again.`);
        } finally {
            setLoadingGame(false);
        }
    };

    const handleMatchFound = (matchId: string, gameId: string) => {
        setInLobby(false);
        setMatchFound({ matchId, gameId });
    };

    const handleCancelSearch = () => {
        setInLobby(false);
        setLobby(null);
    };

    const handleBackToDashboard = () => {
        setMatchFound(null);
    };

    if (matchFound) {
        return (
            <MatchFoundScreen
                matchId={matchFound.matchId}
                gameId={matchFound.gameId}
                onBackToDashboard={handleBackToDashboard}
            />
        );
    }

    if (inLobby && lobby) {
        return (
            <LobbyWaiting
                lobby={lobby}
                onMatchFound={handleMatchFound}
                onCancel={handleCancelSearch}
            />
        );
    }

    // Get featured games (first 3)
    const featuredGames = games.slice(0, 3);

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
                            <NotificationBell />
                            <button
                                onClick={() => navigate('/profile')}
                                className="p-2 hover:bg-slate-800 rounded-lg transition-colors text-slate-400 hover:text-slate-300"
                                title="View Profile"
                            >
                                <UserCircle className="w-6 h-6" />
                            </button>
                            <button
                                onClick={() => navigate('/find-friends')}
                                className="p-2 hover:bg-slate-800 rounded-lg transition-colors text-slate-400 hover:text-slate-300"
                                title="Find Friends"
                            >
                                <UserPlus className="w-6 h-6" />
                            </button>
                            <button
                                onClick={handleLogout}
                                className="p-2 hover:bg-slate-800 rounded-lg transition-colors text-slate-400 hover:text-slate-300"
                                title="Logout"
                            >
                                <LogOut className="w-6 h-6" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                {/* Welcome Section */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-slate-100 mb-2">
                        Welcome back, {username || 'Player'}!
                    </h1>
                    <p className="text-slate-400">Ready to play some games?</p>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Main Content - 2/3 width */}
                    <div className="lg:col-span-2 space-y-6">
                        {/* Featured Games */}
                        <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
                            <div className="flex items-center justify-between mb-4">
                                <h2 className="text-xl font-semibold text-slate-100">Featured Games</h2>
                                <button
                                    onClick={() => navigate('/games')}
                                    className="text-purple-400 hover:text-purple-300 transition-colors text-sm"
                                >
                                    View All
                                </button>
                            </div>

                            {loadingGames ? (
                                <div className="flex items-center justify-center py-12">
                                    <div className="w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
                                </div>
                            ) : gamesError ? (
                                <div className="text-center py-12">
                                    <p className="text-red-400">{gamesError}</p>
                                </div>
                            ) : featuredGames.length === 0 ? (
                                <div className="text-center py-12">
                                    <p className="text-slate-500">No games available</p>
                                </div>
                            ) : (
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    {featuredGames.map((game, index) => {
                                        const gradients = [
                                            'from-blue-600 to-purple-600',
                                            'from-purple-600 to-pink-600',
                                            'from-orange-600 to-red-600'
                                        ];
                                        return (
                                            <button
                                                key={game.id}
                                                onClick={() => navigate(`/games/${game.id}`)}
                                                className="group relative overflow-hidden rounded-lg p-6 text-center hover:scale-105 transition-transform"
                                            >
                                                <div className={`absolute inset-0 bg-gradient-to-br ${gradients[index % 3]} opacity-90`}></div>
                                                <div className="relative">
                                                    <div className="text-4xl mb-3 flex items-center justify-center">
                                                        {game.metaData?.pictureUrl ? (
                                                            <img
                                                                src={game.metaData.pictureUrl}
                                                                alt={game.name}
                                                                className="w-16 h-16 object-contain"
                                                            />
                                                        ) : (
                                                            <Gamepad2 className="w-12 h-12 text-white" />
                                                        )}
                                                    </div>
                                                    <h3 className="text-white font-semibold mb-1">{game.name}</h3>
                                                    <p className="text-white/80 text-sm">
                                                        {game.playerConfiguration.minPlayers}-{game.playerConfiguration.maxPlayers} Players
                                                    </p>
                                                </div>
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        {/* Quick Match Button */}
                        <button
                            onClick={() => navigate('/games')}
                            className="w-full bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white py-4 rounded-lg transition-all flex items-center justify-center gap-3 group shadow-lg shadow-purple-500/25"
                        >
                            <Play className="w-6 h-6 group-hover:scale-110 transition-transform" />
                            <span className="font-semibold text-lg">Quick Match - Find a Game Now!</span>
                        </button>

                        {/* Available Games */}
                        <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
                            <div className="flex items-center justify-between mb-4">
                                <h2 className="text-xl font-semibold text-slate-100">Available Games</h2>
                                <span className="text-slate-400 text-sm">{games.length} games</span>
                            </div>

                            {loadingGames ? (
                                <div className="flex items-center justify-center py-8">
                                    <div className="w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
                                </div>
                            ) : games.length === 0 ? (
                                <p className="text-slate-500 text-center py-8">No games available</p>
                            ) : (
                                <div className="space-y-3">
                                    {games.map((game) => (
                                        <div
                                            key={game.id}
                                            className="bg-slate-800 hover:bg-slate-750 border border-slate-700 rounded-lg p-4 transition-colors"
                                        >
                                            <div className="flex items-center gap-3">
                                                {/* Game Icon */}
                                                <div className="w-12 h-12 bg-slate-700 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden p-1">
                                                    {game.metaData?.pictureUrl ? (
                                                        <img
                                                            src={game.metaData.pictureUrl}
                                                            alt={game.name}
                                                            className="w-full h-full object-contain"
                                                        />
                                                    ) : (
                                                        <Gamepad2 className="w-6 h-6 text-slate-400" />
                                                    )}
                                                </div>

                                                {/* Game Info */}
                                                <div className="flex-1 min-w-0">
                                                    <h3 className="text-slate-100 font-medium">{game.name}</h3>
                                                    <p className="text-slate-400 text-xs line-clamp-1">
                                                        {game.playerConfiguration.minPlayers}-{game.playerConfiguration.maxPlayers} Players • {game.metaData?.category || 'Board Game'}
                                                    </p>
                                                </div>

                                                {/* Action Buttons */}
                                                <div className="flex items-center gap-2 flex-shrink-0">
                                                    <button
                                                        onClick={() => handlePlaySolo(game.id, game.name)}
                                                        disabled={loadingGame}
                                                        className="px-3 py-2 bg-slate-700 hover:bg-slate-600 text-slate-200 rounded-lg transition-colors font-medium text-sm disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
                                                    >
                                                        Solo
                                                    </button>
                                                    <button
                                                        onClick={() => handleFindMatch(game.id, game.name)}
                                                        disabled={loadingGame}
                                                        className="px-3 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium text-sm disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
                                                    >
                                                        Match
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Sidebar - 1/3 width */}
                    <div className="space-y-6">
                        {/* Quick Actions */}
                        <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
                            <h2 className="text-lg font-semibold text-slate-100 mb-4">Quick Actions</h2>
                            <div className="space-y-3">
                                <button
                                    onClick={() => navigate('/profile')}
                                    className="w-full flex items-center gap-3 p-3 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors text-left"
                                >
                                    <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
                                        <UserCircle className="w-5 h-5 text-white" />
                                    </div>
                                    <div>
                                        <p className="text-slate-100 font-medium">Your Profile</p>
                                        <p className="text-slate-400 text-sm">View & edit</p>
                                    </div>
                                </button>

                                <button
                                    onClick={() => navigate('/find-friends')}
                                    className="w-full flex items-center gap-3 p-3 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors text-left"
                                >
                                    <div className="w-10 h-10 bg-green-600 rounded-lg flex items-center justify-center">
                                        <UserPlus className="w-5 h-5 text-white" />
                                    </div>
                                    <div>
                                        <p className="text-slate-100 font-medium">Find Friends</p>
                                        <p className="text-slate-400 text-sm">Connect with players</p>
                                    </div>
                                </button>

                                <button
                                    onClick={() => navigate('/friends')}
                                    className="w-full flex items-center gap-3 p-3 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors text-left"
                                >
                                    <div className="w-10 h-10 bg-purple-600 rounded-lg flex items-center justify-center">
                                        <Users className="w-5 h-5 text-white" />
                                    </div>
                                    <div>
                                        <p className="text-slate-100 font-medium">Friends List</p>
                                        <p className="text-slate-400 text-sm">View your friends</p>
                                    </div>
                                </button>

                                <button
                                    onClick={() => navigate('/games')}
                                    className="w-full flex items-center gap-3 p-3 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors text-left"
                                >
                                    <div className="w-10 h-10 bg-orange-600 rounded-lg flex items-center justify-center">
                                        <Gamepad2 className="w-5 h-5 text-white" />
                                    </div>
                                    <div>
                                        <p className="text-slate-100 font-medium">Game Catalog</p>
                                        <p className="text-slate-400 text-sm">Browse all games</p>
                                    </div>
                                </button>

                                <button
                                    onClick={() => navigate('/achievements')}
                                    className="w-full flex items-center gap-3 p-3 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors text-left"
                                >
                                    <div className="w-10 h-10 bg-yellow-600 rounded-lg flex items-center justify-center">
                                        <Trophy className="w-5 h-5 text-white" />
                                    </div>
                                    <div>
                                        <p className="text-slate-100 font-medium">Achievements</p>
                                        <p className="text-slate-400 text-sm">View your trophies</p>
                                    </div>
                                </button>
                            </div>
                        </div>

                        {/* Fun Fact */}
                        <div className="bg-gradient-to-br from-purple-900/20 to-pink-900/20 border border-purple-500/20 rounded-lg p-6">
                            <div className="flex items-center gap-2 mb-3">
                                <Sparkles className="w-5 h-5 text-purple-400" />
                                <h3 className="text-slate-100 font-semibold">Did You Know?</h3>
                            </div>
                            <p className="text-slate-300 text-sm leading-relaxed">
                                BanditGames uses AI-powered opponents to give you the best gaming experience!
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}