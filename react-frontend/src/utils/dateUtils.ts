/**
 * Date formatting utilities
 */

/**
 * Format a date string to a human-readable format
 * Example: "Jan 15, 2025"
 */
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  });
};
