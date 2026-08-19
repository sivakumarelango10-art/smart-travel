# ==============================================================================
# SmartTravel Frontend — Production Multi-stage Dockerfile
# ==============================================================================

# Build Stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copy dependency definitions
COPY package*.json ./

# Install clean dependencies
RUN npm ci

# Copy source code
COPY . .

# Build arguments for build-time environment variables
ARG VITE_API_BASE_URL=/api/v1
ARG VITE_WS_BASE_URL=
ARG VITE_SWAGGER_URL=/swagger-ui.html

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_WS_BASE_URL=$VITE_WS_BASE_URL \
    VITE_SWAGGER_URL=$VITE_SWAGGER_URL

# Build production distribution
RUN npm run build

# Runtime Stage
FROM nginx:1.27-alpine AS runner

# Remove default nginx welcome page
RUN rm -rf /usr/share/nginx/html/*

# Copy built distribution from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration with SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
    CMD wget -qO- http://localhost/healthz | grep -q 'healthy' || exit 1

CMD ["nginx", "-g", "daemon off;"]
