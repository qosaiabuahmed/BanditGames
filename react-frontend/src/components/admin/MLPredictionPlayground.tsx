import { useState } from 'react';

const ML_SERVICE_URL = 'http://localhost:8000';

const EMPTY_BOARD = [
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
];

const SAMPLE_BOARD = [
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'E', 'E', 'E', 'E'],
  ['E', 'E', 'E', 'X', 'E', 'E', 'E'],
  ['E', 'E', 'O', 'X', 'E', 'E', 'E'],
  ['E', 'E', 'X', 'O', 'E', 'E', 'E'],
];

interface MovePredictionResult {
  column: number;
  probability: number;
  all_probabilities: number[];
}

interface WinProbabilityResult {
  value: number;
  win_percentage: number;
  evaluation: string;
}

export default function MLPredictionPlayground() {
  const [board, setBoard] = useState<string[][]>(SAMPLE_BOARD);
  const [modelType, setModelType] = useState<'cnn' | 'lr'>('cnn');
  const [predictionType, setPredictionType] = useState<'move' | 'win'>('move');
  const [loading, setLoading] = useState(false);
  const [moveResult, setMoveResult] = useState<MovePredictionResult | null>(null);
  const [winResult, setWinResult] = useState<WinProbabilityResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleCellClick = (row: number, col: number) => {
    const newBoard = board.map(r => [...r]);
    // Cycle through E -> X -> O -> E
    if (newBoard[row][col] === 'E') newBoard[row][col] = 'X';
    else if (newBoard[row][col] === 'X') newBoard[row][col] = 'O';
    else newBoard[row][col] = 'E';
    setBoard(newBoard);
  };

  const handlePredict = async () => {
    setLoading(true);
    setError(null);
    setMoveResult(null);
    setWinResult(null);

    try {
      if (predictionType === 'move') {
        const response = await fetch(
          `${ML_SERVICE_URL}/api/predict/move?model_type=${modelType}`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              board,
              legal_moves: [0, 1, 2, 3, 4, 5, 6],
            }),
          }
        );

        if (!response.ok) throw new Error('Failed to get move prediction');
        const data: MovePredictionResult = await response.json();
        setMoveResult(data);
      } else {
        const response = await fetch(
          `${ML_SERVICE_URL}/api/predict/win-probability?model_type=${modelType}`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ board }),
          }
        );

        if (!response.ok) throw new Error('Failed to get win probability');
        const data: WinProbabilityResult = await response.json();
        setWinResult(data);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Prediction failed');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = () => {
    const data = {
      board,
      modelType,
      predictionType,
      timestamp: new Date().toISOString(),
      result: predictionType === 'move' ? moveResult : winResult,
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ml-prediction-${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleReset = () => {
    setBoard(EMPTY_BOARD);
    setMoveResult(null);
    setWinResult(null);
    setError(null);
  };

  const handleLoadSample = () => {
    setBoard(SAMPLE_BOARD);
    setMoveResult(null);
    setWinResult(null);
    setError(null);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
      <h3 className="text-2xl font-bold text-slate-100 mb-6">🧪 ML Prediction Playground</h3>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Board Input */}
        <div>
          <div className="mb-4">
            <label className="block text-sm font-medium text-slate-400 mb-2">
              Board State (Click to edit: E → X → O)
            </label>
            <div className="inline-block bg-slate-800 p-4 rounded-lg">
              {board.map((row, rowIdx) => (
                <div key={rowIdx} className="flex gap-1">
                  {row.map((cell, colIdx) => (
                    <button
                      key={colIdx}
                      onClick={() => handleCellClick(rowIdx, colIdx)}
                      className={`w-12 h-12 rounded border-2 font-bold text-lg transition-colors ${
                        cell === 'X'
                          ? 'bg-red-900 border-red-600 text-red-100'
                          : cell === 'O'
                          ? 'bg-yellow-900 border-yellow-600 text-yellow-100'
                          : 'bg-slate-700 border-slate-600 text-slate-500'
                      } hover:border-purple-500`}
                    >
                      {cell === 'E' ? '' : cell}
                    </button>
                  ))}
                </div>
              ))}
            </div>
          </div>

          <div className="flex gap-2">
            <button
              onClick={handleLoadSample}
              className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-100 rounded-lg transition-colors text-sm"
            >
              Load Sample
            </button>
            <button
              onClick={handleReset}
              className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-100 rounded-lg transition-colors text-sm"
            >
              Clear Board
            </button>
          </div>
        </div>

        {/* Right: Controls & Results */}
        <div>
          <div className="space-y-4 mb-6">
            {/* Model Type */}
            <div>
              <label className="block text-sm font-medium text-slate-400 mb-2">
                Model Type
              </label>
              <div className="flex gap-3">
                <button
                  onClick={() => setModelType('cnn')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    modelType === 'cnn'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  CNN
                </button>
                <button
                  onClick={() => setModelType('lr')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    modelType === 'lr'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  Linear Regression
                </button>
              </div>
            </div>

            {/* Prediction Type */}
            <div>
              <label className="block text-sm font-medium text-slate-400 mb-2">
                Prediction Type
              </label>
              <div className="flex gap-3">
                <button
                  onClick={() => setPredictionType('move')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    predictionType === 'move'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  Move Prediction
                </button>
                <button
                  onClick={() => setPredictionType('win')}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    predictionType === 'win'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  Win Probability
                </button>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3">
              <button
                onClick={handlePredict}
                disabled={loading}
                className="flex-1 px-6 py-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 disabled:from-slate-700 disabled:to-slate-700 text-white rounded-lg font-semibold transition-all disabled:cursor-not-allowed"
              >
                {loading ? 'Predicting...' : 'Get Prediction'}
              </button>
              {(moveResult || winResult) && (
                <button
                  onClick={handleExport}
                  className="px-6 py-3 bg-slate-700 hover:bg-slate-600 text-white rounded-lg font-semibold transition-colors"
                >
                  Export
                </button>
              )}
            </div>
          </div>

          {/* Results */}
          {error && (
            <div className="bg-red-900/20 border border-red-600/50 rounded-lg p-4">
              <p className="text-red-400 text-sm">{error}</p>
            </div>
          )}

          {moveResult && (
            <div className="bg-slate-800 border border-purple-600/50 rounded-lg p-4">
              <h4 className="text-lg font-semibold text-purple-400 mb-3">
                Move Prediction Result
              </h4>
              <div className="space-y-3">
                <div>
                  <span className="text-slate-400 text-sm">Best Move:</span>
                  <p className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400">
                    Column {moveResult.column}
                  </p>
                </div>
                <div>
                  <span className="text-slate-400 text-sm">Confidence:</span>
                  <p className="text-2xl font-bold text-green-400">
                    {(moveResult.probability * 100).toFixed(1)}%
                  </p>
                </div>
                <div>
                  <span className="text-slate-400 text-sm block mb-2">
                    All Column Probabilities:
                  </span>
                  <div className="flex gap-1">
                    {moveResult.all_probabilities.map((prob, idx) => (
                      <div key={idx} className="flex-1 text-center">
                        <div className="text-xs text-slate-500 mb-1">{idx}</div>
                        <div className="h-20 bg-slate-700 rounded relative overflow-hidden">
                          <div
                            className={`absolute bottom-0 w-full transition-all ${
                              idx === moveResult.column
                                ? 'bg-gradient-to-t from-purple-500 to-pink-500'
                                : 'bg-slate-600'
                            }`}
                            style={{ height: `${prob * 100}%` }}
                          ></div>
                        </div>
                        <div className="text-xs text-slate-400 mt-1">
                          {(prob * 100).toFixed(0)}%
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {winResult && (
            <div className="bg-slate-800 border border-purple-600/50 rounded-lg p-4">
              <h4 className="text-lg font-semibold text-purple-400 mb-3">
                Win Probability Result
              </h4>
              <div className="space-y-3">
                <div>
                  <span className="text-slate-400 text-sm">Win Percentage:</span>
                  <p className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400">
                    {winResult.win_percentage.toFixed(1)}%
                  </p>
                </div>
                <div>
                  <span className="text-slate-400 text-sm">Evaluation:</span>
                  <p className="text-xl font-semibold text-green-400">
                    {winResult.evaluation}
                  </p>
                </div>
                <div>
                  <span className="text-slate-400 text-sm">Raw Value:</span>
                  <p className="text-lg text-slate-300">
                    {winResult.value.toFixed(4)}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
