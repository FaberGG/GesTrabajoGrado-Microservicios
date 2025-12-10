# 🎓 Sistema de Gestión de Trabajo de Grado - Microservicios

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Event--Driven-blue.svg)](https://martinfowler.com/articles/201701-event-driven.html)
[![CQRS](https://img.shields.io/badge/Pattern-CQRS-purple.svg)](https://martinfowler.com/bliki/CQRS.html)
[![Hexagonal](https://img.shields.io/badge/Architecture-Hexagonal-red.svg)](https://alistair.cockburn.us/hexagonal-architecture/)

Sistema completo basado en microservicios para la gestión de trabajos de grado, implementando **arquitecturas limpias**, **patrones de diseño avanzados** y **comunicación asíncrona basada en eventos**.

---

## 📋 Tabla de Contenidos

1. [Arquitectura General](#-arquitectura-general)
2. [Microservicios](#-microservicios)
3. [Patrones de Diseño](#-patrones-de-diseño-y-arquitecturas)
4. [Comunicación Entre Servicios](#-comunicación-entre-servicios)
5. [Eventos del Sistema](#-eventos-del-sistema)
6. [Inicio Rápido](#-inicio-rápido-con-docker-compose)
7. [Configuración](#-configuración)

---

## 🏗️ Arquitectura General

### Diagrama de Componentes

```
                          ┌─────────────────────┐
                          │   CLIENTE (Web/App) │
                          └──────────┬──────────┘
                                     │
                                     │ HTTP + JWT
                                     ▼
                          ┌─────────────────────┐
                          │  GATEWAY SERVICE    │
                          │  (Puerto 8080)      │
                          │  • Routing          │
                          │  • JWT Validation   │
                          │  • Role-Based Auth  │
                          └──────────┬──────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
              ▼                      ▼                      ▼
    ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
    │ IDENTITY SERVICE│   │SUBMISSION SERVICE│   │ REVIEW SERVICE  │
    │  (Puerto 8081)  │   │  (Puerto 8082)  │   │  (Puerto 8084)  │
    │  • Auth & Users │   │  • Formato A    │   │  • Evaluaciones │
    │  • JWT Token    │   │  • Anteproyectos│   │  • Asignaciones │
    │  • Facade       │   │  • Hexagonal    │   │  • Template     │
    └─────────────────┘   └────────┬─────────┘   └────────┬────────┘
                                   │                      │
                                   │ Publica eventos      │
                                   │ (RabbitMQ)           │
                                   ▼                      ▼
                          ┌──────────────────────────────┐
                          │      RABBITMQ BROKER         │
                          │      (Puertos 5672, 15672)   │
                          │  • formato-a-exchange        │
                          │  • anteproyecto-exchange     │
                          │  • evaluacion-exchange       │
                          └──────────┬───────────────────┘
                                     │
                  ┌──────────────────┼──────────────────┐
                  │                  │                  │
                  ▼                  ▼                  ▼
      ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
      │ NOTIFICATION     │ │ PROGRESS TRACKING│ │   PostgreSQL DBs │
      │   SERVICE        │ │     SERVICE      │ │   (5 databases)  │
      │ (Puerto 8083)    │ │  (Puerto 8085)   │ │   • identity     │
      │ • Email/SMS      │ │  • Event Store   │ │   • submission   │
      │ • Templates      │ │  • Read Model    │ │   • review       │
      │ • Decorator      │ │  • CQRS Query    │ │   • notification │
      └──────────────────┘ └──────────────────┘ │   • tracking     │
                                                 └──────────────────┘
```

### Microservicios de Negocio

| Servicio | Puerto | Responsabilidad | Arquitectura/Patrón |
|----------|--------|-----------------|---------------------|
| **Gateway Service** | 8080 | Punto de entrada único, routing, JWT auth | Spring Cloud Gateway |
| **Identity Service** | 8081 | Gestión de usuarios y autenticación | Patrón Facade |
| **Submission Service** | 8082 | Formato A y Anteproyectos | Hexagonal + DDD |
| **Review Service** | 8084 | Evaluación de documentos | Template Method |
| **Notification Service** | 8083 | Notificaciones multicanal | Patrón Decorator |
| **Progress Tracking** | 8085 | Estado de proyectos | CQRS + Event Sourcing |

### Infraestructura

- **RabbitMQ** (Puertos 5672, 15672): Message broker para comunicación asíncrona
- **PostgreSQL** (Puertos 5432-5436): Base de datos independiente por servicio
- **Docker & Docker Compose**: Containerización y orquestación

### Patrón Arquitectónico: Event-Driven + CQRS

El sistema utiliza **Event-Driven Architecture** con patrón **CQRS** (Command Query Responsibility Segregation):

```
┌─────────────────┐         ┌─────────────────┐
│  Submission     │         │     Review      │
│   Service       │         │    Service      │
│  (Commands)     │         │   (Commands)    │
└────────┬────────┘         └────────┬────────┘
         │                           │
         │ Publica eventos           │
         └─────────┬─────────────────┘
                   │
                   ▼
           ┌──────────────┐
           │   RabbitMQ   │
           │ (Event Bus)  │
           └──────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
         ▼                   ▼
┌─────────────────┐   ┌─────────────────┐
│ Notification    │   │ Progress        │
│   Service       │   │  Tracking       │
│  (Consumer)     │   │   Service       │
│                 │   │ (Read Model)    │
└─────────────────┘   └─────────────────┘
```

**Beneficios:**
- ✅ Desacoplamiento entre servicios
- ✅ Historial completo de eventos (Event Sourcing)
- ✅ Vistas optimizadas para consultas
- ✅ Auditoría y trazabilidad completa
- ✅ Escalabilidad independiente

---

## 🎯 Microservicios

### 1. Gateway Service (Puerto 8080)

**Responsabilidad**: API Gateway centralizado - punto de entrada único al sistema.

**Funcionalidades Clave**:
- ✅ **Enrutamiento**: Proxy inverso a todos los microservicios
- ✅ **Autenticación JWT**: Validación de tokens en cada request
- ✅ **Autorización basada en roles**: Control de acceso por endpoint
- ✅ **Propagación de contexto**: Headers `X-User-*` a servicios downstream
- ✅ **Circuit Breaker**: Resiliencia ante fallos
- ✅ **Logging centralizado**: Request/Response tracking

**Filtros Implementados**:
- `JwtGatewayFilter`: Valida JWT y extrae claims (userId, rol, email)
- `RoleFilter`: Verifica permisos por rol (configurable)
- `RequestResponseLoggingFilter`: Auditoría de peticiones

**Rutas Públicas** (sin JWT):
- `/api/identity/**` - Registro y login
- `/api/auth/**` - Autenticación
- `/api/gateway/health` - Health checks

**Documentación Detallada**: Ver [`GESTION_JWT_MANUAL.md`](./GESTION_JWT_MANUAL.md)

---

### 2. Identity Service (Puerto 8081)

**Responsabilidad**: Gestión de identidad, autenticación y autorización.

**Arquitectura**: **Patrón Facade**

**Funcionalidades Clave**:
- ✅ Registro de usuarios (ESTUDIANTE, DOCENTE, COORDINADOR, JEFE_DEPARTAMENTO)
- ✅ Login con generación de token JWT (JJWT 0.12.x)
- ✅ Gestión de perfiles de usuario
- ✅ Búsqueda y consulta de usuarios con paginación
- ✅ Endpoints internos para comunicación entre servicios

**Tecnologías**:
- Spring Security + JWT
- PostgreSQL + Flyway (migraciones)
- Swagger/OpenAPI 3

**Patrón Facade**:
```
Controllers → IdentityFacade → { UserService, AuthService, JwtTokenProvider }
```

**Beneficios del Facade**:
- ✅ Simplifica controladores (una sola dependencia)
- ✅ Desacopla lógica de negocio de infraestructura
- ✅ Facilita testing unitario y mantenimiento

**Endpoints Principales**:
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Autenticación (retorna JWT)
- `GET /api/auth/profile` - Perfil del usuario autenticado
- `GET /api/auth/users/search` - Búsqueda de usuarios

**Documentación**: Ver [`identity-service/README.md`](./identity-service/README.md)

---

### 3. Submission Service (Puerto 8082)

**Responsabilidad**: Gestión del ciclo de vida de proyectos de grado.

**Arquitectura**: **Hexagonal (Puertos y Adaptadores) + Domain-Driven Design**

**Capas**:
```
Infrastructure Layer (JPA, RabbitMQ, REST, File Storage)
         ↓
Application Layer (Use Cases, DTOs, Ports)
         ↓
Domain Layer (Proyecto, Value Objects, Domain Events)
```

**Aggregate Root**: `Proyecto`
- Value Objects: `Titulo`, `Participantes`, `ObjetivosProyecto`
- Entities: `FormatoAInfo`, `AnteproyectoInfo`
- Domain Events: `FormatoACreado`, `AnteproyectoSubido`
- Specifications: `PuedeReenviarFormatoASpec`, `PuedeSubirAnteproyectoSpec`

**Funcionalidades**:
- ✅ Crear Formato A (RF2) con validaciones
- ✅ Reenviar hasta 3 intentos (RF4)
- ✅ Subir Anteproyecto (RF6)
- ✅ Consultar estado (RF5)
- ✅ Asignar evaluadores (RF8)
- ✅ Almacenamiento seguro de PDFs
- ✅ Validación de carta de aceptación

**Estados del Proyecto**:
```
FORMATO_A_DILIGENCIADO → EN_EVALUACION_COORDINADOR →
CORRECCIONES_SOLICITADAS (máx 3) / FORMATO_A_APROBADO →
ANTEPROYECTO_ENVIADO → ANTEPROYECTO_EN_EVALUACION
```

**Eventos Publicados**:
- `formato-a.enviado` (Exchange: formato-a-exchange)
- `formato-a.reenviado` (Exchange: formato-a-exchange)
- `anteproyecto.enviado` (Exchange: anteproyecto-exchange)

**Documentación**: [`submission-service/README.md`](./submission-service/README.md)

---

### 4. Review Service (Puerto 8084)

**Responsabilidad**: Gestión de evaluaciones académicas.

**Arquitectura**: **Patrón Template Method**

**Funcionalidades**:
- ✅ Evaluación de Formato A por coordinador (RF3)
- ✅ Asignación de evaluadores a anteproyectos (RF7)
- ✅ Evaluación de anteproyectos por dos evaluadores
- ✅ Comunicación HTTP con Submission Service (WebClient)

**Patrón Template Method**:
```java
abstract class EvaluationTemplate {
    public final EvaluationResult evaluate() {
        validatePermissions();    // Común
        DocumentInfo doc = fetchDocument();  // Abstracto
        validateDocumentState(doc);  // Abstracto
        Evaluation eval = saveEvaluation();  // Común
        updateSubmissionService();  // Abstracto
        publishEvent();  // Abstracto
        return buildResult();  // Común
    }
}
```

**Implementaciones**:
- `FormatoAEvaluationService` - Coordinador evalúa Formato A
- `AnteproyectoEvaluationService` - Evaluadores evalúan anteproyecto

**Eventos Publicados**:
- `formatoa.evaluado` (Exchange: evaluacion-exchange)
- `evaluadores.asignados` (Exchange: evaluacion-exchange)
- `anteproyecto.evaluado` (Exchange: evaluacion-exchange)

**Documentación**: [`review-service/README.md`](./review-service/README.md)

---

### 5. Notification Service (Puerto 8083)

**Responsabilidad**: Notificaciones multicanal.

**Arquitectura**: **Patrón Decorator**

**Funcionalidades**:
- ✅ Envío síncrono y asíncrono
- ✅ Múltiples canales (Email, SMS extensible)
- ✅ Plantillas dinámicas por tipo
- ✅ Validación y logging configurables
- ✅ Múltiples destinatarios
- ✅ Sin persistencia (microservicio ligero)

**Tipos de Notificaciones**:
| Tipo | Uso | Contexto |
|------|-----|----------|
| `DOCUMENT_SUBMITTED` | RF2, RF4, RF6 | Nuevo documento enviado |
| `EVALUATION_COMPLETED` | RF3 | Evaluación completada |
| `EVALUATOR_ASSIGNED` | RF7 | Evaluador asignado |
| `STATUS_CHANGED` | RF5 | Cambio de estado |
| `DEADLINE_REMINDER` | Futuro | Recordatorio |

**Patrón Decorator**:
```java
NotificationService base = new EmailNotificationService();
base = new ValidationDecorator(base);
base = new LoggingDecorator(base);
base.send(notification);  // Validación + Envío + Logging
```

**Consumo de Eventos**: Escucha todas las colas para generar notificaciones

**Documentación**: [`notification-service/README.md`](./notification-service/README.md)

---

### 6. Progress Tracking Service (Puerto 8085)

**Responsabilidad**: Seguimiento de estado de proyectos.

**Arquitectura**: **CQRS Read Model + Event Sourcing**

**Funcionalidades**:
- ✅ **Event Store**: Historial completo e inmutable
- ✅ **Vista Materializada**: Estado actual pre-calculado
- ✅ **APIs REST de solo lectura** (GET)
- ✅ **Auditoría completa**: Quién, qué, cuándo
- ✅ **Info de participantes**: Director, estudiantes, evaluadores

**Modelo de Datos**:
```sql
-- Event Store (append-only)
historial_eventos (evento_id, proyecto_id, tipo_evento, 
                   fecha, resultado, usuario_responsable)

-- Read Model (vista materializada)
proyecto_estado (proyecto_id, estado_actual, fase,
                 formato_a_version, director_id, estudiante1_id)
```

**Eventos Consumidos** (6 tipos):
- `formato-a.enviado`
- `formato-a.reenviado`
- `formatoa.evaluado`
- `anteproyecto.enviado`
- `evaluadores.asignados`
- `anteproyecto.evaluado`

**Endpoints de Consulta**:
- `GET /api/progress/proyectos/{id}/estado`
- `GET /api/progress/proyectos/{id}/historial`
- `GET /api/progress/proyectos/mis-proyectos`
- `GET /api/progress/estudiantes/{id}/historial`

**Beneficios del CQRS**:
- ✅ Consultas ultra-rápidas
- ✅ Historial completo para auditoría
- ✅ Desacoplamiento total
- ✅ Escalabilidad independiente

**Documentación**: [`progress-tracking-service/README.md`](./progress-tracking-service/README.md)

---

## 🎨 Patrones de Diseño y Arquitecturas

### Resumen

| Servicio | Patrón | Beneficio |
|----------|--------|-----------|
| **Gateway** | API Gateway | Punto de entrada único, seguridad |
| **Identity** | Facade | Simplificación de interfaces |
| **Submission** | Hexagonal + DDD | Lógica de negocio protegida |
| **Review** | Template Method | Reutilización de algoritmos |
| **Notification** | Decorator | Extensibilidad sin modificar código |
| **Progress Tracking** | CQRS + Event Sourcing | Separación lectura/escritura |

---

## 🔄 Comunicación Entre Servicios

### 1. Comunicación Síncrona (HTTP/REST)

**Gateway → Todos los servicios**: Proxy de requests

**Review → Submission**: Obtener documentos
```java
WebClient.create("http://submission-service:8082")
    .get().uri("/api/submissions/{id}", id)
    .header("X-User-Id", userId)
    .retrieve().bodyToMono(DocumentDTO.class)
```

**Review → Identity**: Obtener info de usuarios

### 2. Comunicación Asíncrona (RabbitMQ)

**Publicadores**:
- Submission Service → formato-a, anteproyecto events
- Review Service → evaluacion events

**Consumidores**:
- Progress Tracking Service → Todos los eventos
- Notification Service → Todos los eventos

### 3. Propagación de Contexto

Gateway añade headers automáticamente:
- `X-User-Id`: ID del usuario
- `X-User-Role`: Rol (DOCENTE, COORDINADOR, etc.)
- `X-User-Email`: Email

```java
@PostMapping("/formatoA")
public ResponseEntity<?> crear(
    @RequestHeader("X-User-Id") Long userId,
    @RequestHeader("X-User-Role") String role) {
    // Contexto de usuario disponible
}
```

---

## 📡 Eventos del Sistema

### Arquitectura de Eventos

El sistema utiliza **RabbitMQ** como message broker para comunicación asíncrona basada en eventos.

### Topología de RabbitMQ

```
Exchange: formato-a-exchange (type: direct)
├── Queue: progress-tracking.formato-a
│   ├── Routing Key: formato-a.enviado
│   └── Routing Key: formato-a.reenviado
└── Queue: notifications.formato-a
    └── Routing Key: formato-a.*

Exchange: anteproyecto-exchange (type: direct)
├── Queue: progress-tracking.anteproyecto
│   └── Routing Key: anteproyecto.enviado
└── Queue: notifications.anteproyecto
    └── Routing Key: anteproyecto.*

Exchange: evaluacion-exchange (type: direct)
├── Queue: progress-tracking.evaluacion
│   ├── Routing Key: formatoa.evaluado
│   ├── Routing Key: evaluadores.asignados
│   └── Routing Key: anteproyecto.evaluado
└── Queue: notifications.evaluacion
    └── Routing Key: *.evaluado
    └── Routing Key: evaluadores.asignados
```

### Eventos Publicados y Consumidos

| Evento | Publicador | Consumidores | Descripción |
|--------|-----------|--------------|-------------|
| `formato-a.enviado` | Submission | Progress Tracking, Notification | Primera versión del Formato A |
| `formato-a.reenviado` | Submission | Progress Tracking, Notification | Versión 2 o 3 del Formato A |
| `formatoa.evaluado` | Review | Progress Tracking, Notification | Coordinador evalúa Formato A |
| `anteproyecto.enviado` | Submission | Progress Tracking, Notification | Envío del anteproyecto |
| `evaluadores.asignados` | Review | Progress Tracking, Notification | Asignación de evaluadores |
| `anteproyecto.evaluado` | Review | Progress Tracking, Notification | Evaluación de anteproyecto |

### Estructura de un Evento

Todos los eventos comparten una estructura común:

```json
{
  "proyectoId": 123,
  "titulo": "Sistema de IA para análisis de datos",
  "modalidad": "INDIVIDUAL",
  "programa": "INGENIERIA_SISTEMAS",
  "timestamp": "2025-12-10T10:30:00",
  
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR",
  
  "directorId": 12,
  "directorNombre": "Dr. Juan Pérez",
  
  "estudiante1Id": 1001,
  "estudiante1Nombre": "María García",
  "estudiante1Email": "maria.garcia@unicauca.edu.co",
  
  "estudiante2Id": null,
  "estudiante2Nombre": null,
  "estudiante2Email": null
}
```

### Ejemplo: Evento `formatoa.evaluado`

```json
{
  "proyectoId": 123,
  "formatoAId": 456,
  "version": 1,
  "titulo": "Sistema de IA para análisis de datos",
  "modalidad": "INDIVIDUAL",
  "programa": "INGENIERIA_SISTEMAS",
  "timestamp": "2025-12-10T16:00:00",
  
  "resultado": "APROBADO",
  "observaciones": "Excelente propuesta, bien estructurada",
  
  "usuarioResponsableId": 5,
  "usuarioResponsableNombre": "Dra. Ana Martínez",
  "usuarioResponsableRol": "COORDINADOR",
  
  "directorId": 12,
  "directorNombre": "Dr. Juan Pérez",
  
  "estudiante1Id": 1001,
  "estudiante1Nombre": "María García",
  "estudiante1Email": "maria.garcia@unicauca.edu.co"
}
```

**Documentación completa**: Ver [`progress-tracking-service/DOCUMENTACION_EVENTOS_COMPLETA.md`](./progress-tracking-service/DOCUMENTACION_EVENTOS_COMPLETA.md)

---

## 🚀 Inicio Rápido con Docker Compose

### Prerequisitos

- Docker Desktop instalado
- Docker Compose (incluido en Docker Desktop)
- Al menos 4GB de RAM disponible
- Puertos 8080-8085, 5432-5434, 5672, 15672 disponibles

### Pasos para Iniciar

1. **Configurar variables de entorno**
   ```bash
   # Copiar el archivo de ejemplo
   copy .env.example .env
   
   # Editar .env con tus valores reales
   notepad .env
   ```

2. **Iniciar todos los servicios**
   ```bash
   docker-compose up -d
   ```

3. **Verificar el estado de los servicios**
   ```bash
   docker-compose ps
   ```

4. **Ver logs de todos los servicios**
   ```bash
   docker-compose logs -f
   ```

5. **Ver logs de un servicio específico**
   ```bash
   docker-compose logs -f gateway
   docker-compose logs -f identity
   docker-compose logs -f submission
   docker-compose logs -f notification
   docker-compose logs -f review
   docker-compose logs -f progress-tracking
   ```

### Detener los Servicios

```bash
# Detener sin eliminar volúmenes (datos se mantienen)
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

## 🔧 Distribución de Puertos

### Servicios de Aplicación
| Servicio | Puerto Interno | Puerto Externo | URL |
|----------|---------------|----------------|-----|
| Gateway | 8080 | 8080 | http://localhost:8080 |
| Identity | 8081 | 8081 | http://localhost:8081 |
| Submission | 8082 | 8082 | http://localhost:8082 |
| Notification | 8083 | 8083 | http://localhost:8083 |
| Review | 8084 | 8084 | http://localhost:8084 |
| Progress Tracking | 8085 | 8085 | http://localhost:8085 |

### Infraestructura
| Servicio | Puerto Interno | Puerto Externo | Descripción |
|----------|---------------|----------------|-------------|
| RabbitMQ AMQP | 5672 | 5672 | Protocolo de mensajería |
| RabbitMQ Management | 15672 | 15672 | UI de administración |
| PostgreSQL Identity | 5432 | 5432 | Base de datos Identity |
| PostgreSQL Submission | 5432 | 5433 | Base de datos Submission |
| PostgreSQL Notification | 5432 | 5434 | Base de datos Notification |
| PostgreSQL Review | 5432 | 5435 | Base de datos Review |
| PostgreSQL Progress Tracking | 5432 | 5436 | Base de datos Progress Tracking |

## 🔐 Variables de Entorno Requeridas

El archivo `.env` debe contener:

```env
# JWT - Secret para firmar tokens (mínimo 32 caracteres)
JWT_SECRET=your-super-secret-jwt-key-change-this

# RabbitMQ - Credenciales del message broker
RABBITMQ_USER=admin
RABBITMQ_PASS=admin_password

# Bases de Datos
IDENTITY_DB_USER=identity_user
IDENTITY_DB_PASS=identity_password

SUBMISSION_DB_USER=submission_user
SUBMISSION_DB_PASS=submission_password

NOTIFICATION_DB_USER=notification_user
NOTIFICATION_DB_PASS=notification_password

REVIEW_DB_USER=review_user
REVIEW_DB_PASS=review_password

PROGRESS_TRACKING_DB_USER=progress_user
PROGRESS_TRACKING_DB_PASS=progress_password

# SMTP - Configuración para envío de emails
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
SMTP_FROM=noreply@trabajogrado.com
```

## 🏥 Health Checks

Todos los servicios exponen endpoints de health check:

- Gateway: http://localhost:8080/api/gateway/health
- Identity: http://localhost:8081/actuator/health
- Submission: http://localhost:8082/actuator/health
- Notification: http://localhost:8083/actuator/health
- Review: http://localhost:8084/actuator/health
- Progress Tracking: http://localhost:8085/actuator/health
- RabbitMQ: http://localhost:15672 (usuario/contraseña desde .env)

## 📊 Monitoreo

### RabbitMQ Management UI
Accede a http://localhost:15672 con las credenciales configuradas en `.env`

### Logs en Tiempo Real
```bash
# Todos los servicios
docker-compose logs -f

# Servicio específico
docker-compose logs -f gateway
```

## 🔄 Reconstruir Servicios

Cuando hagas cambios en el código:

```bash
# Reconstruir un servicio específico
docker-compose up -d --build gateway

# Reconstruir todos los servicios
docker-compose up -d --build
```

## 🐛 Troubleshooting

### Los contenedores no inician
```bash
# Ver logs detallados
docker-compose logs

# Verificar puertos en uso
netstat -ano | findstr "8080"
netstat -ano | findstr "5432"
```

### Limpiar y reiniciar desde cero
```bash
# Detener y eliminar todo
docker-compose down -v

# Eliminar imágenes
docker-compose down --rmi all -v

# Reconstruir y iniciar
docker-compose up -d --build
```

### Error de conexión a base de datos
- Verificar que las variables de entorno en `.env` coincidan
- Esperar 30-60 segundos para que las bases de datos inicien completamente
- Revisar logs: `docker-compose logs postgres-identity`

### Problemas con RabbitMQ
```bash
# Verificar estado
docker-compose exec rabbitmq rabbitmq-diagnostics ping

# Ver logs
docker-compose logs rabbitmq
```

## 🏗️ Desarrollo Local vs Producción

### Modo Desarrollo (servicios individuales)
Cada microservicio tiene su propio `docker-compose.yml` para desarrollo aislado:
- `identity-service/docker-compose.yml`
- `submission-service/docker-compose.yml`
- `notification-service/docker-compose.yml`
- `review-service/docker-compose.yml`
- `progress-tracking-service/docker-compose.yml`

### Modo Producción (docker-compose raíz)
El `docker-compose.yaml` en la raíz inicia **todo el sistema completo** con:
- Todos los microservicios
- Todas las bases de datos
- RabbitMQ
- Red compartida `trabajo-grado-network`
- Health checks configurados
- Límites de recursos

## 📁 Estructura del Proyecto

```
GesTrabajoGrado-Microservicios/
├── docker-compose.yaml          # ⭐ Docker Compose principal (USAR ESTE)
├── .env                          # Variables de entorno (crear desde .env.example)
├── .env.example                  # Plantilla de variables de entorno
├── README.md                     # Esta documentación
├── gateway-service/
│   ├── Dockerfile
│   └── src/
├── identity-service/
│   ├── Dockerfile
│   ├── docker-compose.yml       # Para desarrollo individual
│   └── src/
├── submission-service/
│   ├── Dockerfile
│   ├── docker-compose.yml       # Para desarrollo individual
│   └── src/
├── notification-service/
│   ├── Dockerfile
│   ├── docker-compose.yml       # Para desarrollo individual
│   └── src/
├── review-service/
│   ├── Dockerfile
│   ├── docker-compose.yml       # Para desarrollo individual
│   └── src/
└── progress-tracking-service/
    ├── Dockerfile
    ├── docker-compose.yml       # Para desarrollo individual
    └── src/
```

## ✅ Checklist de Despliegue

- [ ] Copiar `.env.example` a `.env`
- [ ] Configurar todas las variables en `.env`
- [ ] Cambiar `JWT_SECRET` por un valor seguro (mínimo 32 caracteres)
- [ ] Configurar credenciales SMTP reales para emails
- [ ] Verificar que los puertos estén disponibles
- [ ] Ejecutar `docker-compose up -d`
- [ ] Esperar 1-2 minutos para que todos los servicios inicien
- [ ] Verificar health checks de todos los servicios
- [ ] Probar acceso al gateway: http://localhost:8080

## 📞 Endpoints Principales

### Gateway (Punto de Entrada Único)

**Base URL**: `http://localhost:8080`

Todos los requests del cliente deben pasar por el Gateway, que:
1. Valida el token JWT (excepto rutas públicas)
2. Verifica autorización por rol
3. Añade headers `X-User-*`
4. Proxy la petición al servicio correspondiente

### Endpoints por Servicio

#### 1. Identity Service (Autenticación)

```bash
# Registro de usuario (Público)
POST http://localhost:8080/api/auth/register
Content-Type: application/json
{
  "nombres": "Juan",
  "apellidos": "Pérez",
  "email": "juan.perez@unicauca.edu.co",
  "password": "Pass123!",
  "rol": "DOCENTE",
  "programa": "INGENIERIA_SISTEMAS"
}

# Login (Público)
POST http://localhost:8080/api/auth/login
Content-Type: application/json
{
  "email": "juan.perez@unicauca.edu.co",
  "password": "Pass123!"
}
# Response: { "token": "eyJhbGci...", "user": {...} }

# Perfil del usuario autenticado (Requiere JWT)
GET http://localhost:8080/api/auth/profile
Authorization: Bearer eyJhbGci...

# Búsqueda de usuarios (Requiere JWT)
GET http://localhost:8080/api/auth/users/search?query=juan&rol=DOCENTE
Authorization: Bearer eyJhbGci...
```

#### 2. Submission Service (Gestión de Proyectos)

```bash
# Crear Formato A (Requiere JWT - Rol: DOCENTE)
POST http://localhost:8080/api/submissions/formatoA
Authorization: Bearer eyJhbGci...
Content-Type: multipart/form-data
- pdf: archivo.pdf
- data: {
    "titulo": "Sistema de IA",
    "modalidad": "INDIVIDUAL",
    "objetivoGeneral": "Desarrollar...",
    "objetivosEspecificos": ["Objetivo 1", "Objetivo 2"],
    "estudiante1Id": 1001
  }

# Reenviar Formato A (Requiere JWT - Rol: DOCENTE)
POST http://localhost:8080/api/submissions/formatoA/{id}/reenviar
Authorization: Bearer eyJhbGci...
Content-Type: multipart/form-data
- pdf: archivo_v2.pdf

# Consultar proyecto por ID (Requiere JWT)
GET http://localhost:8080/api/submissions/{id}
Authorization: Bearer eyJhbGci...

# Consultar proyectos del estudiante (Requiere JWT - Rol: ESTUDIANTE)
GET http://localhost:8080/api/submissions/estudiante/{estudianteId}
Authorization: Bearer eyJhbGci...

# Subir Anteproyecto (Requiere JWT - Rol: DOCENTE)
POST http://localhost:8080/api/submissions/anteproyecto/{proyectoId}
Authorization: Bearer eyJhbGci...
Content-Type: multipart/form-data
- pdf: anteproyecto.pdf
```

#### 3. Review Service (Evaluaciones)

```bash
# Listar Formatos A pendientes (Requiere JWT - Rol: COORDINADOR)
GET http://localhost:8080/api/review/formatoA/pendientes?page=0&size=10
Authorization: Bearer eyJhbGci...

# Evaluar Formato A (Requiere JWT - Rol: COORDINADOR)
POST http://localhost:8080/api/review/formatoA/{id}/evaluar
Authorization: Bearer eyJhbGci...
Content-Type: application/json
{
  "decision": "APROBADO",
  "observaciones": "Excelente propuesta"
}

# Asignar evaluadores (Requiere JWT - Rol: JEFE_DEPARTAMENTO)
POST http://localhost:8080/api/review/anteproyectos/asignar
Authorization: Bearer eyJhbGci...
Content-Type: application/json
{
  "anteproyectoId": 5,
  "evaluador1Id": 15,
  "evaluador2Id": 20
}

# Evaluar Anteproyecto (Requiere JWT - Rol: DOCENTE evaluador asignado)
POST http://localhost:8080/api/review/anteproyectos/{id}/evaluar
Authorization: Bearer eyJhbGci...
Content-Type: application/json
{
  "decision": "APROBADO",
  "observaciones": "Buen trabajo"
}
```

#### 4. Progress Tracking Service (Consultas de Estado)

```bash
# Estado actual del proyecto (Requiere JWT)
GET http://localhost:8080/api/progress/proyectos/{id}/estado
Authorization: Bearer eyJhbGci...

# Historial completo de eventos (Requiere JWT)
GET http://localhost:8080/api/progress/proyectos/{id}/historial?page=0&size=20
Authorization: Bearer eyJhbGci...

# Mis proyectos (director o estudiante autenticado)
GET http://localhost:8080/api/progress/proyectos/mis-proyectos
Authorization: Bearer eyJhbGci...

# Historial del estudiante (Requiere JWT)
GET http://localhost:8080/api/progress/estudiantes/{estudianteId}/historial
Authorization: Bearer eyJhbGci...
```

#### 5. Notification Service

```bash
# Notificaciones del usuario autenticado (Requiere JWT)
GET http://localhost:8080/api/notifications/mis-notificaciones
Authorization: Bearer eyJhbGci...
```

### Endpoints Directos (Solo Desarrollo)

⚠️ **No usar en producción** - Bypasean el Gateway y sus validaciones

- Identity: `http://localhost:8081/api/auth/*`
- Submission: `http://localhost:8082/api/submissions/*`
- Review: `http://localhost:8084/api/review/*`
- Progress: `http://localhost:8085/api/progress/*`
- Notification: `http://localhost:8083/api/notifications/*`

### Documentación Swagger

Cada microservicio expone documentación Swagger en:
- `http://localhost:808X/swagger-ui.html` (donde X es el puerto del servicio)

---

## 📚 Documentación Adicional

### Documentos de Referencia

| Documento | Descripción | Ubicación |
|-----------|-------------|-----------|
| **Gestión JWT Manual** | Cómo funciona la autenticación JWT sin OAuth2 | [`GESTION_JWT_MANUAL.md`](./GESTION_JWT_MANUAL.md) |
| **Submission Service** | Arquitectura Hexagonal + DDD | [`submission-service/README.md`](./submission-service/README.md) |
| **Review Service** | Patrón Template Method | [`review-service/README.md`](./review-service/README.md) |
| **Progress Tracking** | CQRS + Event Sourcing | [`progress-tracking-service/README.md`](./progress-tracking-service/README.md) |
| **Notification Service** | Patrón Decorator | [`notification-service/README.md`](./notification-service/README.md) |
| **Identity Service** | Patrón Facade | [`identity-service/README.md`](./identity-service/README.md) |
| **Eventos Completos** | Todos los eventos del sistema | [`progress-tracking-service/DOCUMENTACION_EVENTOS_COMPLETA.md`](./progress-tracking-service/DOCUMENTACION_EVENTOS_COMPLETA.md) |

---

## 🏆 Requisitos Funcionales Implementados

| RF | Descripción | Servicio(s) | Estado |
|----|-------------|-------------|--------|
| **RF2** | Crear Formato A para iniciar proyecto | Submission | ✅ Implementado |
| **RF3** | Evaluar Formato A (coordinador) | Review | ✅ Implementado |
| **RF4** | Reenviar Formato A (máx 3 intentos) | Submission | ✅ Implementado |
| **RF5** | Consultar estado del proyecto | Progress Tracking | ✅ Implementado |
| **RF6** | Subir anteproyecto | Submission | ✅ Implementado |
| **RF7** | Listar y asignar evaluadores | Review | ✅ Implementado |
| **RF8** | Evaluar anteproyecto | Review | ✅ Implementado |

---

## 🔒 Seguridad

### Autenticación JWT

- **Generación**: Identity Service genera tokens firmados con HMAC-SHA256
- **Validación**: Gateway valida firma y expiración en cada request
- **Propagación**: Gateway añade headers `X-User-*` a servicios downstream
- **Expiración**: Tokens expiran en 1 hora (configurable)

**Documentación completa**: Ver [`GESTION_JWT_MANUAL.md`](./GESTION_JWT_MANUAL.md)

### Autorización por Roles

| Rol | Permisos |
|-----|----------|
| **ESTUDIANTE** | Ver estado de su proyecto |
| **DOCENTE** | Crear/reenviar Formato A, subir anteproyecto, evaluar anteproyectos asignados |
| **COORDINADOR** | Evaluar Formato A |
| **JEFE_DEPARTAMENTO** | Asignar evaluadores a anteproyectos |

### Niveles de Seguridad

1. **Gateway**: Validación de JWT y verificación de roles
2. **Servicios**: Validación de contexto de usuario (headers `X-User-*`)
3. **Dominio**: Reglas de negocio (ej: solo el director puede reenviar su Formato A)

---

## 🧪 Testing

Cada microservicio incluye:
- ✅ Tests unitarios (JUnit 5 + Mockito)
- ✅ Tests de integración (Spring Boot Test)
- ✅ Tests de controllers (MockMvc / WebTestClient)
- ✅ Tests de repositorios (DataJpaTest)

**Ejecutar tests**:
```bash
# Todos los tests de un servicio
cd submission-service
mvn test

# Con cobertura
mvn test jacoco:report
```

---

## 📈 Escalabilidad y Rendimiento

### Estrategias Implementadas

- ✅ **Microservicios independientes**: Escala horizontal por servicio
- ✅ **Event-Driven**: Desacoplamiento temporal y espacial
- ✅ **CQRS**: Separación de escritura/lectura para optimizar consultas
- ✅ **Bases de datos independientes**: Sin punto único de fallo
- ✅ **Circuit Breaker**: Resiliencia ante fallos (en Gateway)
- ✅ **Caching**: En vistas materializadas (Progress Tracking)

### Recomendaciones para Producción

1. **Load Balancer**: Nginx o AWS ELB delante del Gateway
2. **Múltiples instancias**: Escalar servicios más demandados (Gateway, Submission)
3. **RabbitMQ Cluster**: Alta disponibilidad del message broker
4. **PostgreSQL con réplicas**: Read replicas para Progress Tracking
5. **Monitoring**: Prometheus + Grafana para métricas
6. **Logging centralizado**: ELK Stack o similar

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.2.x**
- **Spring Cloud Gateway**
- **Spring Data JPA**
- **Spring Security + JWT**
- **Spring AMQP (RabbitMQ)**
- **Hibernate Validator**

### Bases de Datos
- **PostgreSQL 15+**
- **Flyway** (Migraciones)

### Mensajería
- **RabbitMQ 3.12**

### Documentación
- **SpringDoc OpenAPI 3** (Swagger)

### Containerización
- **Docker**
- **Docker Compose**

### Testing
- **JUnit 5**
- **Mockito**
- **Spring Boot Test**
- **Testcontainers** (para tests de integración)

---

## 🚧 Trabajo Futuro

### Mejoras Planificadas

- [ ] **Refresh Tokens**: Implementar renovación de tokens sin re-login
- [ ] **Métricas y Monitoring**: Integrar Prometheus + Grafana
- [ ] **API Rate Limiting**: Limitar requests por usuario/IP
- [ ] **Webhook Events**: Notificar a sistemas externos
- [ ] **File Storage S3**: Migrar de almacenamiento local a S3/MinIO
- [ ] **Búsqueda Avanzada**: Elasticsearch para búsquedas full-text
- [ ] **Dashboard Web**: Frontend React/Angular
- [ ] **Mobile App**: Aplicación móvil nativa
- [ ] **CI/CD Pipeline**: GitHub Actions / GitLab CI
- [ ] **Kubernetes**: Migrar de Docker Compose a K8s

### Funcionalidades Adicionales

- [ ] Sustentación de anteproyectos
- [ ] Gestión de calendario de sustentaciones
- [ ] Generación de reportes PDF
- [ ] Firma digital de documentos
- [ ] Integración con sistemas académicos
- [ ] Chat en tiempo real (WebSockets)

---

## 👥 Contribución

Para contribuir al proyecto:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

### Estándares de Código

- **Java**: Seguir convenciones de Java (CamelCase, etc.)
- **Commits**: Mensajes descriptivos en español
- **Tests**: Agregar tests para nuevas funcionalidades
- **Documentación**: Actualizar README si aplica

---

## 📄 Licencia

Este proyecto es parte de un trabajo académico de la Universidad del Cauca.

---

## 📧 Contacto y Soporte

Para preguntas o soporte técnico, consultar la documentación individual de cada microservicio o revisar los issues en el repositorio.

---

## 🎓 Créditos

**Sistema de Gestión de Trabajos de Grado**  
**Universidad del Cauca**  
**Ingeniería de Software II**  
**Año:** 2025

**Arquitectura y Patrones:**
- Arquitectura Hexagonal (Submission Service)
- Domain-Driven Design (Submission Service)
- Patrón Facade (Identity Service)
- Patrón Template Method (Review Service)
- Patrón Decorator (Notification Service)
- CQRS + Event Sourcing (Progress Tracking Service)
- Event-Driven Architecture (Sistema completo)

---

**Última actualización:** Diciembre 10, 2025  
**Versión:** 2.0.0
      "email": "maria.garcia@unicauca.edu.co"
    },
    "estudiante2": {
      "id": 1002,
      "nombre": "Carlos López Ramírez",
      "email": "carlos.lopez@unicauca.edu.co"
    }
  },
  "formatoA": {
    "version": 2,
    "intentoActual": 2,
    "estado": "APROBADO"
  },
  "anteproyecto": {
    "estado": "EN_EVALUACION",
    "evaluadoresAsignados": true
  },
  "ultimaActualizacion": "2025-12-06T18:30:00"
}
```

### Modelo de Datos

**Tabla: `historial_eventos`** (Event Store)
- Registro inmutable de todos los eventos
- Campos: proyecto_id, tipo_evento, fecha, descripcion, version, resultado, observaciones, usuario_responsable, metadata

**Tabla: `proyecto_estado`** (Vista Materializada)
- Estado actual pre-calculado del proyecto
- Campos: proyecto_id, titulo, modalidad, programa, estado_actual, fase
- Participantes: director, codirector, estudiante1, estudiante2
- Estado Formato A: version, intento_actual, estado
- Estado Anteproyecto: estado, evaluadores_asignados

### Documentación de Eventos

Para información completa sobre los eventos del sistema, consulta:

📄 **`progress-tracking-service/DOCUMENTACION_EVENTOS_COMPLETA.md`**

Este documento centraliza:
- ✅ Cuándo publicar cada evento (submission-service, review-service)
- ✅ Estructura completa de todos los eventos (payloads)
- ✅ Información de participantes requerida
- ✅ Código de implementación
- ✅ Guías de validación y pruebas

## 📚 Documentación Adicional

### Por Microservicio

- **Progress Tracking Service**: Ver `progress-tracking-service/README.md`
  - Arquitectura CQRS
  - Event Sourcing
  - APIs de consulta
  - Documentación de eventos: `DOCUMENTACION_EVENTOS_COMPLETA.md`

- **Identity Service**: Ver `identity-service/README.md`
  - Gestión de usuarios
  - Autenticación JWT
  - Roles y permisos

- **Submission Service**: Ver `submission-service/README.md`
  - Gestión de Formato A
  - Gestión de Anteproyectos
  - Carga de documentos

- **Review Service**: Ver `review-service/README.md`
  - Evaluación de Formato A
  - Asignación de evaluadores
  - Evaluación de Anteproyectos

- **Notification Service**: Ver `notification-service/README.md`
  - Envío de emails
  - Notificaciones del sistema

### Guías de Pruebas

- **Eventos con Postman**: Ver `PRUEBA_EVENTOS_POSTMAN.md`
- **Review Service**: Ver `review-service/GUIA_PRUEBAS.md`

## 🔒 Seguridad

- Todos los servicios corren con usuarios no-root
- JWT para autenticación
- Variables de entorno para secretos
- Health checks configurados
- Límites de recursos por contenedor

## 🎯 Próximos Pasos

1. Configurar CI/CD para builds automatizados
2. Agregar monitoring con Prometheus/Grafana
3. Implementar circuit breakers con Resilience4j
4. Agregar API Gateway rate limiting
5. Configurar logs centralizados con ELK Stack

---

**Autor**: Equipo de Desarrollo - Unicauca  
**Fecha**: Octubre 2025  
**Versión**: 1.0.0

"# MicroservicioSubmission"
