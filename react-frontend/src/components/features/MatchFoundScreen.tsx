import { useState, useEffect } from 'react';
import { GameRedirectService } from '../../services/gameRedirectService';
import { GameMetadataService } from '../../services/gameMetadataService';
import { MatchmakingService, type Match } from '../../services/matchmakingService';
import { UserService } from '../../services/userService';

interface MatchFoundScreenProps {
    matchId: string;
    gameId: string;
    onBackToDashboard: () => void;
}

export default function MatchFoundScreen({ matchId, gameId, onBackToDashboard }: MatchFoundScreenProps) {
    const [isLaunching, setIsLaunching] = useState(false);
    const [gameName, setGameName] = useState<string>('');
    const [match, setMatch] = useState<Match | null>(null);
    const [playerNames, setPlayerNames] = useState<Map<string, string>>(new Map());

    useEffect(() => {
        const fetchGameName = async () => {
            try {
                const games = await GameMetadataService.listGames();
                const game = games.find(g => g.id === gameId);
                setGameName(game?.name || 'Unknown Game');
            } catch (error) {
                console.error('Failed to fetch game name:', error);
                setGameName('Unknown Game');
            }
        };
        fetchGameName();
    }, [gameId]);

    useEffect(() => {
        const fetchMatchAndPlayers = async () => {
            try {
                const matchData = await MatchmakingService.getMatchById(matchId);
                setMatch(matchData);

                // Fetch usernames for all players
                const names = new Map<string, string>();
                for (const player of matchData.players) {
                    try {
                        const user = await UserService.getUserById(player.userId);
                        names.set(player.userId, user.username);
                    } catch (err) {
                        console.error(`Failed to fetch username for ${player.userId}:`, err);
                        names.set(player.userId, 'Player');
                    }
                }
                setPlayerNames(names);
            } catch (error) {
                console.error('Failed to fetch match data:', error);
            }
        };

        fetchMatchAndPlayers();
    }, [matchId]);

    const handleLaunchGame = async () => {
        if (!match || match.players.length < 2) {
            alert('Match data not ready. Please wait...');
            return;
        }

        setIsLaunching(true);
        try {
            const player1 = match.players[0];
            const player2 = match.players[1];

            const gameUrl = await GameRedirectService.getGameUrlWithPlayers(
                gameId,
                matchId,
                player1.userId,
                playerNames.get(player1.userId) || 'Player 1',
                player2.userId,
                playerNames.get(player2.userId) || 'Player 2'
            );

            // Open game in new tab
            window.open(gameUrl, '_blank');
            setIsLaunching(false);
        } catch (error) {
            console.error('Failed to launch game:', error);
            alert('Failed to launch game. Please try again.');
            setIsLaunching(false);
        }
    };
    return (
        <div className="min-h-screen bg-slate-950 flex justify-center items-center p-8">
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-12 shadow-2xl max-w-2xl w-full text-center">
                <div className="text-9xl mb-6 animate-spin-once">
                    🎉
                </div>

                <h2 className="text-green-400 text-4xl font-bold mb-4">
                    Match Found!
                </h2>

                <p className="text-slate-400 text-lg mb-8">
                    Successfully matched with another player
                </p>

                <div className="bg-slate-800 border border-slate-700 rounded-xl p-8 mb-8 text-left">
                    <h3 className="text-slate-100 text-xl font-semibold mb-6 text-center">
                        Match Details
                    </h3>

                    <div className="space-y-3">
                        <div className="flex justify-between p-4 bg-slate-700 rounded-lg">
                            <span className="text-slate-400 font-medium">Match ID:</span>
                            <span className="text-slate-100 font-semibold font-mono text-sm">
                                {matchId.substring(0, 8)}...{matchId.substring(matchId.length - 4)}
                            </span>
                        </div>

                        <div className="flex justify-between p-4 bg-slate-700 rounded-lg">
                            <span className="text-slate-400 font-medium">Players:</span>
                            <span className="text-green-400 font-semibold">
                                2/2 Ready
                            </span>
                        </div>

                        <div className="flex justify-between p-4 bg-slate-700 rounded-lg">
                            <span className="text-slate-400 font-medium">Game:</span>
                            <span className="text-slate-100 font-semibold">
                                {gameName || 'Loading...'}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Player Information */}
                {match && match.players.length >= 2 && (
                    <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 mb-8 text-left">
                        <h3 className="text-slate-100 text-xl font-semibold mb-4 text-center">
                            Player Information
                        </h3>
                        <div className="space-y-3">
                            {/* Player 1 */}
                            <div className="p-4 bg-slate-700 rounded-lg">
                                <div className="flex justify-between mb-2">
                                    <span className="text-slate-400 font-medium">Player 1:</span>
                                    <span className="text-green-400 font-semibold">
                                        {playerNames.get(match.players[0].userId) || 'Loading...'}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-slate-500 text-sm">Player ID:</span>
                                    <span className="text-slate-300 font-mono text-xs">
                                        {match.players[0].userId}
                                    </span>
                                </div>
                            </div>

                            {/* Player 2 */}
                            <div className="p-4 bg-slate-700 rounded-lg">
                                <div className="flex justify-between mb-2">
                                    <span className="text-slate-400 font-medium">Player 2:</span>
                                    <span className="text-green-400 font-semibold">
                                        {playerNames.get(match.players[1].userId) || 'Loading...'}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-slate-500 text-sm">Player ID:</span>
                                    <span className="text-slate-300 font-mono text-xs">
                                        {match.players[1].userId}
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div className="mt-4 text-xs text-slate-500 text-center">
                            💡 Tip: Use these IDs and names to manually update player information in the game if needed
                        </div>
                    </div>
                )}

                <div className="bg-purple-500/20 border border-purple-500/50 text-purple-300 p-4 rounded-lg mb-8 text-sm">
                    Click "Launch Game" below to open {gameName || 'the game'} in a new tab!
                </div>

                <div className="flex flex-col gap-4">
                    <button
                        onClick={handleLaunchGame}
                        disabled={isLaunching}
                        className="w-full px-6 py-4 text-lg bg-green-600 hover:bg-green-700 disabled:bg-slate-700 text-white rounded-lg font-bold transition-all disabled:cursor-not-allowed"
                    >
                        {isLaunching ? 'Launching Game...' : 'Launch Game'}
                    </button>

                    <button
                        onClick={onBackToDashboard}
                        disabled={isLaunching}
                        className="w-full px-4 py-3 bg-transparent border-2 border-purple-600 hover:bg-purple-600 disabled:border-slate-700 text-purple-600 hover:text-white disabled:text-slate-600 rounded-lg font-semibold transition-all disabled:cursor-not-allowed"
                    >
                        Back to Dashboard
                    </button>
                </div>
            </div>
        </div>
    );
}