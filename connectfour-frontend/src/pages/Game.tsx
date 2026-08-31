import { useLocation, Navigate } from 'react-router-dom';
import GameBoard from '../components/GameBoard';

export default function Game() {
  const location = useLocation();
  const state = location.state as {
    gameId: number;
    playerXName: string;
    playerOName: string;
    playerXType: 'human' | 'ai_mcts';
    playerOType: 'human' | 'ai_mcts';
    currentPlayerRole?: 'X' | 'O';  // Which player this user is in PvP mode
  } | null;

  console.log('[GAME PAGE] Received state:', state);

  if (!state || !state.gameId) {
    console.error('[GAME PAGE] No state or gameId found, redirecting to setup');
    return <Navigate to="/game/setup" replace />;
  }

  console.log('[GAME PAGE] Rendering GameBoard with gameId:', state.gameId);

  return (
    <GameBoard
      gameId={state.gameId}
      playerXName={state.playerXName}
      playerOName={state.playerOName}
      playerXType={state.playerXType}
      playerOType={state.playerOType}
      currentPlayerRole={state.currentPlayerRole}
    />
  );
}
