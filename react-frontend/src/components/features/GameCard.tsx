import type { Game } from '../../types/game';
import { Gamepad2, Users, Clock, Star, Eye } from 'lucide-react';

interface GameCardProps {
  game: Game;
  isFavorite: boolean;
  onToggleFavorite: (gameId: string) => void;
  onViewDetails: (gameId: string) => void;
}

export default function GameCard({ game, isFavorite, onToggleFavorite, onViewDetails }: GameCardProps) {
  const gradients = [
    'from-blue-600 to-purple-600',
    'from-purple-600 to-pink-600',
    'from-orange-600 to-red-600',
    'from-green-600 to-blue-600',
    'from-pink-600 to-purple-600',
  ];

  const gradient = gradients[game.name.length % gradients.length];

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden hover:border-purple-500/50 transition-all group">
      {/* Game Header with Gradient and Icon */}
      <div className={`relative h-32 bg-gradient-to-br ${gradient} flex items-center justify-center`}>
        {game.metaData?.pictureUrl ? (
          <img
            src={game.metaData.pictureUrl}
            alt={game.name}
            className="w-16 h-16 object-contain"
          />
        ) : (
          <Gamepad2 className="w-16 h-16 text-white/90" />
        )}
        {/* Status Badge */}
        <div className="absolute top-3 right-3">
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${
            game.status === 'ACTIVE'
              ? 'bg-green-500/20 text-green-400 border border-green-500/50'
              : 'bg-slate-500/20 text-slate-400 border border-slate-500/50'
          }`}>
            {game.status}
          </span>
        </div>
        {/* Favorite Star */}
        <button
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavorite(game.id);
          }}
          className="absolute top-3 left-3 p-2 bg-black/30 hover:bg-black/50 rounded-lg transition-colors"
        >
          <Star
            className={`w-5 h-5 ${
              isFavorite ? 'fill-yellow-400 text-yellow-400' : 'text-white'
            }`}
          />
        </button>
      </div>

      {/* Game Content */}
      <div className="p-4">
        {/* Game Name */}
        <h3 className="text-lg font-semibold text-slate-100 mb-2 line-clamp-1 group-hover:text-purple-400 transition-colors">
          {game.name}
        </h3>

        {/* Description */}
        <p className="text-slate-400 text-sm mb-4 line-clamp-2 min-h-[40px]">
          {game.description}
        </p>

        {/* Game Details Grid */}
        <div className="space-y-2 mb-4">
          <div className="flex items-center justify-between text-sm">
            <span className="text-slate-500">Category</span>
            <span className="text-slate-300 font-medium">{game.metaData.category}</span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-slate-500">Theme</span>
            <span className="text-slate-300 font-medium">{game.metaData.theme}</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Users className="w-4 h-4 text-slate-500" />
            <span className="text-slate-300">
              {game.playerConfiguration.minPlayers}-{game.playerConfiguration.maxPlayers} Players
            </span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Clock className="w-4 h-4 text-slate-500" />
            <span className="text-slate-300">{game.metaData.estimatedDurationMinutes} min</span>
          </div>
        </div>

        {/* Complexity Badge */}
        <div className="mb-4">
          <span className="inline-block px-3 py-1 bg-slate-800 border border-slate-700 rounded-full text-xs text-slate-300">
            {game.metaData.complexity}
          </span>
        </div>

        {/* Actions */}
        <button
          onClick={() => onViewDetails(game.id)}
          className="w-full py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium flex items-center justify-center gap-2"
        >
          <Eye className="w-4 h-4" />
          View Details
        </button>
      </div>
    </div>
  );
}
