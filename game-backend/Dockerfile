# =============================================================================
# Multi-Stage Dockerfile for Connect Four Game Backend
# =============================================================================

# Stage 1: Base image with Python
FROM python:3.11-slim AS base

# Set working directory
WORKDIR /app

# Install system dependencies for PostgreSQL client
RUN apt-get update && apt-get install -y \
    postgresql-client \
    libpq-dev \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Stage 2: Dependencies layer (cached unless requirements.txt changes)
FROM base AS dependencies

# Copy requirements file
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Stage 3: Application layer
FROM dependencies AS application

# Copy application code
COPY src/ /app/src/
COPY database/ /app/database/
COPY docker-entrypoint.sh /docker-entrypoint.sh
COPY project.xml /app/project.xml

# Create directory for any output files
RUN mkdir -p /app/data

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONPATH=/app:$PYTHONPATH

# Make entrypoint script executable
RUN chmod +x /docker-entrypoint.sh

# Expose port (if we add a web API later)
EXPOSE 8000

# Set entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]

# Default command - can be overridden (run FastAPI server)
CMD ["uvicorn", "src.api.api:app", "--host", "0.0.0.0", "--port", "8000"]
