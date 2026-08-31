import React from 'react';
import Cell from './Cell';

interface BoardProps {
  board: string[][];
  onCellClick: (row: number, col: number) => void;
  disabled: boolean;
}

const Board: React.FC<BoardProps> = ({ board, onCellClick, disabled }) => {
  return (
    <div className="board">
      {board.map((row, rowIndex) => (
        <div key={rowIndex} className="board-row">
          {row.map((cell, colIndex) => (
            <Cell
              key={`${rowIndex}-${colIndex}`}
              value={cell}
              onClick={() => onCellClick(rowIndex, colIndex)}
              disabled={disabled}
            />
          ))}
        </div>
      ))}
    </div>
  );
};

export default Board;
