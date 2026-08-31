import json
from typing import List, Dict
from pathlib import Path
class KnowledgeLoader:
    """
    Loads and structures knowledge base content for the chatbot.
    Handles game rules and platform guidance documentation.
    """

    def __init__(self, knowledge_file: str = "../knowledge_base/game_rules.json"):
        self.knowledge_file = Path(knowledge_file)
        self.data = self._load_knowledge()

    def _load_knowledge(self) -> Dict:
        """Load knowledge base from JSON file."""
        if not self.knowledge_file.exists():
            raise FileNotFoundError(f"Knowledge base file not found: {self.knowledge_file}")

        with open(self.knowledge_file, 'r', encoding='utf-8') as f:
            return json.load(f)

    def get_documents(self) -> List[Dict[str, str]]:
        """
        Convert knowledge base into document chunks for vector storage.

        Returns:
            List of documents with 'content' and 'metadata'
        """
        documents = []

        # Process game rules
        for game in self.data.get("games", []):
            game_name = game["name"]
            game_id = game["id"]

            # Overview document
            documents.append({
                "content": f"{game_name} Overview:\n{game['description']}\n"
                          f"Players: {game['players']}\n"
                          f"Duration: {game['duration']}\n"
                          f"Category: {game['category']}",
                "metadata": {
                    "type": "game_overview",
                    "game": game_name,
                    "game_id": game_id
                }
            })

            # Setup rules
            if "rules" in game and "setup" in game["rules"]:
                setup_content = f"{game_name} Setup:\n" + "\n".join(
                    f"{i+1}. {rule}" for i, rule in enumerate(game["rules"]["setup"])
                )
                documents.append({
                    "content": setup_content,
                    "metadata": {
                        "type": "game_setup",
                        "game": game_name,
                        "game_id": game_id
                    }
                })

            # Gameplay rules
            if "rules" in game and "gameplay" in game["rules"]:
                gameplay_content = f"{game_name} Gameplay:\n" + "\n".join(
                    f"{i+1}. {rule}" for i, rule in enumerate(game["rules"]["gameplay"])
                )
                documents.append({
                    "content": gameplay_content,
                    "metadata": {
                        "type": "game_gameplay",
                        "game": game_name,
                        "game_id": game_id
                    }
                })

            # Winning conditions
            if "rules" in game and "winning" in game["rules"]:
                winning_content = f"{game_name} Winning Conditions:\n" + "\n".join(
                    f"{i+1}. {rule}" for i, rule in enumerate(game["rules"]["winning"])
                )
                documents.append({
                    "content": winning_content,
                    "metadata": {
                        "type": "game_winning",
                        "game": game_name,
                        "game_id": game_id
                    }
                })

            # Strategy tips
            if "rules" in game and "strategy_tips" in game["rules"]:
                strategy_content = f"{game_name} Strategy Tips:\n" + "\n".join(
                    f"- {tip}" for tip in game["rules"]["strategy_tips"]
                )
                documents.append({
                    "content": strategy_content,
                    "metadata": {
                        "type": "game_strategy",
                        "game": game_name,
                        "game_id": game_id
                    }
                })

            # Common questions
            if "common_questions" in game:
                for qa in game["common_questions"]:
                    documents.append({
                        "content": f"{game_name} - Q: {qa['question']}\nA: {qa['answer']}",
                        "metadata": {
                            "type": "game_faq",
                            "game": game_name,
                            "game_id": game_id
                        }
                    })

        # Process platform guidance
        for category_data in self.data.get("platform_guidance", []):
            category = category_data["category"]
            for topic in category_data.get("topics", []):
                documents.append({
                    "content": f"{topic['title']}:\n{topic['content']}",
                    "metadata": {
                        "type": "platform_guidance",
                        "category": category,
                        "topic": topic["title"]
                    }
                })

        return documents

    def get_game_names(self) -> List[str]:
        """Get list of all available game names."""
        return [game["name"] for game in self.data.get("games", [])]

    def get_game_by_name(self, name: str) -> Dict:
        """Get full game data by name."""
        for game in self.data.get("games", []):
            if game["name"].lower() == name.lower() or game["id"] == name.lower():
                return game
        return None
