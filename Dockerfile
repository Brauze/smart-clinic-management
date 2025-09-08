# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml file
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jre-slim

# Set the working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --group spring

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the app directory to the spring user
RUN chown -R spring:spring /app

# Switch to the non-root user
USER spring

# Expose the port that the application runs on
EXPOSE 8080

# Set JVM options for optimal performance in containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# Health check to ensure the application is running
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Define the entry point to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Alternative entrypoint without shell (more secure)
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Labels for better maintainability
LABEL maintainer="your-email@example.com"
LABEL version="1.0"
LABEL description="Smart Clinic Management System - Spring Boot Backend"

# Environment variables (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080
