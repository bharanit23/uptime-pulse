# Multi-stage Dockerfile for UptimePulse Sentinel
# Stage 1: Build the Application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy gradle wrapper and config
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle

# Make wrapper executable & cache dependencies
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build production jar
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add unprivileged system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable jar from builder stage
COPY --from=builder --chown=appuser:appgroup /app/build/libs/uptime-pulse-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
