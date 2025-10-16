# Multi-stage build para optimizar tamaño de imagen

# Etapa 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build

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

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto del gateway
EXPOSE 8080

# Variables de entorno por defecto (pueden sobrescribirse en docker-compose)
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE="docker"

# Health check para Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/gateway/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]