#!/bin/bash
# =============================================================================
# Docker Entrypoint Script
# Waits for PostgreSQL to be ready before starting the application
# =============================================================================

set -e

echo "==============================================="
echo "Connect Four - Docker Container Starting"
echo "==============================================="

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c '\q' 2>/dev/null; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

echo "✓ PostgreSQL is ready!"

# Verify tables exist
echo "Checking database tables..."
TABLE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | xargs)

if [ "$TABLE_COUNT" -eq "0" ]; then
  echo "⚠ No tables found. Running schema initialization..."
  PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -f /app/database/schema.sql
  echo "✓ Database schema created"
else
  echo "✓ Database tables exist ($TABLE_COUNT tables found)"
fi

# Run any database migrations if needed
if [ -d "/app/database" ]; then
  echo "Checking for migrations..."
  for migration in /app/database/migration_*.sql; do
    if [ -f "$migration" ]; then
      echo "Applying $(basename $migration)..."
      PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -f "$migration" 2>/dev/null || true
    fi
  done
  echo "✓ Migrations check complete"
fi

echo "==============================================="
echo "Starting Application"
echo "==============================================="

# Execute the command passed to docker run
exec "$@"
