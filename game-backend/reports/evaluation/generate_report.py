#!/usr/bin/env python3
"""
Generate comprehensive evaluation report with visualizations.

Usage:
    python generate_report.py
"""

import json
import subprocess
import sys
from pathlib import Path
import matplotlib
matplotlib.use('Agg')  # Non-interactive backend
import matplotlib.pyplot as plt
import seaborn as sns
from datetime import datetime

# Set style
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (10, 6)
plt.rcParams['font.size'] = 11


def run_evaluation():
    """Run the evaluation script and get JSON results."""
    print("Running model evaluation...")
    # Get project root (reports/evaluation/generate_report.py -> ../../ = project root)
    project_root = Path(__file__).parent.parent.parent
    result = subprocess.run(
        ['python', 'src/ml/evaluation/evaluate_models.py', '--format', 'json'],
        capture_output=True,
        text=True,
        cwd=project_root
    )

    if result.returncode != 0:
        print(f"Error running evaluation: {result.stderr}")
        sys.exit(1)

    return json.loads(result.stdout)


def create_visualizations(analysis, output_dir):
    """Generate all visualization charts."""
    output_dir = Path(output_dir)
    output_dir.mkdir(exist_ok=True)

    # Chart 1: Policy Model Accuracy Comparison
    fig, ax = plt.subplots(figsize=(12, 6))

    datasets = list(analysis['policy_comparison'].keys())
    lr_accs = [analysis['policy_comparison'][d]['lr_accuracy'] * 100 for d in datasets]
    cnn_accs = [analysis['policy_comparison'][d]['cnn_accuracy'] * 100 for d in datasets]

    x = range(len(datasets))
    width = 0.35

    bars1 = ax.bar([i - width/2 for i in x], lr_accs, width, label='Logistic Regression', color='#FF6B6B')
    bars2 = ax.bar([i + width/2 for i in x], cnn_accs, width, label='CNN', color='#4ECDC4')

    ax.set_xlabel('Dataset')
    ax.set_ylabel('Test Accuracy (%)')
    ax.set_title('Policy Models: Move Prediction Accuracy', fontsize=14, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(datasets)
    ax.legend()
    ax.grid(axis='y', alpha=0.3)

    # Add value labels on bars
    for bars in [bars1, bars2]:
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'{height:.1f}%',
                   ha='center', va='bottom', fontsize=9)

    plt.tight_layout()
    plt.savefig(output_dir / 'policy_accuracy_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()

    # Chart 2: Value Model MAE Comparison
    fig, ax = plt.subplots(figsize=(12, 6))

    lr_maes = [analysis['value_comparison'][d]['lr_mae'] for d in datasets]
    cnn_maes = [analysis['value_comparison'][d]['cnn_mae'] for d in datasets]

    bars1 = ax.bar([i - width/2 for i in x], lr_maes, width, label='Logistic Regression', color='#FF6B6B')
    bars2 = ax.bar([i + width/2 for i in x], cnn_maes, width, label='CNN', color='#4ECDC4')

    ax.set_xlabel('Dataset')
    ax.set_ylabel('Test MAE (lower is better)')
    ax.set_title('Value Models: Win Probability Prediction Error', fontsize=14, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(datasets)
    ax.legend()
    ax.grid(axis='y', alpha=0.3)

    # Add value labels on bars
    for bars in [bars1, bars2]:
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'{height:.3f}',
                   ha='center', va='bottom', fontsize=9)

    plt.tight_layout()
    plt.savefig(output_dir / 'value_mae_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()

    # Chart 3: CNN Improvement Percentage
    fig, ax = plt.subplots(figsize=(12, 6))

    policy_improvements = [analysis['policy_comparison'][d]['cnn_improvement_pct'] for d in datasets]
    value_improvements = [analysis['value_comparison'][d]['cnn_improvement_pct'] for d in datasets]

    bars1 = ax.bar([i - width/2 for i in x], policy_improvements, width, label='Policy (Accuracy)', color='#95E1D3')
    bars2 = ax.bar([i + width/2 for i in x], value_improvements, width, label='Value (MAE Reduction)', color='#F38181')

    ax.set_xlabel('Dataset')
    ax.set_ylabel('Improvement (%)')
    ax.set_title('CNN Improvement Over Logistic Regression', fontsize=14, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(datasets)
    ax.legend()
    ax.grid(axis='y', alpha=0.3)
    ax.axhline(y=0, color='black', linestyle='-', linewidth=0.5)

    # Add value labels on bars
    for bars in [bars1, bars2]:
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'+{height:.1f}%',
                   ha='center', va='bottom' if height > 0 else 'top', fontsize=9)

    plt.tight_layout()
    plt.savefig(output_dir / 'cnn_improvement.png', dpi=300, bbox_inches='tight')
    plt.close()

    # Chart 4: Dataset Size vs Performance
    fig, ax = plt.subplots(figsize=(12, 6))

    games = [analysis['datasets'][d]['games'] for d in datasets]
    accuracies = [analysis['policy_comparison'][d]['cnn_accuracy'] * 100 for d in datasets]

    ax.plot(games, accuracies, marker='o', linewidth=2, markersize=10, color='#4ECDC4')
    for i, (g, a, d) in enumerate(zip(games, accuracies, datasets)):
        ax.annotate(f'{d}\n{a:.1f}%', xy=(g, a), xytext=(10, 10 if i % 2 == 0 else -20),
                   textcoords='offset points', fontsize=9,
                   bbox=dict(boxstyle='round,pad=0.5', facecolor='yellow', alpha=0.3))

    ax.set_xlabel('Number of Training Games')
    ax.set_ylabel('CNN Policy Accuracy (%)')
    ax.set_title('Impact of Dataset Size on Model Performance', fontsize=14, fontweight='bold')
    ax.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig(output_dir / 'dataset_size_impact.png', dpi=300, bbox_inches='tight')
    plt.close()

    print(f"✓ Generated 4 visualizations in {output_dir}/")


def generate_markdown_report(analysis, output_file):
    """Generate comprehensive markdown report."""
    lines = []

    # Header
    lines.append("# Connect Four ML Models - Evaluation Report")
    lines.append("")
    lines.append(f"**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    lines.append("")
    lines.append("---")
    lines.append("")

    # Executive Summary
    lines.append("## Summary")
    lines.append("")
    best_policy = analysis['best_models']['policy']
    best_value = analysis['best_models']['value']
    lines.append(f"12 models trained across 3 datasets. Best results:")
    lines.append("")
    lines.append(f"- Policy: {best_policy['model']} on {best_policy['dataset']} - {best_policy['accuracy']*100:.2f}% accuracy")
    lines.append(f"- Value: {best_value['model']} on {best_value['dataset']} - {best_value['mae']:.4f} MAE")
    lines.append("")

    # Datasets
    lines.append("## Datasets")
    lines.append("")
    lines.append("| Dataset | Games | Moves |")
    lines.append("|---------|-------|-------|")
    for name, data in analysis['datasets'].items():
        lines.append(f"| {name} | {data['games']:,} | {data['moves']:,} |")
    lines.append("")

    # Results - Policy Models
    lines.append("## Results")
    lines.append("")
    lines.append("### Policy Models (Move Prediction)")
    lines.append("")
    lines.append("![Policy Accuracy Comparison](policy_accuracy_comparison.png)")
    lines.append("")
    lines.append("| Dataset | LR Accuracy | CNN Accuracy | Improvement |")
    lines.append("|---------|-------------|--------------|-------------|")
    for dataset, data in analysis['policy_comparison'].items():
        lines.append(f"| {dataset} | {data['lr_accuracy']*100:.2f}% | {data['cnn_accuracy']*100:.2f}% | +{data['cnn_improvement_pct']:.1f}% |")
    lines.append("")

    # Results - Value Models
    lines.append("### Value Models (Win Probability Prediction)")
    lines.append("")
    lines.append("![Value MAE Comparison](value_mae_comparison.png)")
    lines.append("")
    lines.append("| Dataset | LR MAE | CNN MAE | Improvement |")
    lines.append("|---------|--------|---------|-------------|")
    for dataset, data in analysis['value_comparison'].items():
        lines.append(f"| {dataset} | {data['lr_mae']:.4f} | {data['cnn_mae']:.4f} | +{data['cnn_improvement_pct']:.1f}% |")
    lines.append("")

    # CNN Improvement
    lines.append("### CNN vs Logistic Regression")
    lines.append("")
    lines.append("![CNN Improvement](cnn_improvement.png)")
    lines.append("")

    # Analysis
    lines.append("## Notes")
    lines.append("")

    lines.append("### Dataset Size vs Performance")
    lines.append("")
    lines.append("![Dataset Size Impact](dataset_size_impact.png)")
    lines.append("")

    lines.append("### CNN vs Logistic Regression")
    lines.append("")
    avg_policy_improvement = sum(d['cnn_improvement_pct'] for d in analysis['policy_comparison'].values()) / len(analysis['policy_comparison'])
    avg_value_improvement = sum(d['cnn_improvement_pct'] for d in analysis['value_comparison'].values()) / len(analysis['value_comparison'])

    lines.append(f"Average CNN improvement:")
    lines.append(f"- Policy: +{avg_policy_improvement:.1f}%")
    lines.append(f"- Value: +{avg_value_improvement:.1f}%")
    lines.append("")

    # Technical Details
    lines.append("## Technical Details")
    lines.append("")
    lines.append("### Model Architectures")
    lines.append("")
    lines.append("**CNN (Convolutional Neural Network):**")
    lines.append("```")
    lines.append("Input: 6x7 board")
    lines.append("Conv2D(64 filters, 3x3) + ReLU + Padding")
    lines.append("Conv2D(64 filters, 3x3) + ReLU + Padding")
    lines.append("Conv2D(32 filters, 3x3) + ReLU + Padding")
    lines.append("Flatten")
    lines.append("Dense(128) + Dropout(0.2)")
    lines.append("Dense(64) + Dropout(0.2)")
    lines.append("Output: Dense(7) for policy, Dense(1) for value")
    lines.append("Parameters: ~237k")
    lines.append("```")
    lines.append("")
    lines.append("**Logistic/Linear Regression:**")
    lines.append("```")
    lines.append("Input: Flattened board (42 features)")
    lines.append("Output: 7 classes (policy) or 1 value (value)")
    lines.append("Parameters: ~300")
    lines.append("```")
    lines.append("")

    lines.append("### Training Configuration")
    lines.append("")
    lines.append("- **Optimizer:** Adam")
    lines.append("- **Learning Rate:** 0.001")
    lines.append("- **Batch Size:** 32")
    lines.append("- **Epochs:** 50 (with early stopping)")
    lines.append("- **Train/Val/Test Split:** 70% / 15% / 15%")
    lines.append("- **Early Stopping:** Patience = 10 epochs")
    lines.append("")

    lines.append("### MLFlow Tracking")
    lines.append("")
    lines.append("All experiments tracked in MLFlow:")
    lines.append("- **URL:** http://localhost:5002")
    lines.append("- **Experiments:** v1_dataset, v2_dataset, v3_dataset")
    lines.append("- **Metrics:** Training/validation loss, test accuracy, test MAE")
    lines.append("- **Artifacts:** Model files, training curves, configuration")
    lines.append("")


    # Footer
    lines.append("---")
    lines.append(f"*Generated {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} from MLFlow data*")
    lines.append("")

    # Write to file
    with open(output_file, 'w') as f:
        f.write('\n'.join(lines))

    print(f"✓ Report saved to {output_file}")


def main():
    print("="*60)
    print("Comprehensive Model Evaluation Report Generator")
    print("="*60)
    print()

    # Get analysis data
    analysis = run_evaluation()

    # Create output directory
    output_dir = Path(__file__).parent
    print(f"Output directory: {output_dir}")
    print()

    # Generate visualizations
    print("Generating visualizations...")
    create_visualizations(analysis, output_dir)
    print()

    # Generate markdown report
    print("Generating markdown report...")
    generate_markdown_report(analysis, output_dir / 'evaluation_report.md')
    print()

    print("="*60)
    print("✓ Report generation complete!")
    print(f"  - View report: {output_dir / 'evaluation_report.md'}")
    print(f"  - Visualizations: {output_dir}/*.png")
    print("="*60)


if __name__ == "__main__":
    main()
