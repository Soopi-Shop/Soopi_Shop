# ==============================================================================
# Multi-stage Production Dockerfile for Soopi Shop Frontend (Vite + React)
# Stage 1: Build static assets using Node.js
# Stage 2: Serve optimized assets with Nginx Alpine
# ==============================================================================

# ----------------- Stage 1: Builder -----------------
FROM node:22-alpine AS builder

WORKDIR /app

# Copy dependency manifests
COPY package.json package-lock.json ./

# Install dependencies cleanly
RUN npm ci

# Build arguments for Vite environment variables
ARG VITE_API_BASE_URL
ARG VITE_CLERK_PUBLISHABLE_KEY

ENV VITE_API_BASE_URL=${VITE_API_BASE_URL} \
    VITE_CLERK_PUBLISHABLE_KEY=${VITE_CLERK_PUBLISHABLE_KEY}

# Copy source code and config files
COPY . .

# Build production bundle
RUN npm run build

# ----------------- Stage 2: Runtime -----------------
FROM nginx:1.27-alpine AS runner

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build artifacts from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
