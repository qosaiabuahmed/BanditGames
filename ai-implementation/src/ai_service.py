"""
AI Service Layer

Handles AI move calculation logic, separating business logic from API layer.
Follows Single Responsibility Principle.
"""

import time
import random
import logging
from typing import Dict, Any, Optional

from src.base_game import BaseGameSimulator
from src.config import AIConfig, Difficulty
from src.mcts import MCTS

logger = logging.getLogger(__name__)


class MoveResult:
    """Data class for move calculation results."""

    def __init__(
        self,
        move: int,
        confidence: float,
        nodes_explored: int,
        computation_time_ms: int,
        game_type: str
    ):
        self.move = move
        self.confidence = confidence
        self.nodes_explored = nodes_explored
        self.computation_time_ms = computation_time_ms
        self.game_type = game_type

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for API response."""
        result = {
            'game_type': self.game_type,
            'move': self.move,
            'confidence': self.confidence,
            'nodes_explored': self.nodes_explored,
            'computation_time_ms': self.computation_time_ms
        }

        # Add 'column' field for backward compatibility with Connect4
        if self.game_type == 'connect4':
            result['column'] = self.move

        return result


class AIService:
    """
    Service for calculating AI moves.

    Responsibilities:
    - Orchestrate MCTS search
    - Apply tactical move detection (hard difficulty)
    - Apply mistake injection (easy/medium difficulty)
    - Calculate move statistics
    """

    @staticmethod
    def calculate_move(game_state: BaseGameSimulator, difficulty: int) -> MoveResult:
        """
        Calculate best move for given game state and difficulty.

        Args:
            game_state: Current game state
            difficulty: Difficulty level (Difficulty.EASY, MEDIUM, or HARD)

        Returns:
            MoveResult with move and statistics
        """
        start_time = time.time()
        game_type = game_state.get_game_type()

        # Get difficulty profile
        profile = AIConfig.get_profile(game_type, difficulty)

        logger.info(
            f"Calculating move for {game_type} - difficulty {difficulty} "
            f"(iterations: {profile['max_iterations']})"
        )

        # Get valid moves for validation
        valid_moves = game_state.get_valid_moves()
        logger.info(f"Valid moves: {valid_moves}")

        # HARD DIFFICULTY: Check for tactical moves first
        if difficulty == Difficulty.HARD:
            tactical_result = AIService._check_tactical_moves(
                game_state,
                game_type,
                start_time
            )
            if tactical_result is not None:
                return tactical_result

        # Run MCTS search
        mcts_result = AIService._run_mcts_search(
            game_state,
            profile,
            valid_moves,
            difficulty,
            start_time
        )

        return mcts_result

    @staticmethod
    def _check_tactical_moves(
        game_state: BaseGameSimulator,
        game_type: str,
        start_time: float
    ) -> Optional[MoveResult]:
        """
        Check for immediate tactical moves (winning/blocking).

        Returns:
            MoveResult if tactical move found, None otherwise
        """
        # 1. Check for immediate winning move (highest priority)
        winning_move = game_state.find_winning_move()
        if winning_move is not None:
            computation_time_ms = int((time.time() - start_time) * 1000)
            logger.info(
                f"[HARD - {game_type}] Found winning move: {winning_move} "
                f"(time: {computation_time_ms}ms)"
            )
            return MoveResult(
                move=winning_move,
                confidence=1.0,
                nodes_explored=0,
                computation_time_ms=computation_time_ms,
                game_type=game_type
            )

        # 2. Check for blocking move (opponent threat)
        blocking_move = game_state.find_blocking_move()
        if blocking_move is not None:
            computation_time_ms = int((time.time() - start_time) * 1000)
            logger.info(
                f"[HARD - {game_type}] Blocking threat at move {blocking_move} "
                f"(time: {computation_time_ms}ms)"
            )
            return MoveResult(
                move=blocking_move,
                confidence=1.0,
                nodes_explored=0,
                computation_time_ms=computation_time_ms,
                game_type=game_type
            )

        # 3. Check for immediate high-scoring move (Connect4 only)
        if game_type == 'connect4' and hasattr(game_state, 'find_best_immediate_move'):
            immediate_move = game_state.find_best_immediate_move()
            if immediate_move is not None:
                col, score = immediate_move
                if score >= 2:  # If we can score 2+ points immediately
                    computation_time_ms = int((time.time() - start_time) * 1000)
                    logger.info(
                        f"[HARD - {game_type}] Immediate scoring move: {col} "
                        f"(scores {score} points, time: {computation_time_ms}ms)"
                    )
                    return MoveResult(
                        move=col,
                        confidence=1.0,
                        nodes_explored=0,
                        computation_time_ms=computation_time_ms,
                        game_type=game_type
                    )

        return None

    @staticmethod
    def _run_mcts_search(
        game_state: BaseGameSimulator,
        profile: Dict[str, Any],
        valid_moves: list,
        difficulty: int,
        start_time: float
    ) -> MoveResult:
        """
        Run MCTS search and apply difficulty-based modifications.

        Args:
            game_state: Current game state
            profile: Difficulty profile with MCTS parameters
            valid_moves: List of valid moves
            difficulty: Difficulty level
            start_time: Start time for timing

        Returns:
            MoveResult with best move
        """
        game_type = game_state.get_game_type()

        # Create MCTS instance
        mcts = MCTS(
            exploration_constant=profile['exploration_constant'],
            max_iterations=profile['max_iterations']
        )

        # Get best move from MCTS
        best_move = mcts.search(game_state)

        # Validate move
        if best_move not in valid_moves:
            logger.error(f"MCTS returned invalid move {best_move}! Valid: {valid_moves}")
            best_move = valid_moves[0] if valid_moves else 0

        # Get move statistics
        stats = mcts.get_move_statistics(game_state)
        move_confidence = stats.get(best_move, {}).get('win_rate', 0.5)

        # Apply mistake probability (easy/medium difficulties)
        mistake_probability = profile.get('mistake_probability', 0.0)
        if mistake_probability > 0 and random.random() < mistake_probability:
            best_move, move_confidence = AIService._apply_mistake(
                best_move,
                valid_moves,
                stats,
                difficulty
            )

        # Calculate timing
        computation_time_ms = int((time.time() - start_time) * 1000)

        logger.info(
            f"[{game_type.upper()}] Move: {best_move}, "
            f"Difficulty: {difficulty}, "
            f"Confidence: {move_confidence:.2%}, "
            f"Nodes: {mcts.nodes_explored}, "
            f"Time: {computation_time_ms}ms"
        )

        return MoveResult(
            move=best_move,
            confidence=move_confidence,
            nodes_explored=mcts.nodes_explored,
            computation_time_ms=computation_time_ms,
            game_type=game_type
        )

    @staticmethod
    def _apply_mistake(
        best_move: int,
        valid_moves: list,
        stats: Dict[int, Dict],
        difficulty: int
    ) -> tuple:
        """
        Apply intentional mistake for easier difficulties.

        Args:
            best_move: Original best move
            valid_moves: List of valid moves
            stats: Move statistics from MCTS
            difficulty: Difficulty level

        Returns:
            (mistake_move, confidence) tuple
        """
        other_moves = [m for m in valid_moves if m != best_move]
        if other_moves:
            mistake_move = random.choice(other_moves)
            logger.info(
                f"[DIFFICULTY {difficulty}] Making intentional mistake! "
                f"Best move: {best_move}, Choosing: {mistake_move}"
            )
            move_confidence = stats.get(mistake_move, {}).get('win_rate', 0.3)
            return mistake_move, move_confidence

        return best_move, stats.get(best_move, {}).get('win_rate', 0.5)

    @staticmethod
    def analyze_position(game_state: BaseGameSimulator, difficulty: int = Difficulty.MEDIUM) -> Dict[str, Any]:
        """
        Analyze current position and return statistics for all moves.

        Args:
            game_state: Current game state
            difficulty: Difficulty level for analysis (default: MEDIUM)

        Returns:
            Dictionary with move analysis
        """
        game_type = game_state.get_game_type()
        profile = AIConfig.get_profile(game_type, difficulty)

        # Create MCTS instance
        mcts = MCTS(
            exploration_constant=profile['exploration_constant'],
            max_iterations=profile['max_iterations']
        )

        # Get detailed statistics
        stats = mcts.get_move_statistics(game_state)

        # Find best move (most visits)
        best_move = max(stats.items(), key=lambda x: x[1]['visits'])[0]

        # Format response
        return {
            'game_type': game_type,
            'best_move': best_move,
            'move_analysis': {
                str(move): {
                    'visits': info['visits'],
                    'wins': info['wins'],
                    'win_rate': info['win_rate'],
                    'confidence': info['confidence']
                }
                for move, info in stats.items()
            },
            'total_simulations': mcts.max_iterations
        }
