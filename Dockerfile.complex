# Multi-stage build for optimal size

# Stage 1: Build backend
FROM gradle:8.5-jdk17 AS backend-build
WORKDIR /app
COPY backend/build.gradle.kts backend/settings.gradle.kts backend/gradle.properties ./
COPY backend/src ./src
RUN gradle build --no-daemon

# Stage 2: Build frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 3: Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy backend JAR
COPY --from=backend-build /app/build/libs/*.jar app.jar

# Copy frontend build
COPY --from=frontend-build /app/dist ./static

# Install nginx for serving frontend
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Configure nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Expose ports
EXPOSE 80 8080

# Start script
COPY start.sh ./
RUN chmod +x start.sh

CMD ["./start.sh"] 