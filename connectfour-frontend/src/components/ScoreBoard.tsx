import type { Player } from '../type/game';

interface ScoreBoardProps {
  scoreX: number;
  scoreO: number;
  currentPlayer: Player;
  currentTurn: number;
  playerXName?: string;
  playerOName?: string;
}

export default function ScoreBoard({
  scoreX,
  scoreO,
  currentPlayer,
  currentTurn,
  playerXName = 'Player X',
  playerOName = 'Player O'
}: ScoreBoardProps) {
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: '2rem',
      padding: '1.5rem',
      backgroundColor: 'white',
      borderRadius: '12px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    }}>
      <div style={{
        flex: 1,
        textAlign: 'center',
        padding: '1rem',
        borderRadius: '8px',
        backgroundColor: currentPlayer === 'X' ? '#FFE5E5' : '#f8f9fa',
        border: currentPlayer === 'X' ? '3px solid #FF4444' : '3px solid transparent',
        transition: 'all 0.3s ease',
      }}>
        <div style={{
          fontSize: '0.9rem',
          color: '#666',
          marginBottom: '0.5rem',
          fontWeight: '500',
        }}>
          {playerXName}
        </div>
        <div style={{
          fontSize: '2.5rem',
          fontWeight: 'bold',
          color: '#FF4444',
          marginBottom: '0.25rem',
        }}>
          {scoreX}
        </div>
        <div style={{
          fontSize: '0.8rem',
          color: '#999',
        }}>
          Player X
        </div>
      </div>

      <div style={{
        flex: 0.5,
        textAlign: 'center',
        padding: '1rem',
      }}>
        <div style={{
          fontSize: '0.9rem',
          color: '#666',
          marginBottom: '0.5rem',
        }}>
          Turn
        </div>
        <div style={{
          fontSize: '2rem',
          fontWeight: 'bold',
          color: '#333',
        }}>
          {currentTurn}
        </div>
        <div style={{
          fontSize: '0.8rem',
          color: currentPlayer === 'X' ? '#FF4444' : '#FFD700',
          fontWeight: '600',
          marginTop: '0.5rem',
        }}>
          {currentPlayer}'s Turn
        </div>
      </div>

      <div style={{
        flex: 1,
        textAlign: 'center',
        padding: '1rem',
        borderRadius: '8px',
        backgroundColor: currentPlayer === 'O' ? '#FFF9E5' : '#f8f9fa',
        border: currentPlayer === 'O' ? '3px solid #FFD700' : '3px solid transparent',
        transition: 'all 0.3s ease',
      }}>
        <div style={{
          fontSize: '0.9rem',
          color: '#666',
          marginBottom: '0.5rem',
          fontWeight: '500',
        }}>
          {playerOName}
        </div>
        <div style={{
          fontSize: '2.5rem',
          fontWeight: 'bold',
          color: '#FFD700',
          marginBottom: '0.25rem',
        }}>
          {scoreO}
        </div>
        <div style={{
          fontSize: '0.8rem',
          color: '#999',
        }}>
          Player O
        </div>
      </div>
    </div>
  );
}
