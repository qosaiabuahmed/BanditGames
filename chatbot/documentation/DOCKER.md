# Docker Deployment Guide

This guide explains how to run the chatbot as a microservice using Docker.

## Architecture

The chatbot runs as a microservice with two main containers:

```
┌─────────────────────────────────────────────────────┐
│                Docker Network                        │
│  ┌──────────────────┐      ┌──────────────────┐    │
│  │  Chatbot API     │      │     Ollama       │    │
│  │  (Port 7777)     │─────▶│  (Port 11434)    │    │
│  │                  │      │                  │    │
│  │  - FastAPI       │      │  - LLM Engine    │    │
│  │  - ChromaDB      │      │  - llama2 model  │    │
│  │  - Cache         │      │                  │    │
│  └──────────────────┘      └──────────────────┘    │
│         │                         │                 │
│         ▼                         ▼                 │
│  ┌──────────────┐         ┌──────────────┐         │
│  │  chroma_db   │         │ ollama_data  │         │
│  │   Volume     │         │   Volume     │         │
│  └──────────────┘         └──────────────┘         │
└─────────────────────────────────────────────────────┘
                      │
                      ▼
              External Web App
           (calls chatbot API)
```

## Services

### 1. Chatbot API
- **Container**: `chatbot-api`
- **Port**: 7777
- **Network**: chatbot-network
- **Purpose**: Main API service that handles chat requests

### 2. Ollama LLM
- **Container**: `chatbot-ollama`
- **Port**: 11434
- **Network**: chatbot-network
- **Purpose**: Local LLM inference engine

## Quick Start

### 1. Start Services

```bash
# Build and start all services
docker-compose up -d

# Check logs
docker-compose logs -f chatbot

# Wait for services to be healthy (may take 1-2 minutes)
```

### 2. Pull the LLM Model

On first run, you need to download the llama2 model:

```bash
docker exec -it chatbot-ollama ollama pull llama2
```

### 3. Test the Service

```bash
# Health check
curl http://localhost:7777/health

# Test chat endpoint
curl -X POST "http://localhost:7777/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What are the rules of Connect Four?",
    "use_cache": true,
    "n_results": 3
  }'
```

## Ports

The following ports are exposed:

| Service | Port | Description | Access |
|---------|------|-------------|--------|
| Chatbot API | 7777 | REST API endpoints | External |
| Ollama | 11434 | LLM inference | Internal only |

### Port Configuration for Multiple Services

If you have other services running, you can change the exposed ports:

```yaml
# In docker-compose.yml
services:
  chatbot:
    ports:
      - "8080:7777"  # External:Internal
```

This would make the chatbot available on port 8080 instead of 7777.

## Environment Variables

Configure the chatbot by editing the `docker-compose.yml`:

```yaml
environment:
  - OLLAMA_MODEL=llama2              # LLM model to use
  - OLLAMA_BASE_URL=http://ollama:11434  # Ollama service URL
  - CHROMA_PERSIST_DIR=/app/chroma_db    # Vector DB storage
  - CACHE_SIZE=100                   # Max cached responses
  - CACHE_TTL=3600                   # Cache TTL in seconds
```

## Volumes

### Persistent Storage

- `ollama_data`: Stores downloaded LLM models (can be large, ~4GB)
- `./chroma_db`: Vector database (mounted from host)
- `./knowledge_base`: Game rules and knowledge (mounted from host)

### Adding/Updating Knowledge

Since `knowledge_base` is mounted from the host, you can:

1. Edit `knowledge_base/game_rules.json` on your host machine
2. Restart the chatbot container:
   ```bash
   docker-compose restart chatbot
   ```

## Integration with Other Services

### Adding to Existing docker-compose.yml

If you already have a `docker-compose.yml` for other services:

1. **Add the chatbot network** to your existing compose file:
   ```yaml
   networks:
     your-existing-network:
       # your existing network config
     chatbot-network:
       external: true
       name: chatbot-network
   ```

2. **Connect your web app** to the chatbot network:
   ```yaml
   services:
     your-web-app:
       networks:
         - your-existing-network
         - chatbot-network
       environment:
         - CHATBOT_API_URL=http://chatbot-api:7777
   ```

3. **Start chatbot services first**:
   ```bash
   # In chatbot directory
   docker-compose up -d

   # In your web app directory
   docker-compose up -d
   ```

### Calling from External Web App

From your web application (running in another container):

```javascript
// Using internal Docker network
const CHATBOT_URL = 'http://chatbot-api:7777';

async function askChatbot(question) {
  const response = await fetch(`${CHATBOT_URL}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      question: question,
      use_cache: true,
      n_results: 3
    })
  });
  return await response.json();
}
```

## Common Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f chatbot
docker-compose logs -f ollama

# Restart chatbot only
docker-compose restart chatbot

# Rebuild after code changes
docker-compose up -d --build chatbot

# Check service health
docker-compose ps

# Access chatbot container shell
docker exec -it chatbot-api /bin/bash

# Check Ollama models
docker exec -it chatbot-ollama ollama list

# Remove everything (including volumes)
docker-compose down -v
```

## API Endpoints

Once running, the chatbot exposes these endpoints on port 7777:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | API information |
| `/health` | GET | Health check |
| `/chat` | POST | Ask a question |
| `/stats` | GET | System statistics |
| `/cache/stats` | GET | Cache statistics |
| `/cache/clear` | POST | Clear cache |
| `/knowledge/add` | POST | Add knowledge |
| `/games` | GET | List available games |
| `/docs` | GET | Interactive API docs |

## Troubleshooting

### Container Won't Start

```bash
# Check logs
docker-compose logs chatbot

# Check if port is already in use
netstat -ano | findstr :7777  # Windows
lsof -i :7777                  # Mac/Linux
```

### Ollama Model Not Found

```bash
# Pull the model
docker exec -it chatbot-ollama ollama pull llama2

# Verify it's installed
docker exec -it chatbot-ollama ollama list
```

### Slow Response Times

First query is always slower (model loading). Subsequent queries should be fast.

```bash
# Check if cache is working
curl http://localhost:7777/cache/stats

# Warm up cache
curl http://localhost:7777/stats
```

### Cannot Connect to Ollama

Check if both containers are on the same network:

```bash
docker network inspect chatbot-network
```

Both `chatbot-api` and `chatbot-ollama` should be listed.

## Production Considerations

### 1. Resource Limits

Add resource constraints in `docker-compose.yml`:

```yaml
services:
  chatbot:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### 2. Security

- Change CORS settings in `main.py` to allow only your domain
- Use environment variables for sensitive config
- Don't expose Ollama port externally (keep it internal)

### 3. Monitoring

Add health check monitoring:

```bash
# Simple health check script
while true; do
  curl -f http://localhost:7777/health || echo "Service down!"
  sleep 60
done
```

### 4. Logging

Configure logging driver in `docker-compose.yml`:

```yaml
services:
  chatbot:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## Scaling

To run multiple chatbot instances:

```bash
docker-compose up -d --scale chatbot=3
```

You'll need a load balancer (nginx, traefik) in front of the instances.

## Backup

Important data to backup:

```bash
# Backup vector database
tar -czf chroma_backup.tar.gz ./chroma_db

# Backup Ollama models (optional, can re-download)
docker run --rm -v chatbot-ollama-data:/data -v $(pwd):/backup \
  ubuntu tar -czf /backup/ollama_backup.tar.gz /data
```

## Updates

To update the chatbot:

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose up -d --build

# Re-pull Ollama image if needed
docker-compose pull ollama
docker-compose up -d ollama
```