import Board from './components/Board';
import gameApi, { NewGameRequest } from './api/gameApi';
import { PlatformApi } from './api/platformApi';
import { useState, useEffect } from "react";
import { useSearchParams } from 'react-router-dom';

function App() {
  const [searchParams] = useSearchParams();

  const [gameId, setGameId] = useState<string | null>(null);
  const [board, setBoard] = useState<string[][]>([
    ['-', '-', '-'],
    ['-', '-', '-'],
    ['-', '-', '-']
  ]);
  const [currentPlayer, setCurrentPlayer] = useState<string>('x');
  const [winner, setWinner] = useState<string | null>(null);
  const [isDraw, setIsDraw] = useState<boolean>(false);
  const [message, setMessage] = useState<string>('Configure game settings to start');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Game state (no setup needed - comes from platform)
  const [playerXName, setPlayerXName] = useState<string>('Player X');
  const [playerOName, setPlayerOName] = useState<string>('Player O');
  const [gameMode, setGameMode] = useState<'pvp' | 'vs_ai' | 'pve'>('pvp');
  const [aiDifficulty, setAiDifficulty] = useState<1 | 2 | 3>(2);
  const [aiPlayer, setAiPlayer] = useState<'x' | 'o'>('o');

  // Platform multiplayer state
  const [isPlatformMode, setIsPlatformMode] = useState<boolean>(false);
  const [isOnlineMultiplayer, setIsOnlineMultiplayer] = useState<boolean>(false);
  const [myRole, setMyRole] = useState<'x' | 'o' | null>(null);
  const [platformUsername, setPlatformUsername] = useState<string>('');
  const [polling, setPolling] = useState<boolean>(false);

  // Platform integration: Handle URL parameters on mount
  useEffect(() => {
    const token = searchParams.get('token');
    const matchId = searchParams.get('matchId');
    const mode = searchParams.get('mode');

    if (token && matchId) {
      console.log('[PLATFORM] Detected platform mode:', { matchId, mode });
      setIsPlatformMode(true);
      authenticateAndStartGame(token, matchId, mode);
    } else {
      // No platform params - show error
      setError('This game must be accessed from the BanditGames platform.');
    }
  }, [searchParams]);

  // Polling for online multiplayer game state sync
  useEffect(() => {
    if (!isOnlineMultiplayer || !gameId || winner || isDraw || loading) {
      return;
    }

    console.log('[POLLING] Setting up polling for online multiplayer');
    const pollInterval = setInterval(async () => {
      try {
        const result = await gameApi.getGameState(gameId);

        if (result.success && result.data) {
          const latestState = result.data;

          // Check if board changed
          const boardChanged = JSON.stringify(latestState.board) !== JSON.stringify(board);

          if (boardChanged) {
            console.log('[POLLING] Board updated by opponent');
            setBoard(latestState.board);
            setCurrentPlayer(latestState.current_player);
          }

          // Always check for game end (even if board didn't change, backend might have detected it)
          if (latestState.winner) {
            console.log('[POLLING] Game ended - winner:', latestState.winner);
            setWinner(latestState.winner);
            setPolling(false);
          } else if (latestState.is_draw) {
            console.log('[POLLING] Game ended - draw');
            setIsDraw(true);
            setPolling(false);
          }
        }
      } catch (err) {
        console.error('[POLLING] Failed to poll game state:', err);
      }
    }, 2000); // Poll every 2 seconds

    return () => {
      console.log('[POLLING] Cleaning up polling');
      clearInterval(pollInterval);
    };
  }, [gameId, isOnlineMultiplayer, winner, isDraw, loading, board]);

  const authenticateAndStartGame = async (token: string, matchId: string, mode: string | null) => {
    try {
      console.log('[AUTH] Authenticating with platform...');
      setLoading(true);
      setError(null);

      const platformUser = await PlatformApi.getCurrentUser(token);
      console.log('[AUTH] Authenticated as:', platformUser.username);

      setPlatformUsername(platformUser.username);

      // Check mode: pvp (multiplayer) or pve (vs AI)
      if (mode === 'pvp') {
        console.log('[MULTIPLAYER] Creating/joining online game');
        setIsOnlineMultiplayer(true);

        const gameState = await gameApi.createGameWithMatchId(platformUser.username, matchId);

        console.log('[MULTIPLAYER] Game state:', gameState);
        console.log('[MULTIPLAYER] My username:', platformUser.username);
        console.log('[MULTIPLAYER] Player X from backend:', gameState.player_x_username);
        console.log('[MULTIPLAYER] Player O from backend:', gameState.player_o_username);

        // Determine my role based on username
        const isPlayerX = platformUser.username === gameState.player_x_username;
        const role = isPlayerX ? 'x' : 'o';

        console.log('[MULTIPLAYER] Am I Player X?:', isPlayerX);
        console.log('[MULTIPLAYER] My role:', role);

        setMyRole(role);
        setGameId(gameState.game_id);
        setBoard(gameState.board);
        setCurrentPlayer(gameState.current_player);
        setPlayerXName(gameState.player_x_username || 'Player X');
        setPlayerOName(gameState.player_o_username || 'Waiting for opponent...');

        setMessage(`You are Player ${role.toUpperCase()}. ${role === gameState.current_player ? 'Your turn!' : 'Waiting for opponent...'}`);
        setPolling(true);

        try {
          await PlatformApi.notifyGameStarted(token, matchId, {
            ticTacToeGameId: gameState.game_id,
            playerXName: gameState.player_x_username || '',
            playerOName: gameState.player_o_username || ''
          });
        } catch (err) {
          console.error('[PLATFORM] Failed to notify game started:', err);
        }
      } else {
        // PvE mode (vs AI)
        console.log('[PVE] Creating game vs AI');
        setIsOnlineMultiplayer(false);

        const gameRequest: NewGameRequest = {
          size: 3,
          player_x_username: platformUser.username,
          player_o_username: 'AI',
          game_mode: 'pve',
          ai_difficulty: 2,
          ai_player: 'o'
        };

        const result = await gameApi.createGame(gameRequest);

        if (result.success && result.data) {
          setGameId(result.data.game_id);
          setBoard(result.data.board);
          setCurrentPlayer(result.data.current_player);
          setPlayerXName(platformUser.username);
          setPlayerOName('AI');
          setMessage(`Game started! ${platformUser.username}'s turn`);
          setGameMode('pve');
        } else {
          throw new Error(result.error || 'Failed to create game');
        }
      }

      setLoading(false);
    } catch (err) {
      console.error('[AUTH] Authentication failed:', err);
      setError(`Authentication failed: ${err instanceof Error ? err.message : 'Unknown error'}`);
      setLoading(false);
    }
  };


  const makeAIMove = async (currentGameId: string) => {
    if (!currentGameId || loading) return;

    setLoading(true);
    setError(null);

    const result = await gameApi.makeAIMove(currentGameId);

    if (result.success && result.data) {
      const data = result.data;
      setBoard(data.board);
      setCurrentPlayer(data.current_player);
      setMessage(data.message);

      if (data.winner) {
        setWinner(data.winner);
        const winnerName = data.winner === 'x' ? playerXName : 'AI';
        setMessage(`${winnerName} wins!`);
      } else if (data.is_draw) {
        setIsDraw(true);
        setMessage("It's a draw!");
      }
    } else {
      setError(`AI move failed: ${result.error}`);
    }

    setLoading(false);
  };

  const handleCellClick = async (row: number, col: number) => {
    if (!gameId || winner || isDraw || loading) {
      return;
    }

    // Don't allow moves if it's AI's turn
    if ((gameMode === 'vs_ai' || gameMode === 'pve') && currentPlayer === aiPlayer) {
      return;
    }

    // MULTIPLAYER: Don't allow moves if it's not my turn
    if (isOnlineMultiplayer && myRole && currentPlayer !== myRole) {
      console.log(`[TURN] Not my turn. I am ${myRole}, current player is ${currentPlayer}`);
      setMessage(`Wait for opponent's turn!`);
      return;
    }

    setLoading(true);
    setError(null);

    const result = await gameApi.makeMove(gameId, row, col);

    if (result.success && result.data) {
      const data = result.data;

      if (data.success) {
        setBoard(data.board);
        setCurrentPlayer(data.current_player);
        setMessage(data.message);

        if (data.winner) {
          setWinner(data.winner);
          setPolling(false);
          const winnerName = data.winner === 'x' ? playerXName :
                            ((gameMode === 'vs_ai' || gameMode === 'pve') ? 'AI' : playerOName);
          setMessage(`${winnerName} wins!`);
        } else if (data.is_draw) {
          setIsDraw(true);
          setPolling(false);
          setMessage("It's a draw!");
        } else if ((gameMode === 'vs_ai' || gameMode === 'pve') && data.current_player === aiPlayer) {
          // Trigger AI move after human move
          setLoading(false);
          setTimeout(() => makeAIMove(gameId), 500);
          return;
        }
      } else {
        setMessage(data.message);
      }
    } else {
      setError(`Move failed: ${result.error}`);
    }

    setLoading(false);
  };


  // Show error if not accessed from platform
  if (!isPlatformMode && error) {
    return (
      <div className="app">
        <div className="container">
          <h1 className="title">Tic Tac Toe</h1>
          <div className="error" style={{
            padding: '2rem',
            backgroundColor: '#f8d7da',
            color: '#721c24',
            borderRadius: '8px',
            textAlign: 'center'
          }}>
            <h2>Access Denied</h2>
            <p>{error}</p>
            <p style={{ marginTop: '1rem' }}>
              Please go to <strong>BanditGames Dashboard</strong> and select a game.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app">
      <div className="container">
        <h1 className="title">Tic Tac Toe</h1>

        <div className="game-info">
          <div className="status">
            {winner ? (
              <div className="winner-message">
                {winner === 'x' ? playerXName : ((gameMode === 'vs_ai' || gameMode === 'pve') ? 'AI' : playerOName)} wins!
              </div>
            ) : isDraw ? (
              <div className="draw-message">It's a draw!</div>
            ) : gameId ? (
              <div className="turn-message">
                {isOnlineMultiplayer && myRole ? (
                  <>
                    <div>You are: <strong>{myRole.toUpperCase()}</strong></div>
                    <div>
                      {currentPlayer === myRole ? (
                        <span style={{ color: '#28a745', fontWeight: 'bold' }}>✓ Your turn!</span>
                      ) : (
                        <span style={{ color: '#ffc107' }}>⏳ Waiting for {currentPlayer === 'x' ? playerXName : playerOName}...</span>
                      )}
                    </div>
                  </>
                ) : (
                  <>Current turn: {currentPlayer === 'x' ? playerXName : ((gameMode === 'vs_ai' || gameMode === 'pve') ? 'AI' : playerOName)}</>
                )}
              </div>
            ) : (
              <div className="waiting-message">Loading game...</div>
            )}
          </div>
          {message && <div className="message">{message}</div>}
          {error && <div className="error">{error}</div>}
        </div>

        <Board
          board={board}
          onCellClick={handleCellClick}
          disabled={!gameId || winner !== null || isDraw || loading}
        />

        {loading && <div className="loading">Processing...</div>}

        {(winner || isDraw) && (
          <div style={{ marginTop: '2rem', textAlign: 'center' }}>
            <button
              onClick={() => window.location.href = 'http://localhost'}
              style={{
                padding: '0.75rem 2rem',
                fontSize: '1rem',
                fontWeight: 'bold',
                backgroundColor: '#6366f1',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                transition: 'background-color 0.2s'
              }}
              onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#4f46e5'}
              onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#6366f1'}
            >
              Back to Platform
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
