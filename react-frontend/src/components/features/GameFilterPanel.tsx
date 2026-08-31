import type { GameFilters } from '../../services/gameMetadataService';
import { Search, Sparkles, Calendar, Clock, User, Building2, Tag } from 'lucide-react';

interface GameFilterPanelProps {
  filters: GameFilters;
  onFiltersChange: (filters: GameFilters) => void;
  onApply: () => void;
  onClear: () => void;
}

export default function GameFilterPanel({ filters, onFiltersChange, onApply, onClear }: GameFilterPanelProps) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
      <h3 className="text-xl font-semibold text-slate-100 mb-6">Advanced Filters</h3>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
        {/* Game Name */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Search className="w-4 h-4 text-purple-400" />
            Game Name
          </label>
          <input
            type="text"
            placeholder="Search by name..."
            value={filters.name || ''}
            onChange={(e) => onFiltersChange({ ...filters, name: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Status */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Sparkles className="w-4 h-4 text-purple-400" />
            Status
          </label>
          <select
            value={filters.status || ''}
            onChange={(e) => onFiltersChange({ ...filters, status: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all cursor-pointer"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="MAINTENANCE">Maintenance</option>
            <option value="DEPRECATED">Deprecated</option>
          </select>
        </div>

        {/* Category */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Tag className="w-4 h-4 text-purple-400" />
            Category
          </label>
          <input
            type="text"
            placeholder="e.g., Strategy"
            value={filters.category || ''}
            onChange={(e) => onFiltersChange({ ...filters, category: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Theme */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Sparkles className="w-4 h-4 text-purple-400" />
            Theme
          </label>
          <input
            type="text"
            placeholder="e.g., Medieval"
            value={filters.theme || ''}
            onChange={(e) => onFiltersChange({ ...filters, theme: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Designer */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <User className="w-4 h-4 text-purple-400" />
            Designer
          </label>
          <input
            type="text"
            placeholder="Designer name..."
            value={filters.designer || ''}
            onChange={(e) => onFiltersChange({ ...filters, designer: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Publisher */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Building2 className="w-4 h-4 text-purple-400" />
            Publisher
          </label>
          <input
            type="text"
            placeholder="Publisher name..."
            value={filters.publisher || ''}
            onChange={(e) => onFiltersChange({ ...filters, publisher: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Release Year */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Calendar className="w-4 h-4 text-purple-400" />
            Release Year
          </label>
          <input
            type="number"
            placeholder="e.g., 2024"
            value={filters.releaseYear || ''}
            onChange={(e) => onFiltersChange({
              ...filters,
              releaseYear: parseInt(e.target.value) || undefined
            })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Min Duration */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Clock className="w-4 h-4 text-purple-400" />
            Min Duration (min)
          </label>
          <input
            type="number"
            placeholder="Min minutes"
            value={filters.minDuration || ''}
            onChange={(e) => onFiltersChange({
              ...filters,
              minDuration: parseInt(e.target.value) || undefined
            })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Max Duration */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Clock className="w-4 h-4 text-purple-400" />
            Max Duration (min)
          </label>
          <input
            type="number"
            placeholder="Max minutes"
            value={filters.maxDuration || ''}
            onChange={(e) => onFiltersChange({
              ...filters,
              maxDuration: parseInt(e.target.value) || undefined
            })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
          />
        </div>

        {/* Complexity */}
        <div>
          <label className="flex items-center gap-2 text-sm font-medium text-slate-300 mb-2">
            <Sparkles className="w-4 h-4 text-purple-400" />
            Complexity
          </label>
          <select
            value={filters.complexity || ''}
            onChange={(e) => onFiltersChange({ ...filters, complexity: e.target.value })}
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all cursor-pointer"
          >
            <option value="">All Complexities</option>
            <option value="Easy">Easy</option>
            <option value="Medium">Medium</option>
            <option value="Hard">Hard</option>
          </select>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap gap-3 pt-4 border-t border-slate-800">
        <button
          onClick={onApply}
          className="flex-1 min-w-[140px] px-6 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium"
        >
          Apply Filters
        </button>
        <button
          onClick={onClear}
          className="flex-1 min-w-[140px] px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition-colors font-medium border border-slate-700"
        >
          Clear All Filters
        </button>
      </div>
    </div>
  );
}
