# Multi-stage build para optimizar tamaño de imagen

# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven primero (para aprovechar cache de Docker)
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y empaquetar aplicación
# Skip tests en build de Docker para acelerar; tests deben ejecutarse en CI/CD
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar wget ANTES de crear usuario
RUN apk add --no-cache wget

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio y cambiar ownership DESPUÉS de crear usuario
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copiar JAR desde etapa de build y cambiar ownership
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Cambiar a usuario no-root DESPUÉS de configurar permisos
USER spring:spring

# Exponer puerto del submission service
EXPOSE 8082

# Variables de entorno por defecto (pueden sobrescribirse en docker-compose)
ENV SPRING_PROFILES_ACTIVE="prod" \
    SERVER_PORT="8082"

# Health check para Docker
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Comando de inicio con optimizaciones JVM para contenedores
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
