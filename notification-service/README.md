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
- ✅ **Trazabilidad con Correlation ID** - Logging estructurado para seguimiento de notificaciones

---

## 🗂️ Tipos de Notificaciones Soportadas

| Tipo | Descripción | Casos de Uso en el PMV | Contexto Requerido |
|------|-------------|------------------------|-------------------|
| `DOCUMENT_SUBMITTED` | Nuevo documento enviado | RF2, RF4, RF6: Notificar al coordinador/jefe cuando se sube Formato A o Anteproyecto | `projectTitle`, `documentType`, `submittedBy`, `submissionDate`, `documentVersion` |
| `EVALUATION_COMPLETED` | Evaluación completada | RF3: Notificar a docentes/estudiantes sobre evaluación de Formato A | `projectTitle`, `documentType`, `evaluationResult`, `evaluatedBy`, `evaluationDate` |
| `EVALUATOR_ASSIGNED` | Evaluador asignado | RF7: Notificar a evaluadores asignados para anteproyecto | `projectTitle`, `documentType`, `directorName`, `dueDate` |
| `STATUS_CHANGED` | Cambio de estado del proyecto | RF5: Cuando cambia el estado visible para el estudiante | `projectTitle`, `currentStatus`, `previousStatus`, `changeDate` |
| `DEADLINE_REMINDER` | Recordatorio de fecha límite | Futuro: Recordatorios automáticos | `projectTitle`, `pendingActivity`, `dueDate`, `daysRemaining` |

---

## 🏗️ Arquitectura de Integración

### Topología de Red Docker

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network (Bridge)                   │
│                                                              │
│  ┌────────────────┐      ┌──────────────────────┐          │
│  │ Submission     │──┐   │   RabbitMQ           │          │
│  │ Service        │  │   │   (Compartido)       │          │
│  └────────────────┘  │   │                      │          │
│                      ├──▶│  Queue:              │          │
│  ┌────────────────┐  │   │  notifications.q     │          │
│  │ Review         │──┤   └──────────────────────┘          │
│  │ Service        │  │            │                         │
│  └────────────────┘  │            │                         │
│                      │            ▼                         │
│  ┌────────────────┐  │   ┌──────────────────────┐          │
│  │ Progress       │──┘   │  Notification        │          │
│  │ Tracking       │      │  Service             │          │
│  └────────────────┘      │  (Consumer)          │          │
│                          └──────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

**🔑 Decisión Arquitectónica:**
- ✅ **Una sola instancia de RabbitMQ** para todos los microservicios
- ✅ Cada microservicio publica mensajes a `notifications.q`
- ✅ Solo Notification Service consume de esta cola
- ✅ Ventajas:
    - Menor overhead de infraestructura
    - Configuración centralizada
    - Más fácil de monitorear y escalar
    - Consistencia en el manejo de mensajes

---

## 📌 Integración desde Otros Microservicios

### Configuración Requerida en `docker-compose.yml`

```yaml
version: '3.8'

networks:
  microservices-network:
    driver: bridge

services:
  # ═══════════════════════════════════════════════════════════
  # RabbitMQ - COMPARTIDO POR TODOS LOS MICROSERVICIOS
  # ═══════════════════════════════════════════════════════════
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: rabbitmq-shared
    restart: unless-stopped
    ports:
      - "5672:5672"      # AMQP
      - "15672:15672"    # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5

  # ═══════════════════════════════════════════════════════════
  # NOTIFICATION SERVICE (Consumer)
  # ═══════════════════════════════════════════════════════════
  notification-service:
    build:
      context: ./notification-service
      dockerfile: Dockerfile
    container_name: notification-service
    restart: unless-stopped
    ports:
      - "8083:8083"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_PORT=5672
      - SPRING_RABBITMQ_USERNAME=guest
      - SPRING_RABBITMQ_PASSWORD=guest
      - NOTIFICATION_MAIL_MOCK=true
    depends_on:
      rabbitmq:
        condition: service_healthy
    networks:
      - microservices-network

  # ═══════════════════════════════════════════════════════════
  # SUBMISSION SERVICE (Producer)
  # ═══════════════════════════════════════════════════════════
  submission-service:
    build:
      context: ./submission-service
      dockerfile: Dockerfile
    container_name: submission-service
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_PORT=5672
      - SPRING_RABBITMQ_USERNAME=guest
      - SPRING_RABBITMQ_PASSWORD=guest
    depends_on:
      rabbitmq:
        condition: service_healthy
    networks:
      - microservices-network

  # ═══════════════════════════════════════════════════════════
  # REVIEW SERVICE (Producer)
  # ═══════════════════════════════════════════════════════════
  review-service:
    build:
      context: ./review-service
      dockerfile: Dockerfile
    container_name: review-service
    restart: unless-stopped
    ports:
      - "8082:8082"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_PORT=5672
      - SPRING_RABBITMQ_USERNAME=guest
      - SPRING_RABBITMQ_PASSWORD=guest
    depends_on:
      rabbitmq:
        condition: service_healthy
    networks:
      - microservices-network

volumes:
  rabbitmq-data:
    driver: local

networks:
  microservices-network:
    driver: bridge
```

---

## 🔌 Configuración en Microservicios Productores

### 1️⃣ Dependencias de Maven (`pom.xml`)

```xml
<dependencies>
    <!-- Spring AMQP para RabbitMQ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    
    <!-- Jackson para serialización JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

### 2️⃣ Configuración de RabbitMQ (`application.yml`)

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

### 3️⃣ Clase de Configuración de RabbitMQ

```java
package com.yourproject.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String NOTIFICATIONS_QUEUE = "notifications.q";
    
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
    
    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFICATIONS_QUEUE, true); // durable
    }
}
```

### 4️⃣ DTOs para Notificaciones

**IMPORTANTE:** Estos DTOs deben ser **idénticos** a los del Notification Service:

```java
// NotificationRequest.java
package com.yourproject.dto;

import java.util.List;
import java.util.Map;

public record NotificationRequest(
    NotificationType notificationType,
    String channel,
    List<Recipient> recipients,
    Map<String, Object> businessContext,
    String message,
    String templateId,
    boolean forceFail
) {}

// NotificationType.java
public enum NotificationType {
    DOCUMENT_SUBMITTED,
    EVALUATION_COMPLETED,
    STATUS_CHANGED,
    EVALUATOR_ASSIGNED,
    DEADLINE_REMINDER
}

// Recipient.java
public record Recipient(
    String email,
    String role,
    String name
) {
    public Recipient(String email) {
        this(email, null, null);
    }
}
```

---

## 🚀 Casos de Uso: Integración por Requisito Funcional

### **RF2 & RF4: Notificar al Coordinador cuando se Sube Formato A**

**Escenario:** Submission Service envía Formato A (versión 1, 2 o 3)

```java
package com.submission.service;

import com.submission.dto.NotificationRequest;
import com.submission.dto.NotificationType;
import com.submission.dto.Recipient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FormatoASubmissionService {
    
    private final RabbitTemplate rabbitTemplate;
    private static final String NOTIFICATIONS_QUEUE = "notifications.q";
    
    public FormatoASubmissionService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Envía notificación al coordinador después de subir Formato A
     */
    public void notifyCoordinatorFormatoASubmitted(
            String projectTitle,
            int documentVersion,
            String submittedByName,
            String coordinatorEmail
    ) {
        NotificationRequest notification = new NotificationRequest(
            NotificationType.DOCUMENT_SUBMITTED,
            "email",
            List.of(new Recipient(coordinatorEmail, "COORDINATOR", null)),
            Map.of(
                "projectTitle", projectTitle,
                "documentType", "FORMATO_A",
                "submittedBy", submittedByName,
                "submissionDate", LocalDateTime.now().toString(),
                "documentVersion", documentVersion
            ),
            null,  // Usa plantilla por defecto
            null,  // Usa template ID por defecto
            false  // No forzar fallo
        );
        
        rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notification);
    }
}
```

**Uso desde el Controller:**

```java
@PostMapping("/formato-a")
public ResponseEntity<FormatoAResponse> submitFormatoA(
        @RequestBody FormatoARequest request,
        @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Guardar Formato A en base de datos
    FormatoA formatoA = formatoAService.save(request);
    
    // 2. Obtener email del coordinador
    String coordinatorEmail = userService.getCoordinatorEmail(request.programId());
    
    // 3. Enviar notificación ASÍNCRONA
    notificationService.notifyCoordinatorFormatoASubmitted(
        formatoA.getProjectTitle(),
        formatoA.getVersion(),
        userDetails.getUsername(),
        coordinatorEmail
    );
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

### **RF3: Notificar a Docentes/Estudiantes sobre Evaluación Completada**

**Escenario:** Review Service evalúa Formato A y notifica resultado

```java
package com.review.service;

import com.review.dto.NotificationRequest;
import com.review.dto.NotificationType;
import com.review.dto.Recipient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationNotificationService {
    
    private final RabbitTemplate rabbitTemplate;
    private static final String NOTIFICATIONS_QUEUE = "notifications.q";
    
    public EvaluationNotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Notifica a docentes y estudiantes sobre evaluación completada
     */
    public void notifyEvaluationCompleted(
            String projectTitle,
            String evaluationResult,  // "APPROVED", "REJECTED", "OBSERVATIONS"
            String evaluatedByName,
            List<String> teacherEmails,
            List<String> studentEmails
    ) {
        // Crear lista de destinatarios
        List<Recipient> recipients = new ArrayList<>();
        teacherEmails.forEach(email -> 
            recipients.add(new Recipient(email, "TEACHER", null))
        );
        studentEmails.forEach(email -> 
            recipients.add(new Recipient(email, "STUDENT", null))
        );
        
        NotificationRequest notification = new NotificationRequest(
            NotificationType.EVALUATION_COMPLETED,
            "email",
            recipients,
            Map.of(
                "projectTitle", projectTitle,
                "documentType", "FORMATO_A",
                "evaluationResult", evaluationResult,
                "evaluatedBy", evaluatedByName,
                "evaluationDate", LocalDateTime.now().toString(),
                "observations", evaluationResult.equals("OBSERVATIONS") 
                    ? "Revisar observaciones en el sistema" 
                    : ""
            ),
            null,
            null,
            false
        );
        
        rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notification);
    }
}
```

**Uso desde el Controller:**

```java
@PostMapping("/evaluations/{formatoAId}")
public ResponseEntity<EvaluationResponse> evaluateFormatoA(
        @PathVariable String formatoAId,
        @RequestBody EvaluationRequest request,
        @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Guardar evaluación
    Evaluation evaluation = evaluationService.save(formatoAId, request);
    
    // 2. Obtener emails de involucrados
    FormatoA formatoA = formatoAService.findById(formatoAId);
    List<String> teacherEmails = formatoA.getTeachers().stream()
        .map(Teacher::getEmail)
        .toList();
    List<String> studentEmails = formatoA.getStudents().stream()
        .map(Student::getEmail)
        .toList();
    
    // 3. Enviar notificación ASÍNCRONA
    notificationService.notifyEvaluationCompleted(
        formatoA.getProjectTitle(),
        evaluation.getResult(),
        userDetails.getUsername(),
        teacherEmails,
        studentEmails
    );
    
    return ResponseEntity.ok(response);
}
```

---

### **RF6: Notificar al Jefe de Departamento cuando se Sube Anteproyecto**

**Escenario:** Submission Service envía Anteproyecto

```java
public void notifyDepartmentHeadAnteproyectoSubmitted(
        String projectTitle,
        String submittedByName,
        String departmentHeadEmail
) {
    NotificationRequest notification = new NotificationRequest(
        NotificationType.DOCUMENT_SUBMITTED,
        "email",
        List.of(new Recipient(departmentHeadEmail, "DEPARTMENT_HEAD", null)),
        Map.of(
            "projectTitle", projectTitle,
            "documentType", "ANTEPROYECTO",
            "submittedBy", submittedByName,
            "submissionDate", LocalDateTime.now().toString(),
            "documentVersion", 1
        ),
        null,
        null,
        false
    );
    
    rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notification);
}
```

---

### **RF7: Notificar a Evaluadores Asignados**

**Escenario:** Review Service asigna evaluadores a un anteproyecto

```java
public void notifyEvaluatorsAssigned(
        String projectTitle,
        String directorName,
        LocalDate dueDate,
        List<String> evaluatorEmails
) {
    List<Recipient> recipients = evaluatorEmails.stream()
        .map(email -> new Recipient(email, "EVALUATOR", null))
        .toList();
    
    NotificationRequest notification = new NotificationRequest(
        NotificationType.EVALUATOR_ASSIGNED,
        "email",
        recipients,
        Map.of(
            "projectTitle", projectTitle,
            "documentType", "ANTEPROYECTO",
            "directorName", directorName,
            "dueDate", dueDate.toString()
        ),
        null,
        null,
        false
    );
    
    rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notification);
}
```

---

## 📊 DTOs y Respuestas

### NotificationRequest (Completo)

```json
{
  "notificationType": "DOCUMENT_SUBMITTED",
  "channel": "email",
  "recipients": [
    {
      "email": "coordinador@unicauca.edu.co",
      "role": "COORDINATOR",
      "name": null
    }
  ],
  "businessContext": {
    "projectTitle": "Sistema de Gestión Académica",
    "documentType": "FORMATO_A",
    "submittedBy": "Juan Pérez",
    "submissionDate": "2025-10-30T10:30:00",
    "documentVersion": 1
  },
  "message": null,
  "templateId": null,
  "forceFail": false
}
```

### NotificationResponse (Síncrono - 200 OK)

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "notificationType": "DOCUMENT_SUBMITTED",
  "status": "SENT",
  "correlationId": "req-123456789",
  "recipientCount": 1,
  "failedRecipients": [],
  "timestamp": "2025-10-30T10:30:15"
}
```

**Posibles valores de `status`:**
- `"SENT"`: Todos los destinatarios recibieron la notificación
- `"PARTIALLY_SENT"`: Algunos destinatarios fallaron
- `"FAILED"`: Todos los destinatarios fallaron
- `"QUEUED"`: Solo para respuestas asíncronas (en realidad el endpoint `/async` retorna 202 sin body)

### Respuesta Asíncrona (202 ACCEPTED)

```http
HTTP/1.1 202 Accepted
X-Correlation-Id: req-123456789
```

**Sin body**. La notificación se procesará en background.

---

## 🧪 Pruebas con Postman/cURL

### Test 1: Formato A Primera Versión (RF2)

```bash
curl -X POST http://localhost:8083/notifications/async \
  -H "Content-Type: application/json" \
  -d '{
    "notificationType": "DOCUMENT_SUBMITTED",
    "channel": "email",
    "recipients": [
      {
        "email": "coordinador@unicauca.edu.co",
        "role": "COORDINATOR",
        "name": null
      }
    ],
    "businessContext": {
      "projectTitle": "Sistema de Gestión de Notificaciones",
      "documentType": "FORMATO_A",
      "submittedBy": "Juan Pérez Docente",
      "submissionDate": "2025-10-30T10:30:00",
      "documentVersion": 1
    },
    "message": null,
    "templateId": null,
    "forceFail": false
  }'
```

**Respuesta esperada:** `202 Accepted`

**Logs del Notification Service:**
```
📧 [EMAIL MOCK ASYNC] Enviando correo a: coordinador@unicauca.edu.co (COORDINATOR)
   Asunto: DOCUMENT_SUBMITTED - Sistema de Gestión de Notificaciones
   Mensaje:
   Estimado(a) Coordinador(a),
   
   Se ha recibido un nuevo documento para revisión:
   
   Proyecto: Sistema de Gestión de Notificaciones
   Tipo de documento: FORMATO_A
   Versión: 1
   Presentado por: Juan Pérez Docente
   Fecha de envío: 2025-10-30T10:30:00
   ...
```

---

### Test 2: Evaluación Completada (RF3)

```bash
curl -X POST http://localhost:8083/notifications/async \
  -H "Content-Type: application/json" \
  -d '{
    "notificationType": "EVALUATION_COMPLETED",
    "channel": "email",
    "recipients": [
      {
        "email": "docente@unicauca.edu.co",
        "role": "TEACHER",
        "name": null
      },
      {
        "email": "estudiante@unicauca.edu.co",
        "role": "STUDENT",
        "name": null
      }
    ],
    "businessContext": {
      "projectTitle": "Sistema de Gestión de Notificaciones",
      "documentType": "FORMATO_A",
      "evaluationResult": "APPROVED",
      "evaluatedBy": "Dra. María González Coordinadora",
      "evaluationDate": "2025-10-30T15:00:00",
      "observations": ""
    },
    "message": null,
    "templateId": null,
    "forceFail": false
  }'
```

---

### Test 3: Formato A Tercera Versión (RF4)

```bash
curl -X POST http://localhost:8083/notifications/async \
  -H "Content-Type: application/json" \
  -d '{
    "notificationType": "DOCUMENT_SUBMITTED",
    "channel": "email",
    "recipients": [
      {
        "email": "coordinador@unicauca.edu.co",
        "role": "COORDINATOR",
        "name": null
      }
    ],
    "businessContext": {
      "projectTitle": "Sistema de Gestión de Notificaciones",
      "documentType": "FORMATO_A",
      "submittedBy": "Juan Pérez Docente",
      "submissionDate": "2025-11-05T11:00:00",
      "documentVersion": 3
    },
    "message": null,
    "templateId": null,
    "forceFail": false
  }'
```

---

## 🔒 Seguridad y Mejores Prácticas

### 1️⃣ Validación de Inputs

✅ Ya implementado con `@Valid` y Jakarta Validation en `NotificationRequest`

### 2️⃣ Manejo de Errores

```java
// En microservicios productores
try {
    rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notification);
    log.info("Notification queued successfully");
} catch (AmqpException e) {
    log.error("Failed to queue notification", e);
    // Estrategia: Log y continuar (no fallar la operación principal)
    // Opción alternativa: Implementar fallback local o retry
}
```

### 3️⃣ Correlation ID

```java
// Propagar Correlation ID desde otros microservicios
import org.slf4j.MDC;

String correlationId = MDC.get("correlationId");
if (correlationId == null) {
    correlationId = UUID.randomUUID().toString();
}

MessagePostProcessor processor = message -> {
    message.getMessageProperties().setHeader("X-Correlation-Id", correlationId);
    return message;
};

rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, notification, processor);
```

### 4️⃣ Reintentos y Dead Letter Queue

✅ Ya configurado en `NotificationConsumer`:
- 1 reintento con delay de 5 segundos
- Luego envía a DLQ (`notifications.dlq`)

**Monitoreo de DLQ:**

```bash
# Acceder a RabbitMQ Management
http://localhost:15672

# User: guest
# Password: guest

# Ver mensajes en Dead Letter Queue
# Queues → notifications.dlq → Get messages
```

---

## 📈 Monitoreo y Observabilidad

### Logs Estructurados

El Notification Service genera logs en formato JSON con los siguientes campos:

```json
{
  "timestamp": "2025-10-30T10:30:15.123",
  "level": "INFO",
  "logger": "NOTIFICATION_LOGGER",
  "event": "NOTIFICATION_SENT",
  "type": "EMAIL",
  "recipient": "coordinador@unicauca.edu.co",
  "correlationId": "req-123456789",
  "mode": "ASYNC",
  "status": "SENT"
}
```

### Health Checks

```bash
# Verificar estado del servicio
curl http://localhost:8083/actuator/health

# Respuesta esperada
{
  "status": "UP"
}
```

### Métricas RabbitMQ

```bash
# Management UI
http://localhost:15672

# Métricas importantes:
# - Messages ready (en cola esperando)
# - Messages unacknowledged (siendo procesados)
# - Publish rate (mensajes/seg publicados)
# - Deliver rate (mensajes/seg entregados)
```

---

## ⚠️ Limitaciones y Consideraciones

### 1️⃣ Mock de Email

**Estado actual:** `NOTIFICATION_MAIL_MOCK=true`

Los emails se simulan con logs. Para producción:

1. Agregar dependencias SMTP:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

2. Configurar SMTP en `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

3. Modificar `BaseNotifierService.sendToRecipient()` para enviar emails reales

### 2️⃣ Sin Persistencia

El servicio NO guarda historial de notificaciones. Si necesitas auditoría:

**Opción 1:** Agregar decorador de persistencia
```java
public class PersistenceNotifierDecorator implements Notifier {
    private final Notifier wrapped;
    private final NotificationRepository repository;
    
    @Override
    public NotificationResponse sendSync(NotificationRequest request) {
        NotificationResponse response = wrapped.sendSync(request);
        repository.save(toEntity(request, response));
        return response;
    }
}
```

**Opción 2:** Delegar auditoría al Progress Tracking Service (RECOMENDADO)
- Progress Tracking escucha eventos de RabbitMQ
- Mantiene el historial completo de notificaciones
- Notification Service permanece sin estado

### 3️⃣ Escalabilidad

**Para entornos de alta carga:**

```yaml
# docker-compose.yml
notification-service:
  deploy:
    replicas: 3  # Múltiples instancias consumiendo
  environment:
    - SPRING_RABBITMQ_LISTENER_SIMPLE_CONCURRENCY=5
    - SPRING_RABBITMQ_LISTENER_SIMPLE_MAX_CONCURRENCY=10
```

**RabbitMQ distribuirá mensajes entre instancias automáticamente.**

### 4️⃣ Plantillas Personalizadas

Si necesitas plantillas específicas por programa:

```java
// En businessContext
"programId": "ingenieria-sistemas",
"customTemplate": "formato_a_sistemas"

// En TemplateService
if (context.containsKey("customTemplate")) {
    templateId = (String) context.get("customTemplate");
}
```

---

## 🔧 Troubleshooting

### Problema 1: Mensajes no se consumen

**Síntomas:**
- RabbitMQ muestra mensajes en `notifications.q`
- Notification Service no los procesa

**Solución:**
```bash
# Verificar logs del Notification Service
docker logs notification-service

# Verificar conexión a RabbitMQ
docker exec notification-service curl -f rabbitmq:5672 || echo "No connection"

# Reiniciar servicio
docker restart notification-service
```

### Problema 2: Mensajes van a DLQ inmediatamente

**Síntomas:**
- Todos los mensajes terminan en `notifications.dlq`
- Logs muestran errores de validación

**Solución:**
```bash
# Revisar mensaje en DLQ desde Management UI
# Verificar estructura del JSON
# Asegurar que todos los campos requeridos estén presentes

# Ejemplo de error común:
# - Missing "businessContext.projectTitle"
# - Invalid email format in recipients
```

### Problema 3: Serialization/Deserialization Errors

**Síntomas:**
```
Could not read JSON: Cannot construct instance of NotificationRequest
```

**Solución:**
```java
// Asegurar que los DTOs sean IDÉNTICOS en todos los microservicios
// Verificar:
// 1. Nombres de campos
// 2. Tipos de datos
// 3. Constructores
// 4. Usar records en lugar de clases tradicionales

// ✅ CORRECTO
public record NotificationRequest(
    NotificationType notificationType,
    String channel,
    List<Recipient> recipients,
    Map<String, Object> businessContext,
    String message,
    String templateId,
    boolean forceFail
) {}

// ❌ INCORRECTO (nombres diferentes)
public record NotificationRequest(
    NotificationType type,  // ← Diferente nombre
    String channelType,     // ← Diferente nombre
    ...
) {}
```

---

## 📚 Ejemplos Completos de Integración

### Ejemplo Completo: Submission Service

```java
// ══════════════════════════════════════════════════════
// 1. Configuration
// ══════════════════════════════════════════════════════
package com.submission.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String NOTIFICATIONS_QUEUE = "notifications.q";
    
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

// ══════════════════════════════════════════════════════
// 2. Notification Service
// ══════════════════════════════════════════════════════
package com.submission.service;

import com.submission.config.RabbitMQConfig;
import com.submission.dto.NotificationRequest;
import com.submission.dto.NotificationType;
import com.submission.dto.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private final RabbitTemplate rabbitTemplate;
    
    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Notifica documento enviado (Formato A o Anteproyecto)
     */
    public void notifyDocumentSubmitted(
            String projectTitle,
            String documentType,  // "FORMATO_A" | "ANTEPROYECTO"
            int version,
            String submittedByName,
            String recipientEmail,
            String recipientRole     // "COORDINATOR" | "DEPARTMENT_HEAD"
    ) {
        try {
            NotificationRequest notification = new NotificationRequest(
                NotificationType.DOCUMENT_SUBMITTED,
                "email",
                List.of(new Recipient(recipientEmail, recipientRole, null)),
                Map.of(
                    "projectTitle", projectTitle,
                    "documentType", documentType,
                    "submittedBy", submittedByName,
                    "submissionDate", LocalDateTime.now().toString(),
                    "documentVersion", version
                ),
                null,
                null,
                false
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_QUEUE, 
                notification
            );
            
            log.info("Notification queued: {} for {}", documentType, recipientEmail);
            
        } catch (AmqpException e) {
            log.error("Failed to queue notification for document submission", e);
            // No fallar la operación principal si la notificación falla
        }
    }
}

// ══════════════════════════════════════════════════════
// 3. Controller con Integración
// ══════════════════════════════════════════════════════
package com.submission.controller;

import com.submission.dto.FormatoARequest;
import com.submission.dto.FormatoAResponse;
import com.submission.service.FormatoAService;
import com.submission.service.NotificationPublisher;
import com.submission.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions/formato-a")
public class FormatoAController {
    
    private final FormatoAService formatoAService;
    private final NotificationPublisher notificationPublisher;
    private final UserService userService;
    
    public FormatoAController(
            FormatoAService formatoAService,
            NotificationPublisher notificationPublisher,
            UserService userService) {
        this.formatoAService = formatoAService;
        this.notificationPublisher = notificationPublisher;
        this.userService = userService;
    }
    
    /**
     * RF2: Subir Formato A (primera versión)
     * RF4: Subir nueva versión del Formato A
     */
    @PostMapping
    public ResponseEntity<FormatoAResponse> submitFormatoA(
            @RequestBody FormatoARequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. Guardar Formato A
        FormatoAResponse response = formatoAService.submit(request, userDetails);
        
        // 2. Obtener email del coordinador
        String coordinatorEmail = userService.getCoordinatorEmailByProgram(
            request.programId()
        );
        
        // 3. Enviar notificación ASÍNCRONA
        notificationPublisher.notifyDocumentSubmitted(
            response.projectTitle(),
            "FORMATO_A",
            response.version(),
            userDetails.getUsername(),
            coordinatorEmail,
            "COORDINATOR"
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * RF6: Subir Anteproyecto
     */
    @PostMapping("/anteproyecto")
    public ResponseEntity<AnteproyectoResponse> submitAnteproyecto(
            @RequestBody AnteproyectoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. Verificar que Formato A esté aprobado
        if (!formatoAService.isApproved(request.formatoAId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // 2. Guardar Anteproyecto
        AnteproyectoResponse response = formatoAService.submitAnteproyecto(
            request, 
            userDetails
        );
        
        // 3. Obtener email del jefe de departamento
        String departmentHeadEmail = userService.getDepartmentHeadEmail(
            request.programId()
        );
        
        // 4. Enviar notificación ASÍNCRONA
        notificationPublisher.notifyDocumentSubmitted(
            response.projectTitle(),
            "ANTEPROYECTO",
            1,  // Primera versión
            userDetails.getUsername(),
            departmentHeadEmail,
            "DEPARTMENT_HEAD"
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

### Ejemplo Completo: Review Service

```java
// ══════════════════════════════════════════════════════
// Review Service - Notification Publisher
// ══════════════════════════════════════════════════════
package com.review.service;

import com.review.config.RabbitMQConfig;
import com.review.dto.NotificationRequest;
import com.review.dto.NotificationType;
import com.review.dto.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReviewNotificationPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(ReviewNotificationPublisher.class);
    private final RabbitTemplate rabbitTemplate;
    
    public ReviewNotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * RF3: Notificar evaluación completada
     */
    public void notifyEvaluationCompleted(
            String projectTitle,
            String evaluationResult,  // "APPROVED" | "REJECTED" | "OBSERVATIONS"
            String evaluatedByName,
            List<String> teacherEmails,
            List<String> studentEmails,
            String observations
    ) {
        try {
            // Crear lista de destinatarios
            List<Recipient> recipients = new ArrayList<>();
            teacherEmails.forEach(email -> 
                recipients.add(new Recipient(email, "TEACHER", null))
            );
            studentEmails.forEach(email -> 
                recipients.add(new Recipient(email, "STUDENT", null))
            );
            
            NotificationRequest notification = new NotificationRequest(
                NotificationType.EVALUATION_COMPLETED,
                "email",
                recipients,
                Map.of(
                    "projectTitle", projectTitle,
                    "documentType", "FORMATO_A",
                    "evaluationResult", evaluationResult,
                    "evaluatedBy", evaluatedByName,
                    "evaluationDate", LocalDateTime.now().toString(),
                    "observations", observations != null ? observations : ""
                ),
                null,
                null,
                false
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_QUEUE, 
                notification
            );
            
            log.info("Evaluation notification queued for project: {}", projectTitle);
            
        } catch (AmqpException e) {
            log.error("Failed to queue evaluation notification", e);
        }
    }
    
    /**
     * RF7: Notificar asignación de evaluadores
     */
    public void notifyEvaluatorsAssigned(
            String projectTitle,
            String directorName,
            String dueDate,
            List<String> evaluatorEmails
    ) {
        try {
            List<Recipient> recipients = evaluatorEmails.stream()
                .map(email -> new Recipient(email, "EVALUATOR", null))
                .toList();
            
            NotificationRequest notification = new NotificationRequest(
                NotificationType.EVALUATOR_ASSIGNED,
                "email",
                recipients,
                Map.of(
                    "projectTitle", projectTitle,
                    "documentType", "ANTEPROYECTO",
                    "directorName", directorName,
                    "dueDate", dueDate
                ),
                null,
                null,
                false
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_QUEUE, 
                notification
            );
            
            log.info("Evaluator assignment notification queued for {} evaluators", 
                evaluatorEmails.size());
            
        } catch (AmqpException e) {
            log.error("Failed to queue evaluator assignment notification", e);
        }
    }
}

// ══════════════════════════════════════════════════════
// Review Controller
// ══════════════════════════════════════════════════════
package com.review.controller;

import com.review.dto.EvaluationRequest;
import com.review.dto.EvaluationResponse;
import com.review.dto.EvaluatorAssignmentRequest;
import com.review.service.EvaluationService;
import com.review.service.ReviewNotificationPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    
    private final EvaluationService evaluationService;
    private final ReviewNotificationPublisher notificationPublisher;
    
    public EvaluationController(
            EvaluationService evaluationService,
            ReviewNotificationPublisher notificationPublisher) {
        this.evaluationService = evaluationService;
        this.notificationPublisher = notificationPublisher;
    }
    
    /**
     * RF3: Evaluar Formato A
     */
    @PostMapping("/formato-a/{formatoAId}")
    public ResponseEntity<EvaluationResponse> evaluateFormatoA(
            @PathVariable String formatoAId,
            @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. Guardar evaluación
        EvaluationResponse response = evaluationService.evaluate(
            formatoAId, 
            request, 
            userDetails
        );
        
        // 2. Enviar notificación ASÍNCRONA
        notificationPublisher.notifyEvaluationCompleted(
            response.projectTitle(),
            response.result(),
            userDetails.getUsername(),
            response.teacherEmails(),
            response.studentEmails(),
            response.observations()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * RF7: Asignar evaluadores a anteproyecto
     */
    @PostMapping("/anteproyecto/{anteproyectoId}/evaluators")
    public ResponseEntity<Void> assignEvaluators(
            @PathVariable String anteproyectoId,
            @RequestBody EvaluatorAssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. Asignar evaluadores
        evaluationService.assignEvaluators(anteproyectoId, request);
        
        // 2. Enviar notificación ASÍNCRONA
        notificationPublisher.notifyEvaluatorsAssigned(
            request.projectTitle(),
            request.directorName(),
            LocalDate.now().plusDays(15).toString(),  // 15 días para evaluar
            request.evaluatorEmails()
        );
        
        return ResponseEntity.ok().build();
    }
}
```

---

## 🎯 Checklist de Integración

### Para cada microservicio que necesite enviar notificaciones:

- [ ] **1. Agregar dependencias Maven**
    - `spring-boot-starter-amqp`
    - `jackson-databind`

- [ ] **2. Crear DTOs idénticos**
    - `NotificationRequest.java`
    - `NotificationType.java`
    - `Recipient.java`

- [ ] **3. Configurar RabbitMQ**
    - `application.yml` con credenciales
    - `RabbitMQConfig.java` con Queue y Converter

- [ ] **4. Crear servicio de publicación**
    - `NotificationPublisher.java`
    - Métodos específicos por caso de uso

- [ ] **5. Integrar en controllers**
    - Llamar a `notificationPublisher` después de operaciones exitosas
    - Usar try-catch para no fallar operación principal

- [ ] **6. Configurar Docker Compose**
    - Conectar a red compartida
    - Variables de entorno de RabbitMQ
    - `depends_on: rabbitmq`

- [ ] **7. Probar integración**
    - Ejecutar operación que dispara notificación
    - Verificar logs del Notification Service
    - Revisar RabbitMQ Management UI

---

## 📖 Resumen Ejecutivo

### ✅ Estado del Microservicio

El Notification Service está **listo para producción** para el PMV con las siguientes características:

**Fortalezas:**
- ✅ Arquitectura limpia con patrón Decorator
- ✅ Soporte para notificaciones síncronas y asíncronas
- ✅ Sistema de plantillas dinámicas
- ✅ Reintentos automáticos y DLQ
- ✅ Logging estructurado con Correlation ID
- ✅ Health checks y monitoreo

**Limitaciones:**
- ⚠️ Mock de email (requiere SMTP real para producción)
- ⚠️ Sin persistencia de historial (delegar a Progress Tracking)

### 🎯 Cobertura de Requisitos Funcionales

| RF | Descripción | Estado | Tipo de Notificación |
|----|-------------|--------|---------------------|
| RF2 | Notificar coordinador al subir Formato A | ✅ Cubierto | `DOCUMENT_SUBMITTED` |
| RF3 | Notificar evaluación completada | ✅ Cubierto | `EVALUATION_COMPLETED` |
| RF4 | Notificar nueva versión Formato A | ✅ Cubierto | `DOCUMENT_SUBMITTED` |
| RF5 | Ver estado (notificación de cambios) | ✅ Cubierto | `STATUS_CHANGED` |
| RF6 | Notificar jefe al subir Anteproyecto | ✅ Cubierto | `DOCUMENT_SUBMITTED` |
| RF7 | Notificar evaluadores asignados | ✅ Cubierto | `EVALUATOR_ASSIGNED` |

### 🏗️ Arquitectura Recomendada

```
                    ┌─────────────────────┐
                    │   RabbitMQ          │
                    │   (Compartido)      │
                    └──────────┬──────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  Submission   │     │    Review     │     │   Progress    │
│   Service     │     │    Service    │     │   Tracking    │
│  (Producer)   │     │  (Producer)   │     │  (Producer)   │
└───────────────┘     └───────────────┘     └───────────────┘
        │                      │                      │
        └──────────────────────┼──────────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Notification       │
                    │  Service            │
                    │  (Consumer)         │
                    └─────────────────────┘
```

**Decisión:** ✅ **Una sola instancia de RabbitMQ para todos los microservicios**

---

## 📞 Contacto y Soporte

Para preguntas sobre integración o problemas:
1. Revisar logs estructurados en Notification Service
2. Consultar RabbitMQ Management UI (`http://localhost:15672`)
3. Verificar mensajes en DLQ para debugging

---

**Versión**: 1.0.0  
**Última actualización**: Octubre 2025  
**Estado**: ✅ Listo para PMV