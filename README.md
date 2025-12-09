# 🎓 Sistema de Gestión de Trabajo de Grado - Microservicios

Sistema completo basado en microservicios para la gestión de trabajos de grado, construido con Spring Boot, arquitectura Event-Driven y patrón CQRS.

## 📋 Arquitectura

El sistema está compuesto por los siguientes servicios:

### Microservicios de Negocio

- **Gateway Service** (Puerto 8080): Punto de entrada único, enrutamiento y autenticación
- **Identity Service** (Puerto 8081): Gestión de usuarios, roles y autenticación JWT
- **Submission Service** (Puerto 8082): Gestión de Formato A, Anteproyectos y documentos
- **Review Service** (Puerto 8084): Evaluación de Formato A y Anteproyectos por coordinadores y evaluadores
- **Notification Service** (Puerto 8083): Envío de notificaciones por email y sistema
- **Progress Tracking Service** (Puerto 8085): **Seguimiento del estado de proyectos (CQRS Read Model)**

### Infraestructura

- **RabbitMQ** (Puertos 5672, 15672): Message broker para comunicación asíncrona entre microservicios
- **PostgreSQL**: Bases de datos independientes para cada servicio

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
- Base URL: http://localhost:8080
- Health: http://localhost:8080/api/gateway/health

### A través del Gateway:
- Auth: `POST http://localhost:8080/api/identity/auth/login`
- Submissions: `http://localhost:8080/api/submissions/*`
- Notifications: `http://localhost:8080/api/notifications/*`
- Reviews: `http://localhost:8080/api/reviews/*`
- Progress Tracking: `http://localhost:8080/api/progress/*`

### Endpoints Directos (Desarrollo):
- Identity: http://localhost:8081/api/identity/*
- Submission: http://localhost:8082/api/submissions/*
- Notification: http://localhost:8083/api/notifications/*
- Review: http://localhost:8084/api/reviews/*
- Progress: http://localhost:8085/api/progress/*

## 🎯 Progress Tracking Service (CQRS Read Model)

El **Progress Tracking Service** es un componente clave que implementa el patrón **CQRS Read Model** para consultas optimizadas del estado de proyectos.

### Características Principales

- ✅ **Event Sourcing**: Guarda historial completo de todos los eventos
- ✅ **Vista Materializada**: Estado actual pre-calculado para consultas rápidas
- ✅ **Solo Lectura**: APIs REST únicamente de consulta (GET)
- ✅ **Auditoría Completa**: Registro inmutable de todos los cambios
- ✅ **Información de Participantes**: Director, codirector, estudiantes, evaluadores

### Eventos Consumidos

| Evento | Origen | Efecto |
|--------|--------|--------|
| `formato-a.enviado` | submission-service | Registra primera versión de Formato A |
| `formato-a.reenviado` | submission-service | Registra versión 2 o 3 de Formato A |
| `formatoa.evaluado` | review-service | Actualiza resultado de evaluación |
| `anteproyecto.enviado` | submission-service | Registra envío de anteproyecto |
| `evaluadores.asignados` | review-service | Registra asignación de evaluadores |
| `anteproyecto.evaluado` | review-service | Actualiza resultado de evaluación |

### Endpoints de Consulta

```bash
# Obtener estado actual del proyecto
GET /api/progress/proyectos/{id}/estado

# Obtener historial completo de eventos
GET /api/progress/proyectos/{id}/historial

# Obtener proyectos del usuario autenticado (director/estudiante)
GET /api/progress/proyectos/mis-proyectos
Headers: X-User-Id, X-User-Role

# Obtener historial del proyecto de un estudiante
GET /api/progress/estudiantes/{estudianteId}/historial

# Health check
GET /api/progress/health
```

### Ejemplo de Respuesta - Estado del Proyecto

```json
{
  "proyectoId": 123,
  "titulo": "Sistema de IA para análisis de datos educativos",
  "modalidad": "DUPLA",
  "programa": "INGENIERIA_SISTEMAS",
  "estadoActual": "ANTEPROYECTO_EN_EVALUACION",
  "estadoLegible": "Anteproyecto en evaluación",
  "siguientePaso": "Esperar evaluación de evaluadores",
  "fase": "ANTEPROYECTO",
  "participantes": {
    "director": {
      "id": 12,
      "nombre": "Dr. Juan Pérez"
    },
    "codirector": {
      "id": 15,
      "nombre": "Dra. Ana Martínez"
    },
    "estudiante1": {
      "id": 1001,
      "nombre": "María García López",
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
  - **DTOs documentados**: Todos los DTOs incluyen validaciones Jakarta Validation y documentación OpenAPI

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
