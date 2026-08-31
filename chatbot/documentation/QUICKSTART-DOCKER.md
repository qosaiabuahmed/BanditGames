# Quick Docker Start Guide

## Summary

Your chatbot is now containerized as a microservice! Two containers work together:

1. **chatbot-api** (Port 7777) - Your FastAPI application
2. **chatbot-ollama** (Port 11434) - AI model engine (internal only)

## Starting Everything

```bash
# First time setup - start containers and download model
docker-compose up -d
docker exec chatbot-ollama ollama pull llama2

# After first time - just start
docker-compose up -d
```

## Testing

```bash
# Health check
curl http://localhost:7777/health

# Ask a question
curl -X POST "http://localhost:7777/chat" \
  -H "Content-Type: application/json" \
  -d '{"question": "What are the rules of Connect Four?"}'

# View API docs
# Open: http://localhost:7777/docs
```

## Calling from Your Web App

### If your web app is in Docker (recommended)

Add to your web app's `docker-compose.yml`:

```yaml
networks:
  chatbot-network:
    external: true

services:
  your-web-app:
    networks:
      - chatbot-network
```

Then call using internal Docker hostname:

```javascript
fetch('http://chatbot-api:7777/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ question: "How do I play?" })
})
```

### If your web app is NOT in Docker

```javascript
fetch('http://localhost:7777/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ question: "How do I play?" })
})
```

## Common Commands

```bash
# View logs
docker-compose logs -f chatbot

# Restart after code changes
docker-compose up -d --build

# Stop everything
docker-compose down

# Check status
docker-compose ps

# List Ollama models
docker exec chatbot-ollama ollama list
```

## Troubleshooting

**Container keeps restarting:**
```bash
docker-compose logs chatbot
```

**Model not found:**
```bash
docker exec chatbot-ollama ollama pull llama2
```

**Port already in use:**
Edit `docker-compose.yml` and change `7777:7777` to `8080:7777` (or any free port)

## Slow First Build?

The first build takes 5-10 minutes because it:
- Downloads Python packages (sentence-transformers is large)
- Compiles dependencies

After the first build, subsequent builds are cached and much faster!

## Full Documentation

See `DOCKER.md` for complete details on production deployment, scaling, and advanced configuration.