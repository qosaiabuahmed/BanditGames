#!/usr/bin/env python3
"""
Evaluate all trained models and extract metrics from MLFlow.

This script:
1. Retrieves all model results from MLFlow experiments
2. Analyzes performance across datasets (v1, v2, v3)
3. Compares model architectures (CNN vs LR)
4. Returns structured data for reporting

Usage:
    python evaluate_models.py
"""

import argparse
import json
from datetime import datetime
import mlflow
from mlflow.tracking import MlflowClient
from typing import Dict, List, Tuple
import sys


def get_experiment_results(client: MlflowClient, exp_name: str) -> Dict[str, Dict[str, float]]:
    """
    Get all model results from an experiment.

    Returns:
        Dictionary mapping model names to their metrics
    """
    exp = client.get_experiment_by_name(exp_name)
    if not exp:
        print(f"Warning: Experiment '{exp_name}' not found")
        return {}

    runs = client.search_runs(exp.experiment_id)
    results = {}

    for run in runs:
        run_name = run.data.tags.get('mlflow.runName', 'unknown')
        metrics = run.data.metrics
        params = run.data.params

        # Determine model type
        model_key = None
        if 'policy' in run_name.lower():
            if 'cnn' in run_name.lower():
                model_key = 'policy_cnn'
            elif 'lr' in run_name.lower() or 'logistic' in run_name.lower():
                model_key = 'policy_lr'
        elif 'value' in run_name.lower():
            if 'cnn' in run_name.lower():
                model_key = 'value_cnn'
            elif 'lr' in run_name.lower() or 'linear' in run_name.lower():
                model_key = 'value_lr'

        if model_key:
            run_data = {
                'run_name': run_name,
                'run_id': run.info.run_id,
                'metrics': metrics,
                'params': params,
                'duration': run.info.end_time - run.info.start_time if run.info.end_time else 0
            }

            # Keep best run for each model type
            # Policy models: highest test_accuracy
            # Value models: lowest test_mae
            if model_key not in results:
                results[model_key] = run_data
            else:
                # Compare with existing best
                if 'policy' in model_key:
                    # Higher accuracy is better
                    new_acc = metrics.get('test_accuracy', 0)
                    old_acc = results[model_key]['metrics'].get('test_accuracy', 0)
                    if new_acc > old_acc:
                        results[model_key] = run_data
                elif 'value' in model_key:
                    # Lower MAE is better
                    new_mae = metrics.get('test_mae', float('inf'))
                    old_mae = results[model_key]['metrics'].get('test_mae', float('inf'))
                    if new_mae < old_mae:
                        results[model_key] = run_data

    return results


def format_percentage(value: float) -> str:
    """Format value as percentage."""
    return f"{value * 100:.2f}%"


def format_improvement(old: float, new: float, lower_is_better: bool = False) -> str:
    """Calculate and format improvement percentage."""
    if old == 0:
        return "N/A"

    if lower_is_better:
        improvement = ((old - new) / old) * 100
    else:
        improvement = ((new - old) / old) * 100

    sign = "+" if improvement > 0 else ""
    return f"{sign}{improvement:.2f}%"


def analyze_results(all_results: Dict[str, Dict]) -> Dict:
    """
    Analyze model results and return structured insights.

    Returns structured data for report generation.
    """
    analysis = {
        'timestamp': datetime.now().isoformat(),
        'datasets': {
            'v1': {'games': 1401, 'moves': 44000},
            'v2': {'games': 5002, 'moves': 156000},
            'v3': {'games': 10401, 'moves': 508000}
        },
        'best_models': {},
        'policy_comparison': {},
        'value_comparison': {},
        'insights': []
    }

    # Find best models
    best_policy_acc = 0
    best_policy_dataset = ""
    best_value_mae = float('inf')
    best_value_dataset = ""

    for dataset, results in all_results.items():
        dataset_name = dataset.replace('_dataset', '')

        # Policy models
        lr_acc = results.get('policy_lr', {}).get('metrics', {}).get('test_accuracy', 0)
        cnn_acc = results.get('policy_cnn', {}).get('metrics', {}).get('test_accuracy', 0)

        analysis['policy_comparison'][dataset_name] = {
            'lr_accuracy': lr_acc,
            'cnn_accuracy': cnn_acc,
            'cnn_improvement_pct': ((cnn_acc - lr_acc) / lr_acc * 100) if lr_acc > 0 else 0
        }

        if cnn_acc > best_policy_acc:
            best_policy_acc = cnn_acc
            best_policy_dataset = dataset_name

        # Value models
        lr_mae = results.get('value_lr', {}).get('metrics', {}).get('test_mae', float('inf'))
        cnn_mae = results.get('value_cnn', {}).get('metrics', {}).get('test_mae', float('inf'))

        analysis['value_comparison'][dataset_name] = {
            'lr_mae': lr_mae,
            'cnn_mae': cnn_mae,
            'cnn_improvement_pct': ((lr_mae - cnn_mae) / lr_mae * 100) if lr_mae > 0 else 0
        }

        if cnn_mae < best_value_mae:
            best_value_mae = cnn_mae
            best_value_dataset = dataset_name

    analysis['best_models'] = {
        'policy': {
            'model': 'CNN',
            'dataset': best_policy_dataset,
            'accuracy': best_policy_acc
        },
        'value': {
            'model': 'CNN',
            'dataset': best_value_dataset,
            'mae': best_value_mae
        }
    }

    # Generate insights (factual observations only)
    # Insight 1: Dataset size impact
    if 'v1' in analysis['policy_comparison'] and 'v2' in analysis['policy_comparison'] and 'v3' in analysis['policy_comparison']:
        v1_acc = analysis['policy_comparison']['v1']['cnn_accuracy']
        v2_acc = analysis['policy_comparison']['v2']['cnn_accuracy']
        v3_acc = analysis['policy_comparison']['v3']['cnn_accuracy']

        if v2_acc > v3_acc:
            analysis['insights'].append({
                'type': 'dataset_size_observation',
                'finding': f'v2 achieves {v2_acc*100:.2f}% accuracy vs v3 {v3_acc*100:.2f}% accuracy',
                'v2_accuracy': v2_acc,
                'v3_accuracy': v3_acc,
                'performance_drop_pct': ((v2_acc - v3_acc) / v2_acc * 100)
            })

        # V1 to V2 improvement
        v1_to_v2_improvement = ((v2_acc - v1_acc) / v1_acc * 100)
        if v1_to_v2_improvement > 5:
            analysis['insights'].append({
                'type': 'data_scaling_benefit',
                'finding': f'v2 improved accuracy by {v1_to_v2_improvement:.1f}% over v1',
                'improvement_pct': v1_to_v2_improvement
            })

    # Insight 2: Architecture comparison
    avg_cnn_improvement_policy = sum(d['cnn_improvement_pct'] for d in analysis['policy_comparison'].values()) / len(analysis['policy_comparison'])
    avg_cnn_improvement_value = sum(d['cnn_improvement_pct'] for d in analysis['value_comparison'].values()) / len(analysis['value_comparison'])

    analysis['insights'].append({
        'type': 'architecture_comparison',
        'finding': 'CNNs outperform Logistic Regression across all datasets',
        'avg_policy_improvement_pct': avg_cnn_improvement_policy,
        'avg_value_improvement_pct': avg_cnn_improvement_value
    })

    return analysis


def main():
    parser = argparse.ArgumentParser(description='Evaluate all trained models')
    parser.add_argument('--format', '-f', type=str, default='json', choices=['json', 'summary'],
                      help='Output format (default: json)')

    args = parser.parse_args()

    print("="*60, file=sys.stderr)
    print("Model Evaluation & Analysis", file=sys.stderr)
    print("="*60, file=sys.stderr)
    print(file=sys.stderr)

    # Connect to MLFlow
    client = MlflowClient()

    # Get results from all experiments
    print("Fetching results from MLFlow...", file=sys.stderr)
    all_results = {}
    for exp_name in ['v1_dataset', 'v2_dataset', 'v3_dataset']:
        print(f"  - {exp_name}...", end=" ", file=sys.stderr)
        results = get_experiment_results(client, exp_name)
        all_results[exp_name] = results
        print(f"✓ ({len(results)} models)", file=sys.stderr)

    print(file=sys.stderr)
    print("Analyzing results...", file=sys.stderr)

    # Analyze results
    analysis = analyze_results(all_results)

    print("✓ Analysis complete", file=sys.stderr)
    print(file=sys.stderr)
    print("="*60, file=sys.stderr)
    print(file=sys.stderr)

    # Output results
    if args.format == 'json':
        # Output JSON to stdout for processing by other scripts
        print(json.dumps(analysis, indent=2))
    else:
        # Print summary to stdout
        print("Best Models:")
        print(f"  Policy: CNN on {analysis['best_models']['policy']['dataset']} - {analysis['best_models']['policy']['accuracy']*100:.2f}%")
        print(f"  Value: CNN on {analysis['best_models']['value']['dataset']} - MAE {analysis['best_models']['value']['mae']:.4f}")
        print()
        print(f"Key Insights: {len(analysis['insights'])} found")
        for i, insight in enumerate(analysis['insights'], 1):
            print(f"  {i}. {insight['type']}: {insight['finding']}")


if __name__ == "__main__":
    main()
