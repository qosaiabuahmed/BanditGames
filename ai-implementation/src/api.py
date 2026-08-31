"""
AI Container API

Flask API that receives game state and returns best move using MCTS.
"""

from flask import Flask, request, jsonify
import logging

from src.game_factory import GameFactory
from src.config import AIConfig, Difficulty
from src.ai_service import AIService

# Initialize Flask app
app = Flask(__name__)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    supported_games = GameFactory.get_supported_games()
    return jsonify({
        'status': 'healthy',
        'service': 'mcts-game-ai',
        'supported_games': supported_games
    }), 200


@app.route('/get-move', methods=['POST'])
def get_move():
    """
    Get best move for current game state (supports multiple games).

    Expected JSON format for Connect4:
    {
        "game_type": "connect4",
        "board": [["X", ".", ...], ...],  // 6x7 grid
        "score_x": 3,
        "score_o": 2,
        "current_player": "X",
        "column_positions": [5, 4, 3, ...],
        "difficulty": 2  // 1 (easy), 2 (medium), 3 (hard)
    }

    Expected JSON format for TicTacToe:
    {
        "game_type": "tictactoe",
        "board": [["X", ".", "O"], [".", "X", "."], [".", ".", "."]],  // 3x3 grid
        "current_player": "O",
        "difficulty": 2
    }

    Returns:
    {
        "game_type": "connect4",
        "move": 3,  // 0-indexed move (column for Connect4, position for TicTacToe)
        "confidence": 0.85,
        "nodes_explored": 2000,
        "computation_time_ms": 856
    }
    """
    try:
        # Get request data
        data = request.get_json()

        if not data:
            return jsonify({
                'error': 'No JSON data provided'
            }), 400

        # Get game_type field (default to connect4 for backward compatibility)
        game_type = data.get('game_type', 'connect4')

        # Ensure game_type is in the data dict for factory
        data['game_type'] = game_type

        # Validate required fields
        required_fields = ['board', 'current_player']
        missing_fields = [field for field in required_fields if field not in data]

        if missing_fields:
            return jsonify({
                'error': f'Missing required fields: {missing_fields}'
            }), 400

        # Get difficulty (default to medium)
        difficulty = data.get('difficulty', Difficulty.MEDIUM)
        if difficulty not in [Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD]:
            logger.warning(f"Invalid difficulty {difficulty}, defaulting to {Difficulty.MEDIUM} (medium)")
            difficulty = Difficulty.MEDIUM

        # Get difficulty profile for this game
        try:
            profile = AIConfig.get_profile(game_type, difficulty)
        except ValueError as e:
            return jsonify({'error': str(e)}), 400

        logger.info(
            f"Received move request for {game_type} - player {data['current_player']} "
            f"(difficulty: {difficulty}, iterations: {profile['max_iterations']})"
        )

        # Create game state from JSON using factory
        try:
            game_state = GameFactory.create_from_json(data)
        except ValueError as e:
            return jsonify({'error': str(e)}), 400

        # Calculate move using AI service
        move_result = AIService.calculate_move(game_state, difficulty)

        return jsonify(move_result.to_dict()), 200

    except Exception as e:
        logger.error(f"Error processing move request: {str(e)}", exc_info=True)
        return jsonify({
            'error': 'Internal server error',
            'message': str(e)
        }), 500


@app.route('/analyze', methods=['POST'])
def analyze_position():
    """
    Analyze current position and return statistics for all moves (supports multiple games).

    Returns detailed analysis of each possible move.

    Expected JSON format: Same as /get-move

    Returns:
    {
        "game_type": "connect4",
        "best_move": 3,  // 0-indexed move
        "move_analysis": {
            "0": {"visits": 150, "win_rate": 0.45, ...},
            "3": {"visits": 200, "win_rate": 0.52, ...},
            ...
        },
        "total_simulations": 2000
    }
    """
    try:
        data = request.get_json()

        if not data:
            return jsonify({'error': 'No JSON data provided'}), 400

        # Validate game_type
        game_type = data.get('game_type')
        if not game_type:
            return jsonify({'error': "Missing required field: 'game_type'"}), 400

        logger.info(f"Received analysis request for {game_type} - player {data['current_player']}")

        # Create game state using factory
        try:
            game_state = GameFactory.create_from_json(data)
        except ValueError as e:
            return jsonify({'error': str(e)}), 400

        # Analyze position using AI service
        result = AIService.analyze_position(game_state, Difficulty.MEDIUM)

        logger.info(f"[{game_type.upper()}] Analysis complete: best move is {result['best_move']} (0-indexed)")

        return jsonify(result), 200

    except Exception as e:
        logger.error(f"Error analyzing position: {str(e)}", exc_info=True)
        return jsonify({
            'error': 'Internal server error',
            'message': str(e)
        }), 500


@app.route('/config', methods=['GET'])
def get_config():
    """
    Get current AI configuration for all games and difficulty levels.

    Returns:
    {
        "connect4": {
            "1": {"max_iterations": 50, "exploration_constant": 1.41, "mistake_probability": 0.2},
            "2": {"max_iterations": 125, "exploration_constant": 1.41, "mistake_probability": 0.01},
            "3": {"max_iterations": 750, "exploration_constant": 1.41, "mistake_probability": 0.0}
        },
        "tictactoe": {
            ...
        }
    }
    """
    try:
        # Convert the config to a serializable format
        config_data = {}
        for game_type, profiles in AIConfig.DIFFICULTY_PROFILES.items():
            config_data[game_type] = {
                str(difficulty): profile.copy()
                for difficulty, profile in profiles.items()
            }

        return jsonify(config_data), 200

    except Exception as e:
        logger.error(f"Error retrieving config: {str(e)}", exc_info=True)
        return jsonify({
            'error': 'Internal server error',
            'message': str(e)
        }), 500


@app.route('/config', methods=['PUT', 'PATCH'])
def update_config():
    """
    Update AI configuration for specific game type and difficulty level.

    Expected JSON format:
    {
        "game_type": "connect4",
        "difficulty": 2,
        "max_iterations": 200,
        "exploration_constant": 1.5,
        "mistake_probability": 0.05
    }

    All parameter fields are optional - only provided fields will be updated.

    Returns:
    {
        "success": true,
        "message": "Configuration updated successfully",
        "updated_config": {
            "max_iterations": 200,
            "exploration_constant": 1.5,
            "mistake_probability": 0.05
        }
    }
    """
    try:
        data = request.get_json()

        if not data:
            return jsonify({'error': 'No JSON data provided'}), 400

        # Validate required fields
        game_type = data.get('game_type')
        difficulty = data.get('difficulty')

        if not game_type:
            return jsonify({'error': "Missing required field: 'game_type'"}), 400

        if difficulty is None:
            return jsonify({'error': "Missing required field: 'difficulty'"}), 400

        # Normalize game_type
        game_type = game_type.lower()

        # Validate game_type
        if game_type not in AIConfig.DIFFICULTY_PROFILES:
            supported = ', '.join(AIConfig.DIFFICULTY_PROFILES.keys())
            return jsonify({
                'error': f"Unsupported game_type: '{game_type}'",
                'supported_games': list(AIConfig.DIFFICULTY_PROFILES.keys())
            }), 400

        # Validate difficulty
        try:
            difficulty = int(difficulty)
        except (ValueError, TypeError):
            return jsonify({'error': f"Invalid difficulty value: {difficulty}"}), 400

        if difficulty not in [Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD]:
            return jsonify({
                'error': f"Invalid difficulty: {difficulty}",
                'valid_difficulties': {
                    'easy': Difficulty.EASY,
                    'medium': Difficulty.MEDIUM,
                    'hard': Difficulty.HARD
                }
            }), 400

        # Get current profile
        current_profile = AIConfig.DIFFICULTY_PROFILES[game_type][difficulty]

        # Track what was updated
        updated_fields = []

        # Update max_iterations if provided
        if 'max_iterations' in data:
            max_iterations = data['max_iterations']
            if not isinstance(max_iterations, int) or max_iterations <= 0:
                return jsonify({
                    'error': f"Invalid max_iterations: {max_iterations}. Must be a positive integer"
                }), 400
            current_profile['max_iterations'] = max_iterations
            updated_fields.append('max_iterations')

        # Update exploration_constant if provided
        if 'exploration_constant' in data:
            exploration_constant = data['exploration_constant']
            if not isinstance(exploration_constant, (int, float)) or exploration_constant <= 0:
                return jsonify({
                    'error': f"Invalid exploration_constant: {exploration_constant}. Must be a positive number"
                }), 400
            current_profile['exploration_constant'] = float(exploration_constant)
            updated_fields.append('exploration_constant')

        # Update mistake_probability if provided
        if 'mistake_probability' in data:
            mistake_probability = data['mistake_probability']
            if not isinstance(mistake_probability, (int, float)) or not (0 <= mistake_probability <= 1):
                return jsonify({
                    'error': f"Invalid mistake_probability: {mistake_probability}. Must be between 0 and 1"
                }), 400
            current_profile['mistake_probability'] = float(mistake_probability)
            updated_fields.append('mistake_probability')

        if not updated_fields:
            return jsonify({
                'error': 'No valid configuration fields provided to update',
                'valid_fields': ['max_iterations', 'exploration_constant', 'mistake_probability']
            }), 400

        logger.info(
            f"Configuration updated for {game_type} difficulty {difficulty}: "
            f"updated fields: {', '.join(updated_fields)}"
        )

        return jsonify({
            'success': True,
            'message': 'Configuration updated successfully',
            'game_type': game_type,
            'difficulty': difficulty,
            'updated_fields': updated_fields,
            'updated_config': current_profile.copy()
        }), 200

    except Exception as e:
        logger.error(f"Error updating config: {str(e)}", exc_info=True)
        return jsonify({
            'error': 'Internal server error',
            'message': str(e)
        }), 500


@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors."""
    return jsonify({
        'error': 'Endpoint not found',
        'available_endpoints': [
            '/health',
            '/get-move',
            '/analyze',
            '/config'
        ]
    }), 404


@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors."""
    logger.error(f"Internal server error: {str(error)}")
    return jsonify({
        'error': 'Internal server error'
    }), 500


if __name__ == '__main__':
    supported_games = GameFactory.get_supported_games()
    logger.info("Starting Multi-Game MCTS AI Service...")
    logger.info(f"Supported games: {', '.join(supported_games)}")

    for game_type in supported_games:
        profiles = AIConfig.DIFFICULTY_PROFILES[game_type]
        logger.info(
            f"  {game_type.upper()} - Easy: {profiles[1]['max_iterations']} iterations, "
            f"Medium: {profiles[2]['max_iterations']} iterations, "
            f"Hard: {profiles[3]['max_iterations']} iterations"
        )

    app.run(
        host=AIConfig.SERVER_HOST,
        port=AIConfig.SERVER_PORT,
        debug=AIConfig.DEBUG_MODE
    )