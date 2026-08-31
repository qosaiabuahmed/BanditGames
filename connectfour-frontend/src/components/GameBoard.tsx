import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import type {AIRecommendation, GameState} from '../type/game';
import {GameApi} from '../service/gameApi';
import {useAuth} from '../contexts/AuthContext';
import Cell from './Cell';
import ScoreBoard from './ScoreBoard';
import GameControls from './GameControls';
import WinnerDisplay from './WinnerDisplay';

interface GameBoardProps {
    gameId: number;
    playerXName: string;
    playerOName: string;
    playerXType: 'human' | 'ai_mcts';
    playerOType: 'human' | 'ai_mcts';
    currentPlayerRole?: 'X' | 'O';  // Which player this user is in PvP mode
}

export default function GameBoard({
                                      gameId,
                                      playerXName: initialPlayerXName,
                                      playerOName: initialPlayerOName,
                                      playerXType,
                                      playerOType,
                                      currentPlayerRole
                                  }: GameBoardProps) {
    const navigate = useNavigate();
    const {token} = useAuth();
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [aiHint, setAiHint] = useState<AIRecommendation | null>(null);
    const [isAnimating, setIsAnimating] = useState(false);

    // Use state for player names so they can be updated when opponent joins
    const [playerXName, setPlayerXName] = useState(initialPlayerXName);
    const [playerOName, setPlayerOName] = useState(initialPlayerOName);

    console.log('[GAMEBOARD] Initialized with player types:', {playerXType, playerOType, currentPlayerRole});

    const isPvPMode = playerXType === 'human' && playerOType === 'human';

    useEffect(() => {
        loadGameState();
    }, [gameId]);

    // Polling for remote multiplayer (PvP mode ONLY)
    // This allows both players to see each other's moves in real-time
    useEffect(() => {
        if (!isPvPMode || !gameState || gameState.game_over || isAnimating) {
            return;
        }

        console.log('[POLLING] Setting up polling for PvP mode');
        const pollInterval = setInterval(async () => {
            try {
                const latestState = await GameApi.getGameState(gameId);

                // Update player names if they've changed (e.g., when opponent joins)
                if (latestState.player_x_username && latestState.player_x_username !== playerXName) {
                    console.log('[POLLING] Player X name updated:', latestState.player_x_username);
                    setPlayerXName(latestState.player_x_username);
                }
                if (latestState.player_o_username && latestState.player_o_username !== playerOName) {
                    console.log('[POLLING] Player O name updated:', latestState.player_o_username);
                    setPlayerOName(latestState.player_o_username);
                }

                // Only update if the board has changed (opponent made a move)
                const currentBoardStr = JSON.stringify(gameState.board);
                const latestBoardStr = JSON.stringify(latestState.board);

                if (currentBoardStr !== latestBoardStr) {
                    console.log('[POLLING] Board changed, updating state');
                    setGameState(latestState);
                }
            } catch (err) {
                console.error('[POLLING] Failed to poll game state:', err);
            }
        }, 2000); // Poll every 2 seconds

        return () => {
            console.log('[POLLING] Cleaning up polling');
            clearInterval(pollInterval);
        };
    }, [gameId, gameState, isPvPMode, isAnimating, playerXName, playerOName]);

    // AI moves are now handled by the backend in PvE mode
    // No need for separate AI triggering

    const loadGameState = async () => {
        try {
            console.log('[LOAD] Loading game state for gameId:', gameId);
            setLoading(true);
            const state = await GameApi.getGameState(gameId);
            console.log('[LOAD] Game state loaded successfully:', state);
            console.log('[LOAD] Player types - X:', playerXType, 'O:', playerOType);

            // Update player names from game state if available (for multiplayer)
            if (state.player_x_username) {
                console.log('[LOAD] Updating Player X name to:', state.player_x_username);
                setPlayerXName(state.player_x_username);
            }
            if (state.player_o_username) {
                console.log('[LOAD] Updating Player O name to:', state.player_o_username);
                setPlayerOName(state.player_o_username);
            }

            setGameState(state);
            setError(null);

            // No need to trigger AI moves - backend handles AI in PvE mode
        } catch (err) {
            console.error('[LOAD] Failed to load game:', err);
            setError(err instanceof Error ? err.message : 'Failed to load game');
        } finally {
            setLoading(false);
        }
    };

    // handleAIMove is no longer needed - backend handles AI moves in PvE mode

    const handleColumnClick = async (column: number) => {
        if (loading || !gameState || gameState.game_over || isAnimating) return;
        if (!gameState.valid_moves.includes(column)) return;

        // In PvP mode, check if it's this player's turn
        if (isPvPMode && currentPlayerRole) {
            if (gameState.current_player !== currentPlayerRole) {
                console.log('[TURN] Not your turn! Current player:', gameState.current_player, 'Your role:', currentPlayerRole);
                setError(`It's not your turn! Wait for ${gameState.current_player === 'X' ? playerXName : playerOName} to play.`);
                setTimeout(() => setError(null), 3000);
                return;
            }
        }

        const currentPlayerType =
            gameState.current_player === 'X' ? playerXType : playerOType;
        if (currentPlayerType === 'ai_mcts') return;

        const nextPlayerType =
            gameState.current_player === 'X' ? playerOType : playerXType;
        const isPvEMode = nextPlayerType === 'ai_mcts';

        console.log('[HUMAN] Click detected, making move at column', column);
        console.log('[HUMAN] Current player:', gameState.current_player, 'Next player type:', nextPlayerType);
        console.log('[HUMAN] Mode:', isPvEMode ? 'PvE (backend will include AI move)' : 'PvP (only human move)');

        setLoading(true);
        setAiHint(null);
        setIsAnimating(true);

        // Show the human piece immediately for better UX (both PvP and PvE)
        // Find the row where the piece will land
        let targetRow = -1;
        for (let row = gameState.board.length - 1; row >= 0; row--) {
            if (gameState.board[row][column] === '.') {
                targetRow = row;
                break;
            }
        }

        if (targetRow !== -1) {
            // Create a local copy of the board with just the human move
            const newBoard = gameState.board.map(row => [...row]);
            newBoard[targetRow][column] = gameState.current_player;

            // Update state locally to show ONLY the human piece first
            const localState: GameState = {
                ...gameState,
                board: newBoard
            };
            console.log('[HUMAN] Showing human piece locally at column', column, 'row', targetRow);
            setGameState(localState);
        }

        try {
            // Make the move on the backend
            const moveResponse = await GameApi.makeMove(
                gameId,
                column,
                gameState.current_player
            );
            console.log('[HUMAN] Move response received, board_after:', moveResponse.board_after);

            setError(null);
            setLoading(false);

            if (isPvEMode) {
                // In PvE mode: backend response includes BOTH moves (human + AI)
                // Wait for human piece animation, then show AI piece
                console.log('[HUMAN] PvE: Waiting 1.5s for human piece animation...');
                setTimeout(async () => {
                    console.log('[HUMAN] PvE: Human animation complete, fetching state with AI move');

                    // Fetch the latest game state which includes the AI's move
                    // This will trigger the AI piece to animate
                    const latestState = await GameApi.getGameState(gameId);
                    setGameState(latestState);

                    // Wait for AI piece animation to complete before allowing new moves
                    setTimeout(() => {
                        console.log('[HUMAN] PvE: AI animation complete, ready for next move');
                        setIsAnimating(false);
                    }, 1500);
                }, 1500);
            } else {
                // In PvP mode: Wait for animation, then fetch state
                console.log('[HUMAN] PvP: Waiting 1.5s for animation to complete...');
                setTimeout(async () => {
                    console.log('[HUMAN] PvP: Animation complete, fetching final state');
                    setIsAnimating(false);

                    // Fetch the real state from backend
                    const state = await GameApi.getGameState(gameId);
                    setGameState(state);
                }, 1500);
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to make move');
            setIsAnimating(false);
            setLoading(false);
        }
    };

    const handleGetAIHint = async () => {
        if (!gameState || gameState.game_over) return;

        try {
            const hint = await GameApi.getAIMove(gameId);
            setAiHint(hint);
            setTimeout(() => setAiHint(null), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to get AI hint');
        }
    };

    const handleNewGame = () => {
        navigate('/game/setup');
    };

    const handleBackToPlatform = () => {
        window.location.href = 'http://localhost';
    };

    if (loading && !gameState) {
        return (
            <div style={{
                minHeight: '100vh',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                backgroundColor: '#f0f2f5',
            }}>
                <div style={{fontSize: '1.5rem', color: '#666'}}>Loading game...</div>
            </div>
        );
    }

    if (error && !gameState) {
        return (
            <div style={{
                minHeight: '100vh',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                backgroundColor: '#f0f2f5',
            }}>
                <div style={{
                    backgroundColor: 'white',
                    padding: '2rem',
                    borderRadius: '12px',
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
                }}>
                    <div style={{color: '#dc3545', fontSize: '1.2rem', marginBottom: '1rem'}}>
                        {error}
                    </div>
                    <button
                        onClick={handleNewGame}
                        style={{
                            padding: '0.75rem 1.5rem',
                            backgroundColor: '#007bff',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '1rem',
                        }}
                    >
                        Start New Game
                    </button>
                </div>
            </div>
        );
    }

    if (!gameState) return null;

    const currentPlayerType =
        gameState.current_player === 'X' ? playerXType : playerOType;
    const showAIHint = currentPlayerType === 'human';

    return (
        <div style={{
            minHeight: '100vh',
            backgroundColor: '#f0f2f5',
            padding: '2rem',
        }}>
            <div style={{
                maxWidth: '800px',
                margin: '0 auto',
            }}>
                <h1 style={{
                    textAlign: 'center',
                    color: '#333',
                    marginBottom: '1rem',
                }}>
                    Connect Four
                </h1>

                <div style={{
                    backgroundColor: playerXType === 'human' && playerOType === 'human' ? '#28a745' : '#dc3545',
                    color: 'white',
                    padding: '1rem',
                    borderRadius: '8px',
                    marginBottom: '1rem',
                    textAlign: 'center',
                    fontWeight: 'bold',
                    fontSize: '1.1rem'
                }}>
                    🎮 GAME
                    MODE: {playerXType === 'human' && playerOType === 'human' ? 'PLAYER vs PLAYER' : 'PLAYER vs AI'}
                    <br/>
                    <span style={{fontSize: '0.9rem'}}>
                        Player X: {playerXType} | Player O: {playerOType}
                    </span>
                    {currentPlayerRole && (
                        <>
                            <br/>
                            <span style={{fontSize: '0.95rem', marginTop: '0.5rem', display: 'inline-block'}}>
                                You are: {currentPlayerRole === 'X' ? '🔴 Player X (Red)' : '🟡 Player O (Yellow)'}
                            </span>
                        </>
                    )}
                </div>

                {isPvPMode && currentPlayerRole && gameState && !gameState.game_over && (
                    <div style={{
                        textAlign: 'center',
                        padding: '0.75rem',
                        backgroundColor: gameState.current_player === currentPlayerRole ? '#d4edda' : '#fff3cd',
                        color: gameState.current_player === currentPlayerRole ? '#155724' : '#856404',
                        borderRadius: '8px',
                        marginBottom: '1rem',
                        fontWeight: 'bold',
                        border: `2px solid ${gameState.current_player === currentPlayerRole ? '#28a745' : '#ffc107'}`
                    }}>
                        {gameState.current_player === currentPlayerRole
                            ? '✅ YOUR TURN - Click a column to play!'
                            : `⏳ Waiting for ${gameState.current_player === 'X' ? playerXName : playerOName} to play...`
                        }
                    </div>
                )}

                <ScoreBoard
                    scoreX={gameState.score_x}
                    scoreO={gameState.score_o}
                    currentPlayer={gameState.current_player}
                    currentTurn={gameState.current_turn}
                    playerXName={playerXName}
                    playerOName={playerOName}
                />

                {aiHint && (
                    <div style={{
                        textAlign: 'center',
                        padding: '1rem',
                        backgroundColor: '#d4edda',
                        borderRadius: '8px',
                        marginBottom: '1rem',
                        border: '2px solid',
                        color: 'black',
                    }}>
                        AI suggests column {aiHint.column} (Confidence: {(aiHint.confidence * 100).toFixed(0)}%)
                    </div>
                )}

                {error && (
                    <div style={{
                        textAlign: 'center',
                        padding: '1rem',
                        backgroundColor: '#f8d7da',
                        borderRadius: '8px',
                        marginBottom: '1rem',
                        border: '2px solid #dc3545',
                        color: '#721c24',
                    }}>
                        {error}
                    </div>
                )}

                <div style={{
                    backgroundColor: '#0066CC',
                    padding: '1.5rem',
                    borderRadius: '12px',
                    display: 'flex',
                    justifyContent: 'center',
                    boxShadow: '0 8px 16px rgba(0,0,0,0.2)',
                }}>
                    <div>
                        {gameState.board.map((row, rowIndex) => (
                            <div key={rowIndex} style={{display: 'flex'}}>
                                {row.map((cell, colIndex) => (
                                    <Cell
                                        key={`${rowIndex}-${colIndex}`}
                                        value={cell}
                                        onClick={() => handleColumnClick(colIndex)}
                                        isValidMove={gameState.valid_moves.includes(colIndex)}
                                        currentPlayer={gameState.current_player}
                                        rowIndex={rowIndex}
                                    />
                                ))}
                            </div>
                        ))}
                    </div>
                </div>

                <GameControls
                    onNewGame={handleNewGame}
                    onGetAIHint={showAIHint ? handleGetAIHint : undefined}
                    gameOver={gameState.game_over}
                    showAIHint={showAIHint}
                />

                {gameState.game_over && (
                    <WinnerDisplay
                        winner={gameState.winner || null}
                        scoreX={gameState.score_x}
                        scoreO={gameState.score_o}
                        onPlayAgain={handleNewGame}
                        onBackToPlatform={token ? handleBackToPlatform : undefined}
                        playerXName={playerXName}
                        playerOName={playerOName}
                    />
                )}
            </div>
        </div>
    );
}
