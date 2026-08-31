# Quick Start Guide

Get the chatbot running in 5 minutes!

## Step 1: Install Ollama

Download and install Ollama from [ollama.ai](https://ollama.ai)

### Windows
Download the installer and run it.

### macOS
```bash
curl https://ollama.ai/install.sh | sh
```

### Linux
```bash
curl https://ollama.ai/install.sh | sh
```

## Step 2: Pull the LLM Model

```bash
ollama pull llama2
```

This downloads the llama2 model (~4GB). You only need to do this once.

## Step 3: Start the Chatbot

### Windows
Double-click `start.bat` or run:
```bash
start.bat
```

### macOS/Linux
```bash
chmod +x start.sh
./start.sh
```

The script will:
1. Create a Python virtual environment
2. Install all dependencies
3. Start the server on `http://localhost:8000`

## Step 4: Test the Chatbot

### Option 1: Use the Web Interface

Open your browser and go to:
```
http://localhost:8000/docs
```

Click on "POST /chat", then "Try it out", and enter:
```json
{
  "question": "What are the rules of Connect Four?",
  "use_cache": true,
  "n_results": 3
}
```

Click "Execute" to see the response!

### Option 2: Use the Test Client

In a new terminal:
```bash
# Activate the virtual environment first
# Windows: venv\Scripts\activate
# macOS/Linux: source venv/bin/activate

python test_client.py
```

### Option 3: Use curl

```bash
curl -X POST "http://localhost:8000/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How do I win at Connect Four?",
    "use_cache": true,
    "n_results": 3
  }'
```

## Example Questions to Try

- "What are the rules of Connect Four?"
- "How do I win at Connect Four?"
- "Can I remove a disc once it's placed?"
- "What are some strategy tips?"
- "How do I start a game on the platform?"
- "How do I reset my password?"

## Troubleshooting

### "Connection refused to http://localhost:11434"

Ollama is not running. Start it:
```bash
ollama serve
```

### "Model 'llama2' not found"

Pull the model:
```bash
ollama pull llama2
```

### Slow first response

This is normal! The first query loads the embedding model and initializes everything. Subsequent queries will be much faster.

## What's Next?

- Read the full [README.md](README.md) for detailed documentation
- Add new games to `knowledge_base/game_rules.json`
- Integrate with your frontend using the REST API
- Customize settings in `.env`

## Architecture Overview

```
Your Question → FastAPI → Cache Check → RAG System
                            ↓
                          Cache Hit?
                            ↓
                    Yes ←   ↓   → No
                     ↓              ↓
                  Return      ChromaDB Search
                  Cached   →  Ollama Generate
                  Response →  Cache & Return
```

Enjoy your chatbot!
