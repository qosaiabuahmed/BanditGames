import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Gamepad2 } from 'lucide-react';
import AchievementsPanel from '../features/AchievementsPanel';

export default function AchievementsPage() {
    const navigate = useNavigate();

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
                            className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg transition-colors"
                        >
                            <ArrowLeft className="w-4 h-4" />
                            Back to Dashboard
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <AchievementsPanel />
            </div>
        </div>
    );
}