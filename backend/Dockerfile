# ==============================================================================
# Multi-stage Production Dockerfile for Soopi Shop Backend
# Stage 1: Build JAR using Maven
# Stage 2: Minimal, secure JRE 21 runtime with non-root user
# ==============================================================================

# ----------------- Stage 1: Builder -----------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml and download dependencies to leverage Docker layer caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy project source code
COPY src ./src

# Build optimized executable jar without running tests
RUN mvn clean package -DskipTests -B

# ----------------- Stage 2: Runtime -----------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create non-root system group and user for security compliance
RUN addgroup --system --gid 10001 soopi && \
    adduser --system --uid 10001 --ingroup soopi --no-create-home soopi

# Copy built artifact from builder stage
COPY --from=builder /build/target/soopi-shop-backend-*.jar /app/app.jar

# Adjust ownership
RUN chown -R soopi:soopi /app

# Switch to non-root user
USER soopi:soopi

# Spring Boot default port
EXPOSE 8000

ENV SERVER_PORT=8000 \
    SPRING_MAIN_WEB_APPLICATION_TYPE=servlet

# Container memory and GC tuning flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
