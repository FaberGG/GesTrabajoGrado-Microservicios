# Multi-stage build: Build with Maven then run on JRE

# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven descriptor first to leverage cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for container healthchecks (used in docker-compose)
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR from builder stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port (overridable via SERVER_PORT)
EXPOSE 8085

# Optional JVM opts
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
