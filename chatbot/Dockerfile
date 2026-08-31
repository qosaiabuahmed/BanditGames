

# Use Python 3.11 slim image for smaller size
FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Set environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY src/ /app/src/
COPY knowledge_base/ /app/knowledge_base/
COPY predefined_cache.json /app/

# Create necessary directories
RUN mkdir -p /app/chroma_db

# Change working directory to src where the Python files are
WORKDIR /app/src

# Expose the application port
EXPOSE 7777

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:7777/health || exit 1

# Run the application (now in src directory)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "7777"]