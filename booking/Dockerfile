# Multi-stage Dockerfile for MeetLines Booking Service
# Stage 1: Build using Maven image
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./

# Copy both module poms (required for parent pom to resolve)
COPY auth/pom.xml ./auth/
COPY booking/pom.xml ./booking/

# Copy booking source code
COPY booking/src ./booking/src/

# Build only the booking module
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -pl booking -am -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder stage
COPY --from=builder /app/booking/target/booking-*.jar app.jar

# Expose application port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
