import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { Game } from '../../types/game';
import { GameMetadataService } from '../../services/gameMetadataService';
import { MatchmakingService } from '../../services/matchmakingService';
import type { Lobby } from '../../services/matchmakingService';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import LobbyWaiting from '../common/LobbyWaiting';
import MatchFoundScreen from '../features/MatchFoundScreen';
import {
  Gamepad2,
  ArrowLeft,
  Users,
  Clock,
  Sparkles,
  BookOpen,
  Trophy,
  Info,
  ExternalLink,
  Play
} from 'lucide-react';

export default function GameDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const { getEmail } = useAuth();
  const [game, setGame] = useState<Game | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [joining, setJoining] = useState(false);
  const [lobby, setLobby] = useState<Lobby | null>(null);
  const [matchId, setMatchId] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      try {
        const data = await GameMetadataService.getGame(id);
        setGame(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load game');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleFindMatch = async () => {
    if (!id || !game) return;

    const email = getEmail();
    if (!email) {
      navigate('/login');
      return;
    }

    try {
      setJoining(true);
      setError(null);

      const user = await UserService.getUserByEmail(email);
      const response = await MatchmakingService.joinLobby(id, user.userId);

      if (response.match) {
        setMatchId(response.match.matchId);
      } else {
        setLobby(response.lobby);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to join matchmaking');
    } finally {
      setJoining(false);
    }
  };

  const handleMatchFound = (foundMatchId: string, _gameId: string) => {
    setLobby(null);
    setMatchId(foundMatchId);
  };

  const handleCancelLobby = () => {
    setLobby(null);
  };

  const handleBackToDashboard = () => {
    navigate('/dashboard');
  };

  if (lobby) {
    return (
      <LobbyWaiting
        lobby={lobby}
        onMatchFound={handleMatchFound}
        onCancel={handleCancelLobby}
      />
    );
  }

  if (matchId && id) {
    return (
      <MatchFoundScreen
        matchId={matchId}
        gameId={id}
        onBackToDashboard={handleBackToDashboard}
      />
    );
  }

  if (!id) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <p className="text-slate-400">Invalid game id.</p>
          <button
            onClick={() => navigate('/games')}
            className="mt-4 px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
          >
            Back to Games
          </button>
        </div>
      </div>
    );
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
              onClick={() => navigate(-1)}
              className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
              <span className="hidden sm:inline">Back</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Loading State */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin mb-4"></div>
            <p className="text-slate-400">Loading game details...</p>
          </div>
        )}

        {/* Error State - Only show if no game loaded */}
        {error && !game && (
          <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-6">
            <p className="text-red-400">
              <strong>Error:</strong> {error}
            </p>
            <button
              onClick={() => navigate('/games')}
              className="mt-4 px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
            >
              Back to Games
            </button>
          </div>
        )}

        {/* Game Details */}
        {!loading && game && (
          <div className="space-y-6">
            {/* Game Header Card */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
              {/* Gradient Banner */}
              <div className="relative h-48 bg-gradient-to-br from-purple-600 via-pink-600 to-purple-600 flex items-center justify-center">
                {game.metaData?.pictureUrl ? (
                  <img
                    src={game.metaData.pictureUrl}
                    alt={game.name}
                    className="w-24 h-24 object-contain"
                  />
                ) : (
                  <Gamepad2 className="w-24 h-24 text-white/90" />
                )}
                {/* Status Badge */}
                <div className="absolute top-4 right-4">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    game.status === 'ACTIVE'
                      ? 'bg-green-500/20 text-green-400 border border-green-500/50'
                      : 'bg-slate-500/20 text-slate-400 border border-slate-500/50'
                  }`}>
                    {game.status}
                  </span>
                </div>
              </div>

              {/* Game Info */}
              <div className="p-6">
                <h1 className="text-3xl font-bold text-slate-100 mb-2">{game.name}</h1>
                <p className="text-slate-400 text-lg">{game.description}</p>

                {/* Quick Stats */}
                <div className="flex flex-wrap gap-4 mt-6">
                  <div className="flex items-center gap-2 px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg">
                    <Users className="w-5 h-5 text-purple-400" />
                    <span className="text-slate-300">
                      {game.playerConfiguration.minPlayers}-{game.playerConfiguration.maxPlayers} Players
                    </span>
                  </div>
                  <div className="flex items-center gap-2 px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg">
                    <Clock className="w-5 h-5 text-purple-400" />
                    <span className="text-slate-300">{game.metaData.estimatedDurationMinutes} min</span>
                  </div>
                  <div className="flex items-center gap-2 px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg">
                    <Sparkles className="w-5 h-5 text-purple-400" />
                    <span className="text-slate-300">{game.metaData.complexity}</span>
                  </div>
                </div>

                {/* Play Button */}
                {game.status === 'ACTIVE' && (
                  <div className="mt-6">
                    {error && (
                      <div className="mb-4 bg-red-900/20 border border-red-500/50 rounded-lg p-3">
                        <p className="text-red-400 text-sm">
                          <strong>Error:</strong> {error}
                        </p>
                      </div>
                    )}
                    <button
                      onClick={handleFindMatch}
                      disabled={joining}
                      className="w-full flex items-center justify-center gap-3 px-8 py-4 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 disabled:from-slate-700 disabled:to-slate-700 text-white rounded-lg transition-all font-bold text-lg shadow-lg hover:shadow-purple-500/50 disabled:cursor-not-allowed disabled:shadow-none"
                    >
                      {joining ? (
                        <>
                          <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                          Finding Match...
                        </>
                      ) : (
                        <>
                          <Play className="w-6 h-6 fill-current" />
                          Find Match & Play
                        </>
                      )}
                    </button>
                    <p className="text-slate-500 text-sm text-center mt-2">
                      Join matchmaking to play with another player
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Metadata Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Info className="w-5 h-5 text-purple-400" />
                <h2 className="text-xl font-semibold text-slate-100">Game Information</h2>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <MetaItem label="Category" value={game.metaData.category} />
                <MetaItem label="Theme" value={game.metaData.theme} />
                <MetaItem label="Designer" value={game.metaData.designer} />
                <MetaItem label="Publisher" value={game.metaData.publisher} />
                <MetaItem label="Release Year" value={game.metaData.releaseYear.toString()} />
                <MetaItem label="Render Type" value={game.renderType} />
                <MetaItem label="Player Types" value={game.playerConfiguration.supportedPlayerTypes.join(', ')} />
              </div>
            </div>

            {/* Rules Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <BookOpen className="w-5 h-5 text-purple-400" />
                <h2 className="text-xl font-semibold text-slate-100">Rules</h2>
              </div>
              <div className="bg-slate-800 border border-slate-700 rounded-lg p-4">
                <p className="text-slate-300 leading-relaxed whitespace-pre-line mb-4">
                  {game.rules.rulesText}
                </p>
                <div className="flex items-center justify-between pt-4 border-t border-slate-700">
                  <p className="text-slate-400 text-sm italic">{game.rules.summary}</p>
                  {game.rules.rulesUrl && (
                    <a
                      href={game.rules.rulesUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="flex items-center gap-2 text-purple-400 hover:text-purple-300 transition-colors text-sm font-medium"
                    >
                      Full Rules
                      <ExternalLink className="w-4 h-4" />
                    </a>
                  )}
                </div>
              </div>
            </div>

            {/* Achievements Section */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Trophy className="w-5 h-5 text-purple-400" />
                <h2 className="text-xl font-semibold text-slate-100">Achievements</h2>
              </div>
              {game.achievements.length === 0 ? (
                <p className="text-slate-500 text-center py-8">No achievements defined for this game.</p>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {game.achievements.map((achievement) => (
                    <div
                      key={achievement.achievementId}
                      className="bg-slate-800 border border-slate-700 rounded-lg p-4 hover:border-purple-500/50 transition-colors"
                    >
                      <div className="flex items-center gap-2 mb-2">
                        <div className="w-8 h-8 bg-gradient-to-br from-yellow-600 to-orange-600 rounded-lg flex items-center justify-center">
                          <Trophy className="w-5 h-5 text-white" />
                        </div>
                        <h3 className="text-slate-100 font-semibold">{achievement.name}</h3>
                      </div>
                      <p className="text-slate-400 text-sm mb-2">{achievement.description}</p>
                      <div className="text-xs text-slate-500">
                        <span className="font-medium">Unlock: </span>
                        <span className="text-slate-400">{achievement.condition}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface MetaItemProps {
  label: string;
  value: string;
}

function MetaItem({ label, value }: MetaItemProps) {
  return (
    <div className="bg-slate-800 border border-slate-700 rounded-lg p-3">
      <div className="text-xs text-slate-500 mb-1">{label}</div>
      <div className="text-slate-100 font-semibold">{value}</div>
    </div>
  );
}
