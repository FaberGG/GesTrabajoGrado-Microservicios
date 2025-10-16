CONFIGURACIÓN (src/main/resources/application.yml)

server:
port: 8081

spring:
datasource:
url: jdbc:postgresql://localhost:5432/submissiondb
username: postgres
password: postgres
jpa:
hibernate:
ddl-auto: update
show-sql: true
rabbitmq:
host: localhost
port: 5672
username: guest
password: guest

submission:
exchange: submission.exchange
queue: submission.queue
routing-key: submission.created

Si el micro de Notificaciones existe y expone /api/notifications, usa esta forma:

notification:
base-url: http://localhost:8082/api/notifications

Si solo tienes el host, usa:
notification:
base-url: http://localhost:8082
y en NotificationClient usa uri("/api/notifications")

CÓMO EJECUTAR

Levantar infraestructura (RabbitMQ + Postgres):
docker compose up -d

RabbitMQ UI: http://localhost:15672
 (user: guest, pass: guest)

Compilar y correr la app:
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=local
Debe verse: "Tomcat started on port(s): 8081 (http)".

Prueba de vida:
curl http://localhost:8081/ping

(debe responder "pong")

ENDPOINTS

POST /api/submissions

Crea un anteproyecto, guarda en DB, publica evento en RabbitMQ
y (opcional) dispara notificación HTTP.

Request (JSON):
{
"titulo": "Mi Tesis",
"resumen": "Resumen cortico del anteproyecto",
"autoresEmails": ["a@uni.edu
","b@uni.edu
"],
"directorId": 101,
"estudiante1Id": 201,
"estudiante2Id": 202
}

Respuesta 201:
{ "proyectoId": 1, "estado": "EN_PROCESO" }

EJEMPLOS DE LLAMADA

PowerShell:
$body = @{
titulo = "Mi Tesis"
resumen = "Resumen cortico del anteproyecto"
autoresEmails = @("a@uni.edu
","b@uni.edu
")
directorId = 101
estudiante1Id = 201
estudiante2Id = 202
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri http://localhost:8081/api/submissions
-ContentType 'application/json' `
-Body $body

curl (bash/cmd):
curl -X POST http://localhost:8081/api/submissions
 ^
-H "Content-Type: application/json" ^
-d "{ "titulo":"Mi Tesis", "resumen":"Resumen cortico del anteproyecto", "autoresEmails":["a@uni.edu
","b@uni.edu
"], "directorId":101, "estudiante1Id":201, "estudiante2Id":202 }"