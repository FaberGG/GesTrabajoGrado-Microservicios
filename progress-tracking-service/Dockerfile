# Imagen base: Java 21 (puede ser 17 si tu proyecto usa esa versión)
FROM openjdk:21-jdk-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado por Maven al contenedor
COPY target/progress-tracking-service-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto 8080 (el mismo que usa tu aplicación)
EXPOSE 8080

# Comando para ejecutar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
