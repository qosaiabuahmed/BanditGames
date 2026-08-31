import requests
import json


class ChatbotClient:
    """Simple client for testing the chatbot API."""

    def __init__(self, base_url: str = "http://localhost:7777"):
        self.base_url = base_url

    def chat(self, question: str, use_cache: bool = True, n_results: int = 3):
        """Send a question to the chatbot."""
        response = requests.post(
            f"{self.base_url}/chat",
            json={
                "question": question,
                "use_cache": use_cache,
                "n_results": n_results
            }
        )
        return response.json()

    def get_stats(self):
        """Get system statistics."""
        response = requests.get(f"{self.base_url}/stats")
        return response.json()

    def get_games(self):
        """Get list of available games."""
        response = requests.get(f"{self.base_url}/games")
        return response.json()

    def clear_cache(self):
        """Clear the response cache."""
        response = requests.post(f"{self.base_url}/cache/clear")
        return response.json()


def main():
    """Example usage of the chatbot client."""
    client = ChatbotClient()

    print("=" * 60)
    print("Board Game Platform Chatbot - Test Client")
    print("=" * 60)

    # Example questions
    questions = [
        "What are the rules of Connect Four?",
        "How do I win at Connect Four?",
        "Can I remove a disc once it's been placed?",
        "How do I start a game?",
        "What happens if I run out of time?",
        "What are some strategy tips for Connect Four?"
    ]

    for i, question in enumerate(questions, 1):
        print(f"\nQuestion {i}: {question}")
        print("-" * 60)

        try:
            result = client.chat(question)
            print(f"Response (cached={result['cached']}):")
            print(result['response'])
            print(f"\nProcessing time: {result['processing_time']:.3f}s")

            if result['sources']:
                print(f"Sources: {len(result['sources'])} documents")

        except Exception as e:
            print(f"Error: {e}")

        print()

    # Get statistics
    print("\n" + "=" * 60)
    print("System Statistics")
    print("=" * 60)
    try:
        stats = client.get_stats()
        print(json.dumps(stats, indent=2))
    except Exception as e:
        print(f"Error getting stats: {e}")


if __name__ == "__main__":
    main()
