/**
 * User status utility functions
 * Centralized status handling to avoid code duplication
 */

export type UserStatus = 'ONLINE' | 'IN_GAME' | 'AWAY' | 'OFFLINE';

/**
 * Get the color class for a user status indicator
 */
export const getStatusColor = (status: string): string => {
  switch (status) {
    case 'ONLINE':
      return 'text-green-500';
    case 'IN_GAME':
      return 'text-blue-500';
    case 'AWAY':
      return 'text-yellow-500';
    default:
      return 'text-slate-500';
  }
};

/**
 * Get the badge color classes for a user status
 */
export const getStatusBadgeColor = (status: string): string => {
  switch (status) {
    case 'ONLINE':
      return 'bg-green-500/20 text-green-400 border-green-500/50';
    case 'IN_GAME':
      return 'bg-blue-500/20 text-blue-400 border-blue-500/50';
    case 'AWAY':
      return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50';
    default:
      return 'bg-slate-500/20 text-slate-400 border-slate-500/50';
  }
};
