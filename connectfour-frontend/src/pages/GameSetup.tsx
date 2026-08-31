import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { GameApi } from '../service/gameApi';
import { PlatformApi } from '../service/platformApi';
import { useAuth } from '../contexts/AuthContext';

export default function GameSetup() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { token, matchId, setAuth, setUser } = useAuth();

  const [gameMode, setGameMode] = useState<'pvp' | 'pve'>('pve');
  const [playerXName, setPlayerXName] = useState('Player X');
  const [playerOName, setPlayerOName] = useState('Player O');
  const [playerXType, setPlayerXType] = useState<'human' | 'ai_mcts'>('human');
  const [playerOType, setPlayerOType] = useState<'human' | 'ai_mcts'>('ai_mcts');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);
  const [showFallback, setShowFallback] = useState(false);

  // Update player types when game mode changes
  const handleGameModeChange = (mode: 'pvp' | 'pve') => {
    setGameMode(mode);
    if (mode === 'pvp') {
      setPlayerXType('human');
      setPlayerOType('human');
      setPlayerOName('Player O');
    } else {
      setPlayerXType('human');
      setPlayerOType('ai_mcts');
      setPlayerOName('AI Bot');
    }
  };

  const authenticateAndStartGame = async (urlToken: string, urlMatchId: string, urlMode: string | null) => {
    try {
      console.log('==== GAME SETUP DEBUG ====');
      console.log('Authenticating with token:', urlToken.substring(0, 20) + '...');
      console.log('Mode from URL:', urlMode);
      console.log('MatchID from URL:', urlMatchId);
      console.log('urlMode === "pvp"?', urlMode === 'pvp');
      console.log('==========================');
      setLoading(true);
      setError(null);

      const platformUser = await PlatformApi.getCurrentUser(urlToken);

      setUser({
        userId: platformUser.userId,
        username: platformUser.username,
        email: platformUser.email,
        playerTag: platformUser.playerTag
      });

      const playerXName = platformUser.username;
      let playerOName: string;
      let playerXType: 'human' | 'ai_mcts';
      let playerOType: 'human' | 'ai_mcts';
      let gameState;

      // Check mode: pvp (multiplayer) or pve (vs AI)
      if (urlMode === 'pvp') {
        // Multiplayer mode - both players are human
        console.log('[SETUP] Creating MULTIPLAYER game (PvP)');
        playerOName = 'Opponent';
        playerXType = 'human';
        playerOType = 'human';

        gameState = await GameApi.createGameWithMatchId(
          playerXName,
          playerOName,
          urlMatchId
        );

        // Use the actual usernames from the backend gameState
        const actualPlayerXName = gameState.player_x_username || playerXName;
        const actualPlayerOName = gameState.player_o_username || 'Waiting for Player O...';

        // Determine if this player is X or O by matching their username
        const isPlayerX = playerXName === actualPlayerXName;
        const currentPlayerRole = isPlayerX ? 'X' : 'O';

        console.log('[SETUP] My username:', playerXName);
        console.log('[SETUP] Player X username:', actualPlayerXName);
        console.log('[SETUP] Player O username:', actualPlayerOName);
        console.log('[SETUP] My role:', currentPlayerRole);

        navigate('/game/play', {
          state: {
            gameId: gameState.game_id,
            playerXName: actualPlayerXName,
            playerOName: actualPlayerOName,
            playerXType: playerXType,
            playerOType: playerOType,
            currentPlayerRole: currentPlayerRole  // 'X' or 'O'
          }
        });
        return;
      } else {
        // PvE mode - player vs AI
        console.log('[SETUP] Creating SOLO game (PvE)');
        playerOName = 'AI Bot';
        playerXType = 'human';
        playerOType = 'ai_mcts';

        gameState = await GameApi.createGame(
          playerXName,
          playerOName,
          playerXType,
          playerOType
        );
      }

      console.log('[SETUP] Game created successfully with ID:', gameState.game_id);
      console.log('[SETUP] Full game state:', gameState);

      if (urlMode === 'pvp') {
        try {
          await PlatformApi.notifyGameStarted(urlToken, urlMatchId, {
            connectFourGameId: gameState.game_id,
            playerXName: playerXName,
            playerOName: playerOName
          });
        } catch (err) {
          console.error('Failed to notify platform:', err);
        }
      }

      console.log('[SETUP] Navigating to game with ID:', gameState.game_id);
      navigate('/game/play', {
        state: {
          gameId: gameState.game_id,
          playerXName: playerXName,
          playerOName: playerOName,
          playerXType: playerXType,
          playerOType: playerOType
        }
      });
    } catch (err) {
      console.error('Failed to authenticate:', err);
      const errorMessage = err instanceof Error ? err.message : 'Unknown error';
      setError(`Authentication failed: ${errorMessage}`);
      setLoading(false);
      setShowFallback(true);
    }
  };

  useEffect(() => {
    const urlToken = searchParams.get('token');
    const urlMatchId = searchParams.get('matchId');
    const urlMode = searchParams.get('mode');

    if (urlToken && urlMatchId) {
      setAuth(urlToken, urlMatchId);
      authenticateAndStartGame(urlToken, urlMatchId, urlMode);
    }
  }, [searchParams, setAuth, setUser, navigate]);

  const handleStartGame = async () => {
    if (!playerXName.trim() || !playerOName.trim()) {
      setError('Please enter names for both players');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      console.log('[SETUP] Creating game with mode:', gameMode);
      console.log('[SETUP] Player X type:', playerXType);
      console.log('[SETUP] Player O type:', playerOType);

      const gameState = await GameApi.createGame(
        playerXName.trim(),
        playerOName.trim(),
        playerXType,
        playerOType
      );

      console.log('[SETUP] Game created with ID:', gameState.game_id);

      if (token && matchId) {
        try {
          await PlatformApi.notifyGameStarted(token, matchId, {
            connectFourGameId: gameState.game_id,
            playerXName: playerXName.trim(),
            playerOName: playerOName.trim()
          });
        } catch (err) {
          console.error('Failed to notify platform:', err);
        }
      }

      navigate('/game/play', {
        state: {
          gameId: gameState.game_id,
          playerXName: playerXName.trim(),
          playerOName: playerOName.trim(),
          playerXType,
          playerOType,
          currentPlayerRole: gameMode === 'pvp' ? 'X' : undefined  // In PvP, creator is always Player X
        }
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create game');
    } finally {
      setLoading(false);
    }
  };

  if (token && loading) {
    return (
      <div style={{
        minHeight: '100vh',
        backgroundColor: '#f0f2f5',
        padding: '2rem',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
      }}>
        <div style={{
          backgroundColor: 'white',
          borderRadius: '12px',
          padding: '3rem',
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
          maxWidth: '500px',
          width: '100%',
          textAlign: 'center',
        }}>
          <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🔴🟡</div>
          <h2 style={{ color: '#333', marginBottom: '1rem' }}>Starting Connect Four...</h2>
          <p style={{ color: '#666', marginBottom: '2rem' }}>
            Get ready to battle against ConnectBot-9000!
          </p>
          <div style={{
            width: '100%',
            height: '4px',
            backgroundColor: '#e9ecef',
            borderRadius: '2px',
            overflow: 'hidden',
          }}>
            <div style={{
              width: '100%',
              height: '100%',
              backgroundColor: '#007bff',
              animation: 'loading 1.5s ease-in-out infinite',
            }} />
          </div>
          <style>{`
            @keyframes loading {
              0% { transform: translateX(-100%); }
              100% { transform: translateX(100%); }
            }
          `}</style>
        </div>
      </div>
    );
  }

  if (token && error && showFallback) {
    const urlToken = searchParams.get('token');
    const urlMatchId = searchParams.get('matchId');
    const urlMode = searchParams.get('mode');

    return (
      <div style={{
        minHeight: '100vh',
        backgroundColor: '#f0f2f5',
        padding: '2rem',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
      }}>
        <div style={{
          backgroundColor: 'white',
          borderRadius: '12px',
          padding: '3rem',
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
          maxWidth: '600px',
          width: '100%',
        }}>
          <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
            <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>⚠️</div>
            <h2 style={{ color: '#dc3545', marginBottom: '1rem' }}>Platform Connection Issue</h2>
            <p style={{ color: '#666', marginBottom: '1rem', fontSize: '0.95rem' }}>
              {error}
            </p>
            <p style={{ color: '#999', fontSize: '0.85rem', marginBottom: '2rem' }}>
              The backend server is experiencing an internal error. This is likely a temporary issue.
            </p>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <button
              onClick={() => {
                if (urlToken && urlMatchId) {
                  setRetryCount(retryCount + 1);
                  authenticateAndStartGame(urlToken, urlMatchId, urlMode);
                }
              }}
              style={{
                width: '100%',
                padding: '1rem',
                fontSize: '1rem',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                fontWeight: '600',
              }}
            >
              Retry Authentication
            </button>

            <button
              onClick={() => {
                setShowFallback(false);
                setError(null);
              }}
              style={{
                width: '100%',
                padding: '1rem',
                fontSize: '1rem',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                fontWeight: '600',
              }}
            >
              Play Without Platform Integration
            </button>

            <p style={{ color: '#999', fontSize: '0.8rem', textAlign: 'center', marginTop: '0.5rem' }}>
              Note: Playing without integration means your game results won't be synced to the platform.
            </p>
          </div>
        </div>
      </div>
    );
  }

  // @ts-ignore
    return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f0f2f5',
      padding: '2rem',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '3rem',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
        maxWidth: '600px',
        width: '100%',
      }}>
        <h1 style={{
          textAlign: 'center',
          color: '#333',
          marginBottom: '0.5rem',
        }}>
          Connect Four
        </h1>
        <p style={{
          textAlign: 'center',
          color: '#666',
          marginBottom: '0.5rem',
        }}>
          Setup your game
        </p>
        <div style={{
          textAlign: 'center',
          padding: '0.5rem',
          backgroundColor: gameMode === 'pvp' ? '#28a745' : '#ffc107',
          color: 'white',
          borderRadius: '4px',
          marginBottom: '2rem',
          fontWeight: 'bold'
        }}>
          Current Mode: {gameMode === 'pvp' ? 'Player vs Player' : 'Player vs AI'}
          <br />
          <span style={{ fontSize: '0.85rem' }}>
            (X: {playerXType}, O: {playerOType})
          </span>
        </div>

        <div style={{ marginBottom: '2rem', padding: '1rem', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
          <label style={{
            display: 'block',
            marginBottom: '0.75rem',
            color: '#333',
            fontWeight: '600',
            fontSize: '1.1rem',
          }}>
            Game Mode
          </label>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <label style={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              cursor: 'pointer',
              padding: '0.75rem',
              backgroundColor: gameMode === 'pve' ? '#007bff' : 'white',
              color: gameMode === 'pve' ? 'white' : '#333',
              border: '2px solid ' + (gameMode === 'pve' ? '#007bff' : '#ddd'),
              borderRadius: '6px',
              fontWeight: '500',
            }}>
              <input
                type="radio"
                name="gameMode"
                checked={gameMode === 'pve'}
                onChange={() => handleGameModeChange('pve')}
                style={{ marginRight: '0.5rem' }}
              />
              Player vs AI
            </label>
            <label style={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              cursor: 'pointer',
              padding: '0.75rem',
              backgroundColor: gameMode === 'pvp' ? '#007bff' : 'white',
              color: gameMode === 'pvp' ? 'white' : '#333',
              border: '2px solid ' + (gameMode === 'pvp' ? '#007bff' : '#ddd'),
              borderRadius: '6px',
              fontWeight: '500',
            }}>
              <input
                type="radio"
                name="gameMode"
                checked={gameMode === 'pvp'}
                onChange={() => handleGameModeChange('pvp')}
                style={{ marginRight: '0.5rem' }}
              />
              Player vs Player
            </label>
          </div>
        </div>

        <div style={{ marginBottom: '2rem' }}>
          <h3 style={{ color: '#FF4444', marginBottom: '1rem' }}>Player X (Red)</h3>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{
              display: 'block',
              marginBottom: '0.5rem',
              color: '#333',
              fontWeight: '500',
            }}>
              Player Name
            </label>
            <input
              type="text"
              value={playerXName}
              onChange={(e) => setPlayerXName(e.target.value)}
              placeholder="Enter Player X name"
              style={{
                width: '100%',
                padding: '0.75rem',
                fontSize: '1rem',
                border: '2px solid #ddd',
                borderRadius: '6px',
                boxSizing: 'border-box',
              }}
            />
          </div>

          <div>
            <label style={{
              display: 'block',
              marginBottom: '0.5rem',
              color: '#333',
              fontWeight: '500',
            }}>
              Player Type
            </label>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input
                  type="radio"
                  name="playerXType"
                  checked={playerXType === 'human'}
                  onChange={() => setPlayerXType('human')}
                  style={{ marginRight: '0.5rem' }}
                />
                Human
              </label>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input
                  type="radio"
                  name="playerXType"
                  checked={playerXType === 'ai_mcts'}
                  onChange={() => setPlayerXType('ai_mcts')}
                  style={{ marginRight: '0.5rem' }}
                />
                AI
              </label>
            </div>
          </div>
        </div>

        <div style={{
          borderTop: '2px solid #e9ecef',
          paddingTop: '2rem',
          marginBottom: '2rem',
        }}>
          <h3 style={{ color: '#FFD700', marginBottom: '1rem' }}>Player O (Yellow)</h3>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{
              display: 'block',
              marginBottom: '0.5rem',
              color: '#333',
              fontWeight: '500',
            }}>
              Player Name
            </label>
            <input
              type="text"
              value={playerOName}
              onChange={(e) => setPlayerOName(e.target.value)}
              placeholder="Enter Player O name"
              style={{
                width: '100%',
                padding: '0.75rem',
                fontSize: '1rem',
                border: '2px solid #ddd',
                borderRadius: '6px',
                boxSizing: 'border-box',
              }}
            />
          </div>

          <div>
            <label style={{
              display: 'block',
              marginBottom: '0.5rem',
              color: '#333',
              fontWeight: '500',
            }}>
              Player Type
            </label>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input
                  type="radio"
                  name="playerOType"
                  checked={playerOType === 'human'}
                  onChange={() => setPlayerOType('human')}
                  style={{ marginRight: '0.5rem' }}
                />
                Human
              </label>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input
                  type="radio"
                  name="playerOType"
                  checked={playerOType === 'ai_mcts'}
                  onChange={() => setPlayerOType('ai_mcts')}
                  style={{ marginRight: '0.5rem' }}
                />
                AI
              </label>

            </div>
          </div>
        </div>

        {error && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#f8d7da',
            color: '#721c24',
            borderRadius: '6px',
            marginBottom: '1rem',
            border: '2px solid #dc3545',
          }}>
            {error}
          </div>
        )}

        <button
          onClick={handleStartGame}
          disabled={loading}
          style={{
            width: '100%',
            padding: '1rem',
            fontSize: '1.1rem',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: loading ? 'not-allowed' : 'pointer',
            fontWeight: '600',
            opacity: loading ? 0.6 : 1,
          }}
        >
          {loading ? 'Creating Game...' : 'Start Game'}
        </button>
      </div>
    </div>
  );
}
