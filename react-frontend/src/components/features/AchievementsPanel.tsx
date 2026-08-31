import { useState, useEffect } from 'react';
import { AchievementService, type AchievementDto } from '../../services/achievementService';
import { GameMetadataService } from '../../services/gameMetadataService';

export default function AchievementsPanel() {
    const [achievements, setAchievements] = useState<AchievementDto[]>([]);
    const [gameNames, setGameNames] = useState<Map<string, string>>(new Map());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchAchievements();
    }, []);

    const fetchAchievements = async () => {
        try {
            setLoading(true);
            const data = await AchievementService.getMyAchievements();
            setAchievements(data);

            // Fetch game names
            const games = await GameMetadataService.listGames();
            const namesMap = new Map<string, string>();
            games.forEach(game => namesMap.set(game.id, game.name));
            setGameNames(namesMap);

            setLoading(false);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load achievements');
            setLoading(false);
        }
    };

    const getAchievementIcon = (code: string): string => {
        const iconMap: Record<string, string> = {
            'SPEED_DEMON': '⚡',
            'FIRST_BLOOD': '🩸',
            'COMEBACK_KING': '👑',
            'PERFECT_GAME': '💎',
            'MARATHON_PLAYER': '🏃',
            'UNSTOPPABLE': '🔥',
            'LEGENDARY': '🌟',
        };
        return iconMap[code] || '🏆';
    };

    const getAchievementTitle = (code: string): string => {
        return code.split('_').map(word =>
            word.charAt(0) + word.slice(1).toLowerCase()
        ).join(' ');
    };

    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center p-12">
                <div className="animate-spin w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-900/20 border border-red-600/50 rounded-lg p-4 text-red-400">
                {error}
            </div>
        );
    }

    if (achievements.length === 0) {
        return (
            <div className="text-center p-12 bg-slate-800 border border-slate-700 rounded-lg">
                <div className="text-6xl mb-4">🏆</div>
                <h3 className="text-xl font-semibold text-slate-300 mb-2">No Achievements Yet</h3>
                <p className="text-slate-500">Start playing games to unlock achievements!</p>
            </div>
        );
    }

    // Group achievements by game
    const achievementsByGame = achievements.reduce((acc, achievement) => {
        const gameId = achievement.gameId;
        if (!acc[gameId]) {
            acc[gameId] = [];
        }
        acc[gameId].push(achievement);
        return acc;
    }, {} as Record<string, AchievementDto[]>);

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-slate-100">
                    Your Achievements
                </h2>
                <div className="bg-purple-600 text-white px-4 py-2 rounded-lg font-semibold">
                    {achievements.length} Total
                </div>
            </div>

            {Object.entries(achievementsByGame).map(([gameId, gameAchievements]) => (
                <div key={gameId} className="bg-slate-800 border border-slate-700 rounded-lg p-6">
                    <h3 className="text-xl font-semibold text-slate-100 mb-4 flex items-center gap-2">
                        <span className="text-2xl">🎮</span>
                        {gameNames.get(gameId) || 'Unknown Game'}
                        <span className="text-sm font-normal text-slate-400 ml-2">
                            ({gameAchievements.length} achievement{gameAchievements.length !== 1 ? 's' : ''})
                        </span>
                    </h3>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {gameAchievements.map((achievement) => (
                            <div
                                key={achievement.achievementId}
                                className="bg-slate-700 border border-slate-600 rounded-lg p-4 hover:border-purple-500 transition-all"
                            >
                                <div className="flex items-start gap-3">
                                    <div className="text-4xl">
                                        {getAchievementIcon(achievement.achievementCode)}
                                    </div>
                                    <div className="flex-1">
                                        <h4 className="font-semibold text-slate-100 mb-1">
                                            {getAchievementTitle(achievement.achievementCode)}
                                        </h4>
                                        <p className="text-xs text-slate-400 mb-2">
                                            {formatDate(achievement.unlockedAt)}
                                        </p>
                                        <div className="text-xs text-slate-500 font-mono">
                                            Match: {achievement.matchId.substring(0, 8)}...
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
}
