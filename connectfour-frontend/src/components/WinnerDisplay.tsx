import type { Player } from '../type/game';

interface WinnerDisplayProps {
  winner: Player | null;
  scoreX: number;
  scoreO: number;
  onPlayAgain: () => void;
  onBackToPlatform?: () => void;
  playerXName?: string;
  playerOName?: string;
}

export default function WinnerDisplay({
  winner,
  scoreX,
  scoreO,
  onPlayAgain,
  onBackToPlatform,
  playerXName = 'Player X',
  playerOName = 'Player O'
}: WinnerDisplayProps) {
  const getWinnerMessage = () => {
    if (winner === null) return 'It\'s a Tie!';
    if (winner === 'X') return `${playerXName} Wins!`;
    return `${playerOName} Wins!`;
  };

  const getWinnerColor = () => {
    if (winner === null) return '#6c757d';
    return winner === 'X' ? '#FF4444' : '#FFD700';
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000,
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '3rem',
        borderRadius: '16px',
        textAlign: 'center',
        maxWidth: '500px',
        boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
      }}>
        <div style={{
          fontSize: '3rem',
          marginBottom: '1rem',
        }}>
          {winner === null ? '🤝' : '🎉'}
        </div>

        <h2 style={{
          fontSize: '2.5rem',
          color: getWinnerColor(),
          marginBottom: '1rem',
          fontWeight: 'bold',
        }}>
          {getWinnerMessage()}
        </h2>

        <div style={{
          display: 'flex',
          justifyContent: 'space-around',
          margin: '2rem 0',
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
        }}>
          <div>
            <div style={{
              fontSize: '0.9rem',
              color: '#666',
              marginBottom: '0.5rem',
            }}>
              {playerXName}
            </div>
            <div style={{
              fontSize: '2rem',
              fontWeight: 'bold',
              color: '#FF4444',
            }}>
              {scoreX}
            </div>
          </div>

          <div style={{
            display: 'flex',
            alignItems: 'center',
            fontSize: '1.5rem',
            color: '#999',
          }}>
            -
          </div>

          <div>
            <div style={{
              fontSize: '0.9rem',
              color: '#666',
              marginBottom: '0.5rem',
            }}>
              {playerOName}
            </div>
            <div style={{
              fontSize: '2rem',
              fontWeight: 'bold',
              color: '#FFD700',
            }}>
              {scoreO}
            </div>
          </div>
        </div>

        <div style={{
          display: 'flex',
          gap: '1rem',
          justifyContent: 'center',
        }}>
          <button
            onClick={onPlayAgain}
            style={{
              padding: '1rem 3rem',
              fontSize: '1.1rem',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: '600',
              boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
              transition: 'all 0.2s ease',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#0056b3';
              e.currentTarget.style.transform = 'translateY(-2px)';
              e.currentTarget.style.boxShadow = '0 6px 12px rgba(0,0,0,0.2)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = '#007bff';
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
            }}
          >
            Play Again
          </button>

          {onBackToPlatform && (
            <button
              onClick={onBackToPlatform}
              style={{
                padding: '1rem 3rem',
                fontSize: '1.1rem',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                fontWeight: '600',
                boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
                transition: 'all 0.2s ease',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = '#5a6268';
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 6px 12px rgba(0,0,0,0.2)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = '#6c757d';
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
              }}
            >
              Back to Platform
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
