# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Construir la aplicación (skip tests para build más rápido)
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar wget ANTES de crear usuario (para health checks)
RUN apk add --no-cache wget

# Copiar el JAR construido desde el builder
COPY --from=builder /app/target/*.jar app.jar

# Crear usuario no-root
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Crear directorio de logs y cambiar ownership
RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring

# Exponer puerto 8081 (para producción con docker-compose raíz)
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
