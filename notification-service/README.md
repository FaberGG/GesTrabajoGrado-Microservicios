# 📧 Notification Service

Microservicio de notificaciones para el Sistema de Gestión de Trabajos de Grado. Proporciona envío de notificaciones por múltiples canales (email, SMS, etc.) con soporte para plantillas dinámicas y procesamiento síncrono/asíncrono.

## 🎯 Características Principales

- ✅ **Envío Síncrono y Asíncrono** - Flexibilidad según las necesidades del negocio
- ✅ **Múltiples Canales** - Email, SMS, y extensible a otros canales
- ✅ **Plantillas Dinámicas** - Sistema de plantillas parametrizadas por tipo de notificación
- ✅ **Patrón Decorator** - Validación y logging configurables sin modificar el código base
- ✅ **Múltiples Destinatarios** - Soporte para notificar a varios usuarios simultáneamente
- ✅ **Integración con RabbitMQ** - Para procesamiento asíncrono resiliente
- ✅ **Sin Persistencia** - Microservicio ligero enfocado en envío de notificaciones

## 🏗️ Arquitectura

### Patrón Decorator
```
┌─────────────────────────────────────┐
│   LoggingNotifierDecorator          │  ← Logging estructurado
│  ┌──────────────────────────────┐   │
│  │ ValidationNotifierDecorator  │   │  ← Validación de negocio
│  │  ┌───────────────────────┐   │   │
│  │  │ BaseNotifierService   │   │   │  ← Servicio base
│  │  └───────────────────────┘   │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

### Flujo de Procesamiento

**Síncrono (HTTP)**
```
Cliente → NotificationController → Notifier → Envío Directo → Respuesta
```

**Asíncrono (RabbitMQ)**
```
Cliente → NotificationController → RabbitMQ → NotificationConsumer → Notifier → Envío
```

## 📋 Tipos de Notificaciones Soportadas

| Tipo | Descripción | Contexto Requerido |
|------|-------------|-------------------|
| `DOCUMENT_SUBMITTED` | Nuevo documento enviado | `projectTitle`, `documentType`, `submittedBy`, `submissionDate`, `documentVersion` |
| `EVALUATION_COMPLETED` | Evaluación completada | `projectTitle`, `documentType`, `evaluationResult`, `evaluatedBy`, `evaluationDate` |
| `EVALUATOR_ASSIGNED` | Evaluador asignado | `projectTitle`, `documentType`, `directorName`, `dueDate` |
| `STATUS_CHANGED` | Cambio de estado del proyecto | `projectTitle`, `currentStatus`, `previousStatus`, `changeDate` |
| `DEADLINE_REMINDER` | Recordatorio de fecha límite | `projectTitle`, `pendingActivity`, `dueDate`, `daysRemaining` |

## 🔌 Integración para Otros Microservicios

### Submission Service

**Escenario**: Notificar cuando un estudiante envía un documento (Formato A o Anteproyecto)

```java
// Ejemplo: Envío Asíncrono vía RabbitMQ (Recomendado)
NotificationRequest request = new NotificationRequest(
    NotificationType.DOCUMENT_SUBMITTED,
    "email",
    List.of(
        new Recipient("coordinador@unicauca.edu.co", "COORDINATOR"),
        new Recipient("jefe.programa@unicauca.edu.co", "PROGRAM_HEAD")
    ),
    Map.of(
        "projectTitle", "Sistema de Gestión Académica",
        "documentType", "FORMATO_A", // o "ANTEPROYECTO"
        "submittedBy", "Juan Pérez",
        "submissionDate", LocalDateTime.now().toString(),
        "documentVersion", 1
    ),
    null,  // mensaje (usa plantilla por defecto)
    null,  // templateId (usa plantilla por defecto)
    false  // forceFail
);

rabbitTemplate.convertAndSend("notification.queue", request);
```

**Endpoint HTTP** (si necesitas respuesta inmediata):
```http
POST http://localhost:8083/notifications/async
Content-Type: application/json

{
  "notificationType": "DOCUMENT_SUBMITTED",
  "channel": "email",
  "recipients": [
    { "email": "coordinador@unicauca.edu.co", "role": "COORDINATOR" },
    { "email": "jefe.programa@unicauca.edu.co", "role": "PROGRAM_HEAD" }
  ],
  "businessContext": {
    "projectTitle": "Sistema de Gestión Académica",
    "documentType": "FORMATO_A",
    "submittedBy": "Juan Pérez",
    "submissionDate": "2025-10-30T10:30:00",
    "documentVersion": 1
  }
}
```

### Review Service

**Escenario 1**: Notificar evaluación completada

```java
NotificationRequest request = new NotificationRequest(
    NotificationType.EVALUATION_COMPLETED,
    "email",
    List.of(
        new Recipient("estudiante@unicauca.edu.co", "STUDENT"),
        new Recipient("docente.director@unicauca.edu.co", "ADVISOR")
    ),
    Map.of(
        "projectTitle", "Sistema de Gestión Académica",
        "documentType", "ANTEPROYECTO",
        "evaluationResult", "APPROVED", // "APPROVED" | "REJECTED" | "OBSERVATIONS"
        "evaluatedBy", "Dr. María González",
        "evaluationDate", LocalDateTime.now().toString()
    ),
    null,
    null,
    false
);

rabbitTemplate.convertAndSend("notification.queue", request);
```

**Escenario 2**: Notificar asignación de evaluadores

```java
NotificationRequest request = new NotificationRequest(
    NotificationType.EVALUATOR_ASSIGNED,
    "email",
    List.of(
        new Recipient("evaluador1@unicauca.edu.co", "EVALUATOR"),
        new Recipient("evaluador2@unicauca.edu.co", "EVALUATOR")
    ),
    Map.of(
        "projectTitle", "Sistema de Gestión Académica",
        "documentType", "ANTEPROYECTO",
        "directorName", "Dr. Carlos Ruiz",
        "dueDate", "2025-11-15"
    ),
    null,
    null,
    false
);

rabbitTemplate.convertAndSend("notification.queue", request);
```

**HTTP Endpoint**:
```http
POST http://localhost:8083/notifications/async
Content-Type: application/json

{
  "notificationType": "EVALUATION_COMPLETED",
  "channel": "email",
  "recipients": [
    { "email": "estudiante@unicauca.edu.co", "role": "STUDENT" },
    { "email": "docente.director@unicauca.edu.co", "role": "ADVISOR" }
  ],
  "businessContext": {
    "projectTitle": "Sistema de Gestión Académica",
    "documentType": "ANTEPROYECTO",
    "evaluationResult": "APPROVED",
    "evaluatedBy": "Dr. María González",
    "evaluationDate": "2025-10-30T15:00:00"
  }
}
```

### Progress Tracking Service (CQRS Read Model)

**Escenario**: Notificar cambios de estado importantes

```java
NotificationRequest request = new NotificationRequest(
    NotificationType.STATUS_CHANGED,
    "email",
    List.of(
        new Recipient("estudiante@unicauca.edu.co", "STUDENT")
    ),
    Map.of(
        "projectTitle", "Sistema de Gestión Académica",
        "currentStatus", "APROBADO",
        "previousStatus", "EN_REVISION",
        "changeDate", LocalDateTime.now().toString()
    ),
    null,
    null,
    false
);

rabbitTemplate.convertAndSend("notification.queue", request);
```

**HTTP Endpoint**:
```http
POST http://localhost:8083/notifications/async
Content-Type: application/json

{
  "notificationType": "STATUS_CHANGED",
  "channel": "email",
  "recipients": [
    { "email": "estudiante@unicauca.edu.co", "role": "STUDENT" }
  ],
  "businessContext": {
    "projectTitle": "Sistema de Gestión Académica",
    "currentStatus": "APROBADO",
    "previousStatus": "EN_REVISION",
    "changeDate": "2025-10-30T16:00:00"
  }
}
```

## 🧪 Pruebas Rápidas con Postman

### Configuración Inicial

1. **Importar Colección**: Puedes importar el archivo `postman_collection.json` incluido en este proyecto
2. **Variables de Entorno**:
   - `base_url`: `http://localhost:8083`

### ⚠️ IMPORTANTE - Estructura del JSON

**Todos los requests deben incluir TODOS los campos del `NotificationRequest` record:**

```json
{
  "notificationType": "...",      // REQUERIDO: Enum NotificationType
  "channel": "email",             // REQUERIDO: String
  "recipients": [...],            // REQUERIDO: Array con al menos 1 elemento
  "businessContext": {...},       // REQUERIDO: Map con contexto de negocio
  "message": null,                // OPCIONAL: null si no se usa mensaje custom
  "templateId": null,             // OPCIONAL: null para usar template por defecto
  "forceFail": false              // OPCIONAL: false en producción
}
```

**Estructura de cada Recipient:**
```json
{
  "email": "...",    // REQUERIDO: Email válido
  "role": "...",     // OPCIONAL: String (COORDINATOR, TEACHER, STUDENT, etc)
  "name": null       // OPCIONAL: String o null
}
```

**Campos del businessContext por tipo de notificación:**

Los campos requeridos en `businessContext` varían según el `notificationType`:

- **DOCUMENT_SUBMITTED**: `projectTitle`, `documentType`, `submittedBy`, `submissionDate`, `documentVersion`
- **EVALUATION_COMPLETED**: `projectTitle`, `documentType`, `evaluationResult`, `evaluatedBy`, `evaluationDate`
- **EVALUATOR_ASSIGNED**: `projectTitle`, `documentType`, `directorName`, `dueDate`
- **STATUS_CHANGED**: `projectTitle`, `currentStatus`, `previousStatus`, `changeDate`
- **DEADLINE_REMINDER**: `projectTitle`, `pendingActivity`, `dueDate`, `daysRemaining`

> **Nota**: Si omites campos opcionales como `message`, `templateId`, o `forceFail`, debes enviarlos explícitamente como `null` o `false` para evitar errores de deserialización.

### Test 1: Envío Síncrono - Documento Enviado

```http
POST {{base_url}}/notifications
Content-Type: application/json

{
  "notificationType": "DOCUMENT_SUBMITTED",
  "channel": "email",
  "recipients": [
    {
      "email": "coordinador@unicauca.edu.co",
      "role": "COORDINATOR",
      "name": "Dr. Pedro Coordinador"
    },
    {
      "email": "jefe.programa@unicauca.edu.co",
      "role": "PROGRAM_HEAD",
      "name": "Dr. Luis Jefe"
    }
  ],
  "businessContext": {
    "projectTitle": "Implementación de Sistema de Notificaciones",
    "documentType": "FORMATO_A",
    "submittedBy": "Juan Pérez Estudiante",
    "submissionDate": "2025-10-30T10:30:00",
    "documentVersion": 1
  },
  "message": null,
  "templateId": null,
  "forceFail": false
}
```

**Respuesta Esperada** (200 OK):
```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "notificationType": "DOCUMENT_SUBMITTED",
  "status": "SENT",
  "correlationId": "req-123456789",
  "recipientCount": 2,
  "failedRecipients": [],
  "timestamp": "2025-10-29T23:45:00"
}
```

### Test 2: Envío Asíncrono - Evaluación Completada

```http
POST {{base_url}}/notifications/async
Content-Type: application/json

{
  "notificationType": "EVALUATION_COMPLETED",
  "channel": "email",
  "recipients": [
    {
      "email": "estudiante@unicauca.edu.co",
      "role": "STUDENT",
      "name": null
    }
  ],
  "businessContext": {
    "projectTitle": "Implementación de Sistema de Notificaciones",
    "documentType": "ANTEPROYECTO",
    "evaluationResult": "APPROVED",
    "evaluatedBy": "Dra. María González",
    "evaluationDate": "2025-10-30T15:00:00"
  },
  "message": null,
  "templateId": null,
  "forceFail": false
}
```

**Respuesta Esperada** (202 ACCEPTED):
```
(Sin body - procesamiento asíncrono)
```

### Test 3: Asignación de Evaluadores

```http
POST {{base_url}}/notifications
Content-Type: application/json

{
  "notificationType": "EVALUATOR_ASSIGNED",
  "channel": "email",
  "recipients": [
    {
      "email": "evaluador1@unicauca.edu.co",
      "role": "EVALUATOR",
      "name": "Dr. Carlos Ruiz"
    },
    {
      "email": "evaluador2@unicauca.edu.co",
      "role": "EVALUATOR",
      "name": "Dra. Ana Torres"
    }
  ],
  "businessContext": {
    "projectTitle": "Implementación de Sistema de Notificaciones",
    "documentType": "ANTEPROYECTO",
    "directorName": "Dr. Carlos Ruiz",
    "dueDate": "2025-11-15"
  },
  "message": null,
  "templateId": null,
  "forceFail": false
}
```

### Test 4: Cambio de Estado

```http
POST {{base_url}}/notifications/async
Content-Type: application/json

{
  "notificationType": "STATUS_CHANGED",
  "channel": "email",
  "recipients": [
    {
      "email": "estudiante@unicauca.edu.co",
      "role": "STUDENT",
      "name": null
    },
    {
      "email": "director@unicauca.edu.co",
      "role": "ADVISOR",
      "name": null
    }
  ],
  "businessContext": {
    "projectTitle": "Implementación de Sistema de Notificaciones",
    "currentStatus": "APROBADO",
    "previousStatus": "EN_REVISION",
    "changeDate": "2025-10-30T16:00:00"
  },
  "message": null,
  "templateId": null,
  "forceFail": false
}
```

### Test 5: Recordatorio de Fecha Límite

```http
POST {{base_url}}/notifications
Content-Type: application/json

{
  "notificationType": "DEADLINE_REMINDER",
  "channel": "email",
  "recipients": [
    {
      "email": "estudiante@unicauca.edu.co",
      "role": "STUDENT",
      "name": null
    }
  ],
  "businessContext": {
    "projectTitle": "Implementación de Sistema de Notificaciones",
    "pendingActivity": "Entrega de Anteproyecto",
    "dueDate": "2025-11-15",
    "daysRemaining": 16
  },
  "message": null,
  "templateId": null,
  "forceFail": false
}
```

### Test 6: Health Check

```http
GET {{base_url}}/actuator/health
```

**Respuesta Esperada**:
```json
{
  "status": "UP"
}
```

## 🔧 Configuración

### Variables de Entorno

```bash
# Servidor
SERVER_PORT=8083

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# JWT (para autenticación si es necesario)
JWT_SECRET=your-secret-key-here

# Configuración de Email (cuando se implemente envío real)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM=noreply@trabajogrado.com

# Features (Decorators)
NOTIFICATIONS_FEATURES_VALIDATION=true
NOTIFICATIONS_FEATURES_LOGGING=true

# Mock Mode (para desarrollo)
NOTIFICATION_MAIL_MOCK=true
```

### application.yml

```yaml
spring:
  application:
    name: notification-service
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

server:
  port: ${SERVER_PORT:8083}

notification:
  mail:
    mock: ${NOTIFICATION_MAIL_MOCK:true}
  from:
    email: noreply@trabajogrado.com
    name: "Sistema Trabajo de Grado"
```

## 🚀 Instalación y Ejecución

### Requisitos Previos
- Java 21+
- Maven 3.8+
- RabbitMQ 3.x (ejecutándose)

### Ejecución Local

```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd notification-service

# 2. Compilar
mvnw clean install

# 3. Ejecutar
mvnw spring-boot:run
```

### Con Docker

```bash
# Construir imagen
docker build -t notification-service .

# Ejecutar
docker run -p 8083:8083 \
  -e RABBITMQ_HOST=rabbitmq \
  -e RABBITMQ_USERNAME=guest \
  -e RABBITMQ_PASSWORD=guest \
  notification-service
```

### Con Docker Compose

```bash
docker-compose up -d
```

## 📊 Monitoreo y Logs

### Endpoints de Actuator

- Health: `GET /actuator/health`
- Info: `GET /actuator/info`
- Metrics: `GET /actuator/metrics`

### Logs Estructurados

Los logs incluyen:
- **correlationId**: Para trazar requests a través de microservicios
- **notificationType**: Tipo de notificación
- **channel**: Canal de envío
- **recipients**: Destinatarios
- **status**: Estado del envío

Ejemplo:
```json
{
  "timestamp": "2025-10-29T23:45:00",
  "level": "INFO",
  "logger": "LoggingNotifierDecorator",
  "message": "Notification sent successfully",
  "correlationId": "req-123456789",
  "notificationType": "DOCUMENT_SUBMITTED",
  "channel": "email",
  "recipients": "coordinador@unicauca.edu.co, jefe.programa@unicauca.edu.co",
  "status": "SENT"
}
```

## 🔐 Seguridad

### Filtro de Contexto

El servicio incluye un filtro (`RequestContextFilter`) que:
- Genera `correlationId` para cada request
- Extrae `userId` del contexto (si está disponible)
- Propaga información de contexto vía MDC (Mapped Diagnostic Context)

### Validación

- Validación de campos requeridos (JSR-303/Jakarta Validation)
- Validación de negocio (ej: contexto requerido según tipo de notificación)
- Sanitización de parámetros

## 🧩 Extensibilidad

### Agregar Nuevo Tipo de Notificación

1. **Agregar enum**:
```java
public enum NotificationType {
    DOCUMENT_SUBMITTED,
    EVALUATION_COMPLETED,
    // ... existentes
    NEW_TYPE  // ← Nuevo tipo
}
```

2. **Crear plantilla** en `TemplateService`:
```java
private String getNewTypeTemplate() {
    return """
        Nuevo Tipo de Notificación
        
        Proyecto: {{projectTitle}}
        ...
        """;
}
```

3. **Usar desde otros microservicios**:
```java
NotificationRequest request = new NotificationRequest(
    NotificationType.NEW_TYPE,
    "email",
    recipients,
    context,
    null, null, false
);
```

### Agregar Nuevo Canal

Extender `BaseNotifierService.sendToRecipient()` con lógica para el nuevo canal.

## 📚 Modelos de Datos

### NotificationRequest

```java
{
  "notificationType": "DOCUMENT_SUBMITTED",  // Requerido
  "channel": "email",                        // Requerido: "email" | "sms"
  "recipients": [                            // Requerido: al menos 1
    {
      "email": "user@example.com",
      "role": "STUDENT"                      // Opcional
    }
  ],
  "businessContext": {                       // Requerido: Map<String, Object>
    "projectId": "uuid",
    "projectTitle": "string",
    // ... campos específicos del tipo
  },
  "message": "string",                       // Opcional: mensaje custom
  "templateId": "string",                    // Opcional: ID de plantilla
  "forceFail": false                         // Opcional: para testing
}
```

### NotificationResponse

```java
{
  "id": "uuid",
  "notificationType": "DOCUMENT_SUBMITTED",
  "status": "SENT",                          // "SENT" | "PARTIALLY_SENT" | "FAILED"
  "correlationId": "req-123456789",
  "recipientCount": 2,
  "failedRecipients": [],
  "timestamp": "2025-10-29T23:45:00"
}
```

## 🤝 Contribución

1. Fork del proyecto
2. Crear branch para feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push al branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto es parte del Sistema de Gestión de Trabajos de Grado de la Universidad del Cauca.

## 📞 Contacto y Soporte

Para preguntas o soporte, contactar al equipo de desarrollo.

---

**Versión**: 1.0.0  
**Última actualización**: Octubre 2025

