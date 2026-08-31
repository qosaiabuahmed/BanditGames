#!/usr/bin/env python3
"""
Generate and log a comprehensive comparative analysis of player types to W&B.

This script queries the database to compare different aspects of player performance:
1. Overall win/loss/draw rates.
2. Average move speed.
3. Opening move preferences.
4. Unforced Error Rate (failure to make a winning move).

Usage:
    - Ensure you are logged into W&B: `wandb login`
    - Run the script: `python reports/generate_comparison_report.py`
"""

import os
import sys
from pathlib import Path
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('Agg')  # Non-interactive backend
import matplotlib.pyplot as plt
import seaborn as sns
import wandb
from typing import List, Optional, Tuple

# Add src to path to import GameplayLogger and game logic
sys.path.append(str(Path(__file__).parent.parent / 'src'))
from gameplay_logger import GameplayLogger
from connect_four import Board, Player, ScoreCalculator, GameConfig

# --- Data Fetching ---

def get_game_outcomes(logger: GameplayLogger) -> pd.DataFrame:
    """Queries all completed games with player type info."""
    print("Querying database for game outcomes...")
    query = "SELECT g.game_id, g.winner, p_x.player_type as player_x_type, p_o.player_type as player_o_type FROM games g JOIN players p_x ON g.player_x_id = p_x.player_id JOIN players p_o ON g.player_o_id = p_o.player_id WHERE g.game_status = 'completed';"
    with logger.get_connection() as conn:
        return pd.read_sql_query(query, conn)

def get_move_data(logger: GameplayLogger) -> pd.DataFrame:
    """Queries all moves with necessary metadata for all analyses."""
    print("Querying database for all move data...")
    query = """
        SELECT m.game_id, m.move_number, m.move_timestamp, m.column_played, m.board_state_before, p.player_type
        FROM moves m
        JOIN players p ON m.player_id = p.player_id
        ORDER BY m.game_id, m.move_number;
    """
    with logger.get_connection() as conn:
        return pd.read_sql_query(query, conn)

# --- Analysis Functions ---

def analyze_win_rates(df: pd.DataFrame) -> pd.DataFrame:
    print("Analyzing win/loss/draw statistics...")
    # (Implementation from previous successful run)
    stats = {}
    if df.empty: return pd.DataFrame()
    for _, row in df.iterrows():
        for p_type in [row.get('player_x_type'), row.get('player_o_type')]:
            if p_type and p_type not in stats: stats[p_type] = {'wins': 0, 'losses': 0, 'draws': 0, 'games': 0}
        p_x_type, p_o_type = row.get('player_x_type'), row.get('player_o_type')
        if p_x_type: stats[p_x_type]['games'] += 1
        if p_o_type: stats[p_o_type]['games'] += 1
        winner = row.get('winner')
        if winner == 'X':
            if p_x_type: stats[p_x_type]['wins'] += 1
            if p_o_type: stats[p_o_type]['losses'] += 1
        elif winner == 'O':
            if p_o_type: stats[p_o_type]['wins'] += 1
            if p_x_type: stats[p_x_type]['losses'] += 1
        elif winner == 'Tie':
            if p_x_type: stats[p_x_type]['draws'] += 1
            if p_o_type: stats[p_o_type]['draws'] += 1
    stats_df = pd.DataFrame.from_dict(stats, orient='index')
    if stats_df.empty: return stats_df
    stats_df['win_rate'] = (stats_df['wins'] / stats_df['games']) * 100
    stats_df.reset_index(inplace=True); return stats_df.rename(columns={'index': 'player_type'})

def analyze_speed(moves_df: pd.DataFrame) -> pd.DataFrame:
    print("Analyzing move speed...")
    # (Implementation from previous successful run)
    if moves_df.empty: return pd.DataFrame()
    moves_df['time_delta'] = moves_df.groupby('game_id')['move_timestamp'].diff().dt.total_seconds()
    speed_stats = moves_df[(moves_df['time_delta'] > 0) & (moves_df['time_delta'] < 60)].groupby('player_type')['time_delta'].mean().reset_index()
    return speed_stats.rename(columns={'time_delta': 'avg_move_time_sec'})

def analyze_opening_moves(moves_df: pd.DataFrame) -> pd.DataFrame:
    print("Analyzing opening move preferences...")
    # (Implementation from previous successful run)
    if moves_df.empty: return pd.DataFrame()
    openings = moves_df[moves_df['move_number'] == 1].copy()
    dist = openings.groupby(['player_type', 'column_played']).size().reset_index(name='count')
    total_openings = dist.groupby('player_type')['count'].transform('sum')
    dist['percentage'] = (dist['count'] / total_openings) * 100
    return dist

def check_for_winning_move(board_state: List[List[str]], player: Player) -> Optional[int]:
    """Checks if any column is an immediate winning move for the player."""
    config = GameConfig()
    for col in range(config.cols):
        temp_board = Board(config.rows, config.cols)
        # Manually construct board from list of lists
        temp_board.grid = [[Player(p) for p in r] for r in board_state]
        # Recalculate column positions based on grid
        for c in range(config.cols):
            for r in range(config.rows - 1, -1, -1):
                if temp_board.grid[r][c] == Player.NONE:
                    temp_board.column_positions[c] = r
                    break
                else:
                    temp_board.column_positions[c] = -1

        if not temp_board.is_valid_column(col):
            continue

        row, _ = temp_board.place_piece(col, player)
        if row is None: continue # Should not happen with is_valid_column check
        
        calc = ScoreCalculator(temp_board, config.connect_length)
        score_x_inc, score_o_inc = calc.calculate_score_for_move(row, col)
        
        if (player == Player.X and score_x_inc > 0) or (player == Player.O and score_o_inc > 0):
            return col # This is a winning move
    return None

def analyze_unforced_errors(moves_df: pd.DataFrame) -> pd.DataFrame:
    """Analyzes failure to make an available winning move."""
    print("Analyzing unforced errors (missed wins)...")
    if moves_df.empty: return pd.DataFrame()

    error_stats = {}
    opportunities = {}

    for _, row in moves_df.iterrows():
        player_type = row['player_type']
        if player_type not in opportunities: opportunities[player_type] = 0
        if player_type not in error_stats: error_stats[player_type] = 0

        # Determine which player is making the move
        player = Player.X if row['move_number'] % 2 == 0 else Player.O
        
        # Check if a winning move was available *before* this move was made
        winning_move = check_for_winning_move(row['board_state_before'], player)
        
        if winning_move is not None:
            opportunities[player_type] += 1
            # If a winning move existed, but the player chose something else, it's an error
            if row['column_played'] != winning_move:
                error_stats[player_type] += 1

    # Create DataFrame
    error_df = pd.DataFrame.from_dict(error_stats, orient='index', columns=['unforced_errors'])
    opp_df = pd.DataFrame.from_dict(opportunities, orient='index', columns=['winning_opportunities'])
    stats_df = pd.concat([error_df, opp_df], axis=1)
    stats_df = stats_df[stats_df['winning_opportunities'] > 0] # Only show players with opportunities
    stats_df['error_rate'] = (stats_df['unforced_errors'] / stats_df['winning_opportunities']) * 100
    
    stats_df.reset_index(inplace=True); return stats_df.rename(columns={'index': 'player_type'})

# --- W&B Logging ---

def log_to_wandb(run, results: dict):
    """Logs all analysis results to an active W&B run."""
    print("Logging results to Weights & Biases...")
    
    # Simple helper for logging
    def log_chart(df, title, x, y, kind='bar', hue=None):
        if df.empty: return
        fig, ax = plt.subplots(figsize=(12, 7) if hue else (10, 6))
        if kind == 'bar': sns.barplot(data=df, x=x, y=y, hue=hue, ax=ax)
        elif kind == 'line': sns.lineplot(data=df, x=x, y=y, hue=hue, marker='o', ax=ax)
        ax.set_title(title); ax.set_ylabel(y); ax.set_xlabel(x)
        plt.tight_layout(); run.log({title.replace(' ', '_'): wandb.Image(fig)}); plt.close(fig)

    if 'win_rates' in results and not results['win_rates'].empty:
        log_chart(results['win_rates'], 'Overall Win Rate by Player Type', 'player_type', 'win_rate')
        run.log({"Player_Win_Rate_Summary": wandb.Table(dataframe=results['win_rates'])})
        print("✓ Logged win rate analysis.")

    if 'speed' in results and not results['speed'].empty:
        log_chart(results['speed'], 'Average Move Time by Player Type', 'player_type', 'avg_move_time_sec')
        run.log({"Move_Speed_Summary": wandb.Table(dataframe=results['speed'])})
        print("✓ Logged speed analysis.")

    if 'opening_moves' in results and not results['opening_moves'].empty:
        log_chart(results['opening_moves'], 'Opening Move Preferences', 'column_played', 'percentage', hue='player_type')
        print("✓ Logged opening move analysis.")
        
    if 'unforced_errors' in results and not results['unforced_errors'].empty:
        log_chart(results['unforced_errors'], 'Unforced Error Rate (Missed Wins)', 'player_type', 'error_rate')
        run.log({"Unforced_Error_Summary": wandb.Table(dataframe=results['unforced_errors'])})
        print("✓ Logged unforced error analysis.")

def main():
    print("="*60); print("Comprehensive Player Performance Analysis to W&B"); print("="*60)
    try:
        logger = GameplayLogger()
        games_df = get_game_outcomes(logger)
        moves_df = get_move_data(logger)
        logger.close()
    except Exception as e:
        print(f"✗ Could not connect to database. Error: {e}"); sys.exit(1)

    if games_df.empty: print("✗ No game data found. Nothing to analyze."); sys.exit(0)

    analysis_results = {
        'win_rates': analyze_win_rates(games_df),
        'speed': analyze_speed(moves_df),
        'opening_moves': analyze_opening_moves(moves_df),
        'unforced_errors': analyze_unforced_errors(moves_df),
    }
    try:
        run = wandb.init(project=os.getenv("WANDB_PROJECT", "connect-four-analysis"), name="full-player-comparison", job_type="analysis")
        log_to_wandb(run, analysis_results)
        run.finish()
        print(f"\n✓ W&B run finished. View results at: {run.url}")
    except Exception as e:
        print(f"\n✗ Failed to log to W&B. Error: {e}"); sys.exit(1)
    print("\n" + "="*60); print("✓ Comprehensive analysis complete."); print("="*60)

if __name__ == "__main__":
    main()
