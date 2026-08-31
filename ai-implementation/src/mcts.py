"""
Monte Carlo Tree Search (MCTS) Implementation for Connect Four

This implements the standard MCTS algorithm with:
- UCB1 for selection
- Random rollouts for simulation
- Win rate tracking for backpropagation
"""

import math
import random
from typing import Optional, List, Dict
from src.base_game import BaseGameSimulator, Player


class MCTSNode:
    """Node in the MCTS tree."""

    def __init__(
        self,
        game_state: BaseGameSimulator,
        parent: Optional['MCTSNode'] = None,
        move: Optional[int] = None
    ):
        """
        Initialize MCTS node.

        Args:
            game_state: Current game state at this node
            parent: Parent node (None for root)
            move: Move that led to this node (None for root)
        """
        self.game_state = game_state
        self.parent = parent
        self.move = move  # The column that was played to reach this node

        # MCTS statistics
        self.visits = 0
        self.wins = 0  # Wins from perspective of parent's player

        # Children
        self.children: Dict[int, 'MCTSNode'] = {}  # move -> child node
        self.untried_moves: List[int] = game_state.get_valid_moves()

    def is_fully_expanded(self) -> bool:
        """Check if all children have been expanded."""
        return len(self.untried_moves) == 0

    def is_terminal(self) -> bool:
        """Check if this is a terminal game state."""
        return self.game_state.is_game_over()

    def ucb1_score(self, exploration_constant: float = 1.41) -> float:
        """
        Calculate UCB1 score for this node.

        UCB1 = win_rate + exploration_constant * sqrt(ln(parent_visits) / visits)

        Args:
            exploration_constant: Balance exploration vs exploitation (√2 ≈ 1.41 is standard)

        Returns:
            UCB1 score
        """
        if self.visits == 0:
            return float('inf')  # Prioritize unvisited nodes

        win_rate = self.wins / self.visits
        exploration = exploration_constant * math.sqrt(
            math.log(self.parent.visits) / self.visits
        )

        return win_rate + exploration

    def best_child(self, exploration_constant: float = 1.41) -> 'MCTSNode':
        """
        Select best child using UCB1.

        Args:
            exploration_constant: Exploration parameter

        Returns:
            Child node with highest UCB1 score
        """
        return max(
            self.children.values(),
            key=lambda child: child.ucb1_score(exploration_constant)
        )

    def expand(self) -> 'MCTSNode':
        """
        Expand by creating a new child node.

        Returns:
            Newly created child node
        """
        # Pick a random untried move
        move = random.choice(self.untried_moves)
        self.untried_moves.remove(move)

        # Create new game state with this move
        new_state = self.game_state.copy()
        new_state.make_move(move)

        # Create child node
        child_node = MCTSNode(
            game_state=new_state,
            parent=self,
            move=move
        )

        self.children[move] = child_node
        return child_node

    def simulate(self) -> Player:
        """
        Run a fast simulation (playout) from this node to game end.
        Uses game-specific heuristic if available (e.g., center preference).

        Returns:
            Winner of the simulation
        """
        # Create a copy for simulation
        sim_state = self.game_state.copy()

        # Play moves until game ends
        while not sim_state.is_game_over():
            valid_moves = sim_state.get_valid_moves()
            if not valid_moves:
                break

            # Use game-specific heuristic if available (50% of the time)
            if len(valid_moves) > 1 and random.random() < 0.5:
                preferred_move = sim_state.get_preferred_move(valid_moves)
                if preferred_move is not None:
                    move = preferred_move
                else:
                    move = random.choice(valid_moves)
            else:
                move = random.choice(valid_moves)

            sim_state.make_move(move)

        # Return winner
        return sim_state.get_winner()

    def backpropagate(self, winner: Optional[Player]):
        """
        Backpropagate simulation result up the tree.

        Args:
            winner: Winner of the simulation (None for tie)
        """
        node = self

        while node is not None:
            node.visits += 1

            # Award win if this node's parent's player won
            # (The parent made the move to reach this node)
            if node.parent is not None:
                # Get the player who made the move to reach this node
                player_who_moved = node.game_state.current_player.opponent()

                if winner == player_who_moved:
                    node.wins += 1
                elif winner is None:  # Tie
                    node.wins += 0.5

            node = node.parent

    def most_visited_child(self) -> Optional['MCTSNode']:
        """
        Get the child with most visits (exploitation, no exploration).

        Returns:
            Most visited child node, or None if no children
        """
        if not self.children:
            return None

        return max(
            self.children.values(),
            key=lambda child: child.visits
        )


class MCTS:
    """Monte Carlo Tree Search agent."""

    def __init__(self, exploration_constant: float = 1.41, max_iterations: int = 1000):
        """
        Initialize MCTS agent.

        Args:
            exploration_constant: UCB1 exploration parameter (√2 is standard)
            max_iterations: Maximum number of MCTS iterations
        """
        self.exploration_constant = exploration_constant
        self.max_iterations = max_iterations

        # Statistics for logging
        self.nodes_explored = 0

        # Cache the last root node to avoid redundant searches
        self.last_root: Optional[MCTSNode] = None

    def search(self, game_state: BaseGameSimulator) -> int:
        """
        Run MCTS search and return best move.

        Args:
            game_state: Current game state

        Returns:
            Best move index (0-indexed)
        """
        # Create root node
        root = MCTSNode(game_state)

        # Run MCTS iterations
        for _ in range(self.max_iterations):
            # 1. SELECTION - traverse tree using UCB1
            node = self._select(root)

            # 2. EXPANSION - add a new child
            if not node.is_terminal() and not node.is_fully_expanded():
                node = node.expand()

            # 3. SIMULATION - random playout
            winner = node.simulate()

            # 4. BACKPROPAGATION - update statistics
            node.backpropagate(winner)

            self.nodes_explored += 1

        # Store root for potential reuse in get_move_statistics()
        self.last_root = root

        # Return move with most visits
        best_child = root.most_visited_child()

        if best_child is None:
            # Fallback: return random valid move
            valid_moves = game_state.get_valid_moves()
            return random.choice(valid_moves) if valid_moves else 0

        return best_child.move

    def _select(self, node: MCTSNode) -> MCTSNode:
        """
        Selection phase: traverse tree using UCB1.

        Args:
            node: Current node

        Returns:
            Selected node for expansion
        """
        while not node.is_terminal():
            if not node.is_fully_expanded():
                # Return this node for expansion
                return node
            else:
                # Select best child using UCB1
                node = node.best_child(self.exploration_constant)

        return node

    def get_move_statistics(self, game_state: BaseGameSimulator) -> Dict[int, Dict]:
        """
        Get detailed statistics for each possible move (for analysis).

        Args:
            game_state: Current game state

        Returns:
            Dictionary mapping moves to their statistics
        """
        # Reuse cached root if available, otherwise run a new search
        if self.last_root is not None:
            root = self.last_root
        else:
            root = MCTSNode(game_state)

            # Run search only if we don't have a cached root
            for _ in range(self.max_iterations):
                node = self._select(root)
                if not node.is_terminal() and not node.is_fully_expanded():
                    node = node.expand()
                winner = node.simulate()
                node.backpropagate(winner)

        # Collect statistics
        stats = {}
        for move, child in root.children.items():
            win_rate = child.wins / child.visits if child.visits > 0 else 0
            stats[move] = {
                'visits': child.visits,
                'wins': child.wins,
                'win_rate': win_rate,
                'confidence': child.visits / self.max_iterations
            }

        return stats