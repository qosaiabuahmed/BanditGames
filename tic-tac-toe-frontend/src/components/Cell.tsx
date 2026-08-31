import React from 'react';

interface CellProps {
  value: string;
  onClick: () => void;
  disabled: boolean;
}

const Cell: React.FC<CellProps> = ({ value, onClick, disabled }) => {
  const displayValue = value === '-' ? '' : value.toUpperCase();

  return (
    <button
      className={`cell ${disabled ? 'disabled' : ''} ${value !== '-' ? 'filled' : ''}`}
      onClick={onClick}
      disabled={disabled || value !== '-'}
    >
      <span className={`cell-value ${value !== '-' ? `player-${value}` : ''}`}>
        {displayValue}
      </span>
    </button>
  );
};

export default Cell;
