import time
import json
from pathlib import Path
from collections import OrderedDict
from typing import Optional, Any
from config import settings


class InMemoryCache:
    """
    Simple in-memory LRU cache with TTL support for caching chatbot responses.
    Helps improve response times for frequently asked questions.
    """

    def __init__(self, max_size: int = None, ttl: int = None):
        self.max_size = max_size or settings.cache_size
        self.ttl = ttl or settings.cache_ttl
        self.cache = OrderedDict()
        self.timestamps = {}
        self.hits = 0
        self.misses = 0

    def _normalize_key(self, key: str) -> str:
        """Normalize the cache key by lowercasing and stripping whitespace."""
        return key.lower().strip()

    def get(self, key: str) -> Optional[Any]:
        """
        Retrieve a value from cache if it exists and hasn't expired.

        Args:
            key: The cache key

        Returns:
            Cached value if found and valid, None otherwise
        """
        normalized_key = self._normalize_key(key)

        if normalized_key not in self.cache:
            self.misses += 1
            return None

        # Check if expired
        if time.time() - self.timestamps[normalized_key] > self.ttl:
            self._remove(normalized_key)
            self.misses += 1
            return None

        # Move to end (most recently used)
        self.cache.move_to_end(normalized_key)
        self.hits += 1
        return self.cache[normalized_key]

    def set(self, key: str, value: Any) -> None:
        """
        Store a value in cache.

        Args:
            key: The cache key
            value: The value to store
        """
        normalized_key = self._normalize_key(key)

        # Remove oldest item if at capacity
        if normalized_key not in self.cache and len(self.cache) >= self.max_size:
            oldest_key = next(iter(self.cache))
            self._remove(oldest_key)

        self.cache[normalized_key] = value
        self.timestamps[normalized_key] = time.time()
        self.cache.move_to_end(normalized_key)

    def _remove(self, key: str) -> None:
        """Remove a key from cache."""
        if key in self.cache:
            del self.cache[key]
            del self.timestamps[key]

    def clear(self) -> None:
        """Clear all cached items."""
        self.cache.clear()
        self.timestamps.clear()
        self.hits = 0
        self.misses = 0

    def get_stats(self) -> dict:
        """Get cache statistics."""
        total_requests = self.hits + self.misses
        hit_rate = (self.hits / total_requests * 100) if total_requests > 0 else 0

        return {
            "size": len(self.cache),
            "max_size": self.max_size,
            "hits": self.hits,
            "misses": self.misses,
            "hit_rate": f"{hit_rate:.2f}%",
            "ttl": self.ttl
        }

    def get_warmup_questions(self, file_path: str = "../predefined_cache.json") -> list:
        """
        Load list of questions to warm up the cache at startup.

        Args:
            file_path: Path to the JSON file containing questions

        Returns:
            List of questions to process through RAG system

        Example JSON format:
        {
            "questions_to_warm": [
                "What are the rules?",
                "How do I start a game?"
            ]
        }
        """
        cache_file = Path(file_path)
        if not cache_file.exists():
            print(f"Cache warmup file not found: {file_path}")
            return []

        try:
            with open(cache_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            questions = data.get("questions_to_warm", [])
            print(f"Found {len(questions)} questions for cache warming.")
            return questions

        except json.JSONDecodeError as e:
            print(f"Error parsing cache warmup file: {e}")
            return []
        except Exception as e:
            print(f"Error loading cache warmup questions: {e}")
            return []


# Global cache instance
response_cache = InMemoryCache()
