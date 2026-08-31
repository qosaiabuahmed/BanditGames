import {useEffect, useState} from 'react';
import type {Cell as CellType} from '../type/game';

interface CellProps {
    value: CellType;
    onClick: () => void;
    isValidMove: boolean;
    currentPlayer: 'X' | 'O';
    rowIndex: number;
}

export default function Cell({value, onClick, isValidMove, currentPlayer, rowIndex}: CellProps) {
    const [prevValue, setPrevValue] = useState<CellType>(value);
    const [isAnimating, setIsAnimating] = useState(false);

    useEffect(() => {
        if (value !== '.' && prevValue === '.') {
            console.log(`[CELL] Animation triggered for piece '${value}' at row ${rowIndex}`);
            setIsAnimating(true);
            const timer = setTimeout(() => setIsAnimating(false), 1500);
            return () => clearTimeout(timer);
        }
        setPrevValue(value);
    }, [value]);

    const getCellColor = () => {
        if (value === 'X') return '#FF4444';
        if (value === 'O') return '#FFD700';
        return '#f8f9fa';
    };

    const getHoverColor = () => {
        if (!isValidMove || value !== '.') return getCellColor();
        return currentPlayer === 'X' ? '#FF888850' : '#FFD70050';
    };

    // Calculate animation distance - piece falls from top through each row
    const cellHeight = 68; // 60px + 8px margin
    const fallDistance = (rowIndex + 1) * cellHeight;

    return (
        <div
            onClick={isValidMove && value === '.' ? onClick : undefined}
            style={{
                width: '60px',
                height: '60px',
                margin: '4px',
                borderRadius: '50%',
                backgroundColor: getCellColor(),
                border: '3px solid #0066CC',
                cursor: isValidMove && value === '.' ? 'pointer' : 'default',
                transition: 'background-color 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.8rem',
                fontWeight: 'bold',
                color: value === 'X' ? '#fff' : '#333',
                boxShadow: value !== '.' ? '0 2px 4px rgba(0,0,0,0.2)' : 'inset 0 2px 4px rgba(0,0,0,0.1)',
                animation: isAnimating ? `dropThrough-${rowIndex} 1.2s cubic-bezier(0.5, 0, 0.5, 1)` : 'none',
            }}
            onMouseEnter={(e) => {
                if (isValidMove && value === '.') {
                    e.currentTarget.style.backgroundColor = getHoverColor();
                }
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = getCellColor();
            }}
        >
            {value !== '.' && value}
            <style>{`
                @keyframes dropThrough-${rowIndex} {
                    0% {
                        transform: translateY(-${fallDistance}px);
                    }
                    ${rowIndex > 0 ? `
                    ${(70 / (rowIndex + 1))}% {
                        transform: translateY(-${fallDistance * 0.5}px);
                    }
                    ` : ''}
                    85% {
                        transform: translateY(0);
                    }
                    92% {
                        transform: translateY(-12px);
                    }
                    96% {
                        transform: translateY(0);
                    }
                    98% {
                        transform: translateY(-5px);
                    }
                    100% {
                        transform: translateY(0);
                    }
                }
            `}</style>
        </div>
    );
}
