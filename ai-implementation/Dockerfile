# =============================================================================
# Dockerfile for Connect Four AI Service (MCTS)
# =============================================================================

FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Copy requirements
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY src/ /app/src/

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONPATH=/app/src:$PYTHONPATH

# Expose Flask port
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD python -c "import requests; requests.get('http://localhost:5000/health')" || exit 1

# Run the Flask API with gunicorn (4 workers for parallel processing)
CMD ["gunicorn", "--workers=4", "--bind=0.0.0.0:5000", "--timeout=120", "src.api:app"]
