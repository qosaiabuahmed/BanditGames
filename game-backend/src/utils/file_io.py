import json
import xml.etree.ElementTree as ET
from typing import Optional, List
import uuid
from datetime import datetime
from src.gameLogic.connect_four import GameConfig, GameState, Player


class ConfigurationManager:
    """Manages loading game configuration from XML files."""

    @staticmethod
    def load_from_xml(filepath: str) -> GameConfig:
        """
        Load game configuration from XML file.

        Args:
            filepath: Path to XML configuration file

        Returns:
            GameConfig object with loaded settings

        Raises:
            ValueError: If configuration values are invalid
        """
        try:
            tree = ET.parse(filepath)
            root = tree.getroot()

            # Find configuration section
            config_elem = root.find('Configurations')
            if config_elem is None:
                raise ValueError("No Configurations section found in XML")

            # Extract values with defaults
            width = int(config_elem.findtext('Width', '7'))
            height = int(config_elem.findtext('Height', '6'))
            high_score = int(config_elem.findtext('Highscores', '5'))

            # Validate
            if width < 4 or height < 4:
                raise ValueError("Board dimensions must be at least 4x4")

            return GameConfig(
                rows=height,
                cols=width,
                high_score_limit=high_score
            )

        except ET.ParseError as e:
            print(f"Error parsing XML configuration: {e}")
            print("Using default configuration...")
            return GameConfig()
        except FileNotFoundError as e:
            print(f"Configuration file not found: {e}")
            print("Using default configuration...")
            return GameConfig()
        except Exception as e:
            print(f"Unexpected error loading configuration: {e}")
            print("Using default configuration...")
            return GameConfig()


class GameSaveManager:
    """Manages saving and loading game states to/from JSON files."""

    @staticmethod
    def save_game_json(
        state: GameState,
        filepath: str,
        player_names: tuple = ("Player 1", "Player 2"),
        game_id: Optional[str] = None
    ) -> None:
        """
        Save game state to JSON file using coordinate-based format.

        Args:
            state: Current game state to save
            filepath: Path where the JSON file will be saved
            player_names: Tuple of (player1_name, player2_name)
            game_id: Optional unique game identifier

        Raises:
            IOError: If file cannot be written
        """
        # Generate game_id if not provided
        if game_id is None:
            game_id = f"game_{datetime.now().strftime('%Y%m%d_%H%M%S')}_{str(uuid.uuid4())[:8]}"

        # Convert board to coordinates format
        pieces = []
        for row_idx, row in enumerate(state.board):
            for col_idx, cell in enumerate(row):
                if cell != Player.NONE:
                    pieces.append({
                        'row': row_idx,
                        'col': col_idx,
                        'player': cell.value
                    })

        save_data = {
            'game_id': game_id,
            'pieces': pieces,
            'score_x': state.score_x,
            'score_o': state.score_o,
            'current_turn': state.current_turn,
            'current_player': state.current_player.value,
            'column_positions': state.column_positions,
            'move_history': state.move_history,
        }

        with open(filepath, 'w') as f:
            json.dump(save_data, f, indent=2)

    @staticmethod
    def load_game_json(filepath: str) -> tuple[GameState, tuple, Optional[str]]:
        """
        Load game state from JSON file.

        Args:
            filepath: Path to the JSON save file

        Returns:
            Tuple of (game_state, player_names, game_id)

        Raises:
            FileNotFoundError: If save file doesn't exist
            json.JSONDecodeError: If file contains invalid JSON
        """
        with open(filepath, 'r') as f:
            data = json.load(f)

        # Use default board dimensions (6x7)
        default_config = GameConfig()
        rows = default_config.rows
        cols = default_config.cols

        # Initialize empty board
        board = [[Player.NONE for _ in range(cols)] for _ in range(rows)]

        # Place pieces from coordinates
        if 'pieces' in data:
            for piece in data['pieces']:
                row = piece['row']
                col = piece['col']
                player = Player(piece['player'])
                board[row][col] = player

        # Handle current_player
        current_turn = data['current_turn']
        if 'current_player' in data:
            current_player = Player(data['current_player'])
        else:
            current_player = Player.X if current_turn % 2 == 0 else Player.O

        state = GameState(
            board=board,
            score_x=data['score_x'],
            score_o=data['score_o'],
            current_turn=current_turn,
            current_player=current_player,
            column_positions=data['column_positions'],
            move_history=data['move_history']
        )

        player_names = tuple(data.get('player_names', ('Player 1', 'Player 2')))
        game_id = data.get('game_id')

        return state, player_names, game_id


class HighScoreManager:
    """Manages high score tracking and persistence."""

    def __init__(self, filepath: str = "high_scores.json"):
        """
        Initialize high score manager.

        Args:
            filepath: Path to high scores file
        """
        self.filepath = filepath
        self.scores: List[int] = []
        self.load_scores()

    def load_scores(self) -> None:
        """
        Load high scores from file.

        If file doesn't exist or contains invalid JSON, initializes empty score list.
        """
        try:
            with open(self.filepath, 'r') as f:
                data = json.load(f)
                self.scores = data.get('scores', [])
        except FileNotFoundError:
            self.scores = []
        except json.JSONDecodeError:
            print(f"Warning: Invalid JSON in {self.filepath}, starting with empty scores")
            self.scores = []

    def save_scores(self) -> None:
        """
        Save high scores to file.

        Raises:
            IOError: If file cannot be written
        """
        with open(self.filepath, 'w') as f:
            json.dump({'scores': self.scores}, f, indent=2)

    def add_score(self, score: int) -> None:
        """
        Add a new score to the high scores list.

        Scores are automatically sorted in descending order.

        Args:
            score: Score to add
        """
        self.scores.append(score)
        self.scores.sort(reverse=True)

    def get_top_scores(self, limit: int = 10) -> List[int]:
        """
        Get the top N high scores.

        Args:
            limit: Number of scores to return

        Returns:
            List of top scores
        """
        return self.scores[:limit]

    def is_high_score(self, score: int, limit: int = 10) -> bool:
        """
        Check if a score qualifies as a high score.

        Args:
            score: Score to check
            limit: Size of high score list

        Returns:
            True if score makes the high score list
        """
        if len(self.scores) < limit:
            return True
        return score > min(self.scores[:limit])
