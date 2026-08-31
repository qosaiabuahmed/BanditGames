# ============================================================================
# Stage 1: Build the React application
# ============================================================================
FROM node:20-alpine AS build

WORKDIR /app

# Accept build arguments
ARG VITE_GAME_API_URL=http://localhost:8000
ARG VITE_PLATFORM_API_URL=http://localhost:8083

# Set as environment variables for the build
ENV VITE_GAME_API_URL=$VITE_GAME_API_URL
ENV VITE_PLATFORM_API_URL=$VITE_PLATFORM_API_URL

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --prefer-offline --no-audit

# Copy source code
COPY . .

# Build the application
RUN npm run build

# ============================================================================
# Stage 2: Serve with Nginx
# ============================================================================
FROM nginx:1.27-alpine

# Copy built files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
