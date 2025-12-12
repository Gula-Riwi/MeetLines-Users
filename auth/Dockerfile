# Multi-stage Dockerfile for MeetLines Auth Service
# Stage 1: Build using Maven image
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom files
COPY pom.xml ./
COPY auth/pom.xml ./auth/

# Copy source code
COPY auth/src ./auth/src/

# Download dependencies and build
RUN mvn clean package -DskipTests -pl auth -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder stage
COPY --from=builder /app/auth/target/auth-*.jar app.jar

# Expose application port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
