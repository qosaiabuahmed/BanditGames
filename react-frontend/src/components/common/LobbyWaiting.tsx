import { useState, useEffect } from 'react';
import { MatchmakingService } from '../../services/matchmakingService';
import type { Lobby } from '../../services/matchmakingService';
import { useAuth } from '../../context/AuthContext';
import { UserService } from '../../services/userService';
import InviteFriendModal from '../features/InviteFriendModal';

interface LobbyWaitingProps {
    lobby: Lobby;
    onMatchFound: (matchId: string, gameId: string) => void;
    onCancel: () => void;
}

export default function LobbyWaiting({ lobby, onMatchFound, onCancel }: LobbyWaitingProps) {
    const { getEmail } = useAuth();
    const [currentLobby, setCurrentLobby] = useState<Lobby>(lobby);
    const [polling, setPolling] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [dots, setDots] = useState('');
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [playerNames, setPlayerNames] = useState<Map<string, string>>(new Map());

    const maxPlayers = 2;
    const currentPlayers = currentLobby.players.length;

    useEffect(() => {
        const interval = setInterval(() => {
            setDots(prev => prev.length >= 3 ? '' : prev + '.');
        }, 500);

        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        const fetchPlayerNames = async () => {
            const names = new Map<string, string>();
            for (const player of currentLobby.players) {
                try {
                    const user = await UserService.getUserById(player.userId);
                    names.set(player.userId, user.username);
                } catch (err) {
                    console.error(`Failed to fetch username for ${player.userId}:`, err);
                    names.set(player.userId, 'Player');
                }
            }
            setPlayerNames(names);
        };

        fetchPlayerNames();
    }, [currentLobby.players]);

    useEffect(() => {
        if (!polling) return;

        const interval = setInterval(async () => {
            try {
                const updatedLobby = await MatchmakingService.getLobbyStatus(
                    currentLobby.lobbyId
                );

                setCurrentLobby(updatedLobby);

                if (updatedLobby.status === 'MATCHED' && updatedLobby.matchId) {
                    setPolling(false);
                    onMatchFound(updatedLobby.matchId, currentLobby.gameId);
                }
            } catch (err) {
                console.error('Failed to poll lobby:', err);
                setError('Connection lost. Please try again.');
                setPolling(false);
            }
        }, 2000);

        return () => clearInterval(interval);
    }, [currentLobby.lobbyId, polling, onMatchFound]);

    const handleCancel = async () => {
        setPolling(false);
        try {
            const email = getEmail();

            if (!email) {
                onCancel();
                return;
            }

            const user = await UserService.getUserByEmail(email);

            await MatchmakingService.leaveLobby(currentLobby.lobbyId, user.userId);
            onCancel();
        } catch (err) {
            console.error('Failed to leave lobby:', err);
            onCancel();
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 flex justify-center items-center p-8">
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-12 shadow-2xl max-w-lg w-full text-center">
                <div className="text-8xl mb-6 animate-bounce">
                    🎮
                </div>

                <h2 className="text-slate-100 text-3xl font-bold mb-2">
                    Finding Match{dots}
                </h2>

                <p className="text-slate-400 text-lg mb-8">
                    Waiting for players: <strong className="text-purple-400">
                    {currentPlayers}/{maxPlayers}
                </strong>
                </p>

                {error && (
                    <div className="bg-red-900/20 border border-red-500/50 text-red-400 p-4 rounded-lg mb-6 text-sm">
                        {error}
                    </div>
                )}

                <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 mb-8">
                    <h3 className="text-slate-300 text-base font-semibold mb-4">
                        Players in Lobby:
                    </h3>

                    {currentLobby.players.map((player) => {
                        const username = playerNames.get(player.userId) || 'Loading...';
                        return (
                            <div
                                key={player.userId}
                                className="flex items-center p-3 bg-slate-700 rounded-lg mb-2"
                            >
                                <div className="w-10 h-10 rounded-full bg-purple-600 text-white flex items-center justify-center font-bold mr-4">
                                    {username.charAt(0).toUpperCase()}
                                </div>
                                <div className="text-left flex-1">
                                    <div className="text-slate-100 font-semibold">
                                        {username}
                                    </div>
                                    <div className="text-slate-400 text-sm">
                                        Ready
                                    </div>
                                </div>
                                <div className="text-green-400 text-xl">
                                    ✓
                                </div>
                            </div>
                        );
                    })}

                    {Array.from({ length: maxPlayers - currentPlayers }).map((_, i) => (
                        <div
                            key={`waiting-${i}`}
                            className="flex items-center p-3 bg-slate-700/50 rounded-lg mb-2 border-2 border-dashed border-slate-600"
                        >
                            <div className="w-10 h-10 rounded-full bg-slate-600 text-slate-400 flex items-center justify-center font-bold mr-4">
                                {currentPlayers + i + 1}
                            </div>
                            <div className="text-left flex-1">
                                <div className="text-slate-400 font-semibold">
                                    Waiting...
                                </div>
                                <div className="text-slate-500 text-sm">
                                    Searching for players
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="w-full h-1.5 bg-slate-700 rounded-full overflow-hidden mb-8">
                    <div className="h-full bg-gradient-to-r from-purple-600 to-pink-600 animate-pulse w-full" />
                </div>

                <button
                    onClick={() => setShowInviteModal(true)}
                    disabled={!polling}
                    className="w-full px-4 py-3 bg-purple-600 hover:bg-purple-700 disabled:bg-slate-700 text-white rounded-lg font-semibold transition-all disabled:cursor-not-allowed disabled:opacity-50 mb-3"
                >
                    👥 Invite Friend
                </button>

                <button
                    onClick={handleCancel}
                    disabled={!polling && !error}
                    className="w-full px-4 py-3 bg-red-600 hover:bg-red-700 disabled:bg-slate-700 text-white rounded-lg font-semibold transition-all disabled:cursor-not-allowed disabled:opacity-50"
                >
                    Cancel Search
                </button>
            </div>

            {/* Invite Friend Modal */}
            {showInviteModal && (
                <InviteFriendModal
                    lobbyId={currentLobby.lobbyId}
                    onClose={() => setShowInviteModal(false)}
                />
            )}
        </div>
    );
}