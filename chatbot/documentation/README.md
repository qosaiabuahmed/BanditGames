# Board Game Platform Chatbot

A RAG (Retrieval-Augmented Generation) based chatbot that provides support for board game rules and platform assistance. Built with Python, FastAPI, ChromaDB, and Ollama.

## Features

- **Game Rule Retrieval**: Answers questions about board game rules with accurate, context-aware responses
- **Platform Guidance**: Helps users navigate the platform, manage accounts, and understand gameplay features
- **RAG System**: Uses ChromaDB for vector storage and semantic search to find relevant information
- **Local LLM**: Powered by Ollama for privacy-friendly, cost-free AI responses
- **Intelligent Caching**: In-memory LRU cache with TTL for fast responses to common questions
- **REST API**: Easy-to-use API endpoints for integration with any frontend
- **Extensible**: Simple JSON-based knowledge base that can be easily extended with new games and content

## Architecture

```
┌─────────────┐
│   User      │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│         FastAPI REST API            │
│  ┌───────────────────────────────┐  │
│  │     Response Cache (LRU)      │  │
│  └───────────────────────────────┘  │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│          RAG System                 │
│  ┌───────────────┐  ┌────────────┐  │
│  │   ChromaDB    │  │  Ollama    │  │
│  │  (Vectors)    │  │   (LLM)    │  │
│  └───────────────┘  └────────────┘  │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│      Knowledge Base (JSON)          │
│  - Game Rules (Connect Four, etc.)  │
│  - Platform Guidance                │
│  - FAQs                             │
└─────────────────────────────────────┘
```

## Prerequisites

1. **Python 3.8+**
2. **Ollama** - Install from [ollama.ai](https://ollama.ai)
   ```bash
   # After installing Ollama, pull the llama2 model
   ollama pull llama2
   ```

## Installation

1. Clone the repository:
   ```bash
   cd chatbot
   ```

2. Create a virtual environment:
   ```bash
   python -m venv venv

   # On Windows
   venv\Scripts\activate

   # On macOS/Linux
   source venv/bin/activate
   ```

3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

4. Create environment configuration:
   ```bash
   # Copy the example env file
   cp .env.example .env

   # Edit .env if you want to customize settings
   ```

## Usage

### Starting the Server

1. Make sure Ollama is running:
   ```bash
   # Verify Ollama is running
   ollama list
   ```

2. Start the chatbot server:
   ```bash
   python main.py
   ```

   The server will start on `http://localhost:8000`

3. Access the interactive API documentation:
   - Swagger UI: `http://localhost:8000/docs`
   - ReDoc: `http://localhost:8000/redoc`

### Using the API

#### Chat Endpoint

```bash
curl -X POST "http://localhost:8000/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What are the rules of Connect Four?",
    "use_cache": true,
    "n_results": 3
  }'
```

Response:
```json
{
  "response": "Connect Four is a two-player strategy game...",
  "cached": false,
  "sources": [
    {
      "type": "game_overview",
      "game": "Connect Four"
    }
  ],
  "processing_time": 1.234
}
```

#### Example Questions

- "What are the rules of Connect Four?"
- "How do I win at Connect Four?"
- "Can I remove a disc once it's been placed?"
- "What are some strategy tips for Connect Four?"
- "How do I start a game?"
- "How do I reset my password?"

### Using the Test Client

```bash
python test_client.py
```

This will run through several example questions and display responses.

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | API information |
| `/chat` | POST | Ask a question to the chatbot |
| `/health` | GET | Health check |
| `/stats` | GET | System statistics (RAG + cache) |
| `/cache/stats` | GET | Cache statistics |
| `/cache/clear` | POST | Clear response cache |
| `/knowledge/add` | POST | Add new document to knowledge base |
| `/games` | GET | List available games |

## Configuration

Edit `.env` file to customize:

```env
OLLAMA_MODEL=llama2              # LLM model to use
OLLAMA_BASE_URL=http://localhost:11434
CHROMA_PERSIST_DIR=./chroma_db   # Vector database storage
CACHE_SIZE=100                   # Max cached responses
CACHE_TTL=3600                   # Cache time-to-live (seconds)
```

## Adding New Games

To add a new game to the knowledge base:

1. Edit `knowledge_base/game_rules.json`
2. Add your game following the existing structure:

```json
{
  "id": "game-id",
  "name": "Game Name",
  "category": "strategy",
  "players": "2-4",
  "duration": "30 minutes",
  "description": "Game overview...",
  "rules": {
    "setup": ["Step 1", "Step 2"],
    "gameplay": ["Rule 1", "Rule 2"],
    "winning": ["Win condition 1"]
  }
}
```

3. Restart the server to reload the knowledge base

Or use the API endpoint:

```bash
curl -X POST "http://localhost:8000/knowledge/add" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Checkers is a classic board game...",
    "metadata": {
      "type": "game_overview",
      "game": "Checkers"
    }
  }'
```

## Project Structure

```
chatbot/
├── main.py                  # FastAPI application
├── rag_system.py            # RAG implementation
├── cache.py                 # In-memory caching system
├── knowledge_loader.py      # Knowledge base loader
├── config.py                # Configuration management
├── test_client.py           # Example client
├── requirements.txt         # Python dependencies
├── .env.example             # Environment template
├── knowledge_base/
│   └── game_rules.json      # Game rules and platform guidance
└── chroma_db/               # Vector database (auto-generated)
```

## Technology Stack

- **FastAPI**: Modern web framework for building APIs
- **ChromaDB**: Vector database for semantic search
- **Ollama**: Local LLM runtime
- **Sentence Transformers**: Text embedding generation
- **LangChain**: LLM orchestration (optional, for future extensions)

## Performance

- **First query**: ~1-2 seconds (includes embedding generation and LLM inference)
- **Cached queries**: <50ms
- **Memory usage**: ~500MB (includes embedding model and ChromaDB)
- **Disk usage**: ~300MB (models and vector database)

## Future Enhancements

- [ ] Context-aware multi-turn conversations with session management
- [ ] Support for multiple languages
- [ ] Voice input/output integration
- [ ] Game recommendation system
- [ ] User feedback collection for improving responses
- [ ] Integration with game platform's user authentication
- [ ] Real-time game state analysis and hints

## Troubleshooting

### Ollama Connection Error

```
Error: Connection refused to http://localhost:11434
```

**Solution**: Make sure Ollama is running:
```bash
ollama serve
```

### Model Not Found

```
Error: model 'llama2' not found
```

**Solution**: Pull the model:
```bash
ollama pull llama2
```

### Slow First Response

The first query may be slow as it:
1. Loads the embedding model
2. Generates embeddings for the query
3. Initializes the LLM

Subsequent queries will be much faster, especially with caching enabled.

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.
