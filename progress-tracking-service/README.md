# Progress Tracking Service - Documentación

## 📋 Descripción General

**Progress Tracking Service** es un microservicio de seguimiento y consulta del estado de proyectos de grado, implementado siguiendo el patrón **CQRS (Command Query Responsibility Segregation)** como **Read Model**.

### Responsabilidades Principales

- **Consumir eventos** de otros microservicios vía RabbitMQ
- **Mantener un historial completo** de todos los eventos del proyecto (Event Sourcing)
- **Proporcionar una vista materializada** del estado actual de cada proyecto
- **Exponer APIs REST de solo lectura** para consultar información

### Características Clave

✅ **Event-Driven Architecture**: Consume eventos sin publicarlos

✅ **Vista Materializada**: Estado actual pre-calculado para consultas rápidas

✅ **Historial Inmutable**: Registro completo de eventos (auditoría completa)

✅ **API REST de Lectura**: Solo endpoints GET (no modifica datos directamente)

✅ **Paginación y Filtros**: Consultas eficientes del historial

---

## 🏗️ Arquitectura

### Patrón CQRS Read Model

```
┌─────────────────────┐
│ Submission Service  │──┐
└─────────────────────┘  │
                         │ Publica
┌─────────────────────┐  │ eventos
│  Review Service     │──┤
└─────────────────────┘  │
                         ▼
                   ┌──────────┐
                   │ RabbitMQ │
                   │ Exchange │
                   └──────────┘
                         │
                         │ Consume
                         ▼
              ┌────────────────────┐
              │ Progress Tracking  │
              │     Service        │
              └────────────────────┘
                         │
                         ├─► historial_eventos (append-only)
                         └─► proyecto_estado (vista materializada)
```

### Componentes

1. **Consumer (ProjectEventConsumer)**: Escucha eventos de RabbitMQ
2. **Repository Layer**: Acceso a datos (JPA/Hibernate)
3. **Service Layer**: Lógica de actualización de estado
4. **Controller Layer**: APIs REST de consulta

---

## 🗄️ Modelo de Datos

### Tabla: `historial_eventos`

Registro inmutable de todos los eventos (Event Store):

```sql
CREATE TABLE historial_eventos (
    evento_id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    descripcion TEXT,
    version INTEGER,
    resultado VARCHAR(50),
    observaciones TEXT,
    usuario_responsable_id BIGINT,
    usuario_responsable_nombre VARCHAR(200),
    usuario_responsable_rol VARCHAR(50),
    metadata TEXT,
    INDEX idx_proyecto (proyecto_id),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo_evento (tipo_evento)
);
```

### Tabla: `proyecto_estado`

Vista materializada del estado actual:

```sql
CREATE TABLE proyecto_estado (
    proyecto_id BIGINT PRIMARY KEY,
    titulo VARCHAR(500),
    modalidad VARCHAR(50),
    programa VARCHAR(100),
    estado_actual VARCHAR(100) NOT NULL,
    fase VARCHAR(50),
    
    -- Formato A
    formato_a_version INTEGER DEFAULT 0,
    formato_a_intento_actual INTEGER DEFAULT 0,
    formato_a_max_intentos INTEGER DEFAULT 3,
    formato_a_estado VARCHAR(50),
    formato_a_fecha_ultimo_envio TIMESTAMP,
    formato_a_fecha_ultima_evaluacion TIMESTAMP,
    
    -- Anteproyecto
    anteproyecto_estado VARCHAR(50),
    anteproyecto_fecha_envio TIMESTAMP,
    anteproyecto_evaluadores_asignados BOOLEAN DEFAULT FALSE,
    
    -- Participantes
    director_id BIGINT,
    director_nombre VARCHAR(200),
    codirector_id BIGINT,
    codirector_nombre VARCHAR(200),
    
    ultima_actualizacion TIMESTAMP
);
```

---

## 📦 DTOs (Data Transfer Objects)

El servicio utiliza DTOs para estructurar las respuestas de forma clara y desacoplada de las entidades de base de datos.

### DTOs de Respuesta

#### EstadoProyectoResponseDTO

DTO principal para el estado completo de un proyecto.

```java
{
  "proyectoId": 1,
  "titulo": "Sistema de IA para Agricultura",
  "modalidad": "INVESTIGACION",
  "programa": "INGENIERIA_SISTEMAS",
  "estadoActual": "FORMATO_A_APROBADO",
  "estadoLegible": "Formato A Aprobado ✅",
  "fase": "FORMATO_A",
  "ultimaActualizacion": "2025-11-01T16:00:00",
  "siguientePaso": "Preparar anteproyecto",
  "formatoA": { /* FormatoAInfoDTO */ },
  "anteproyecto": { /* AnteproyectoInfoDTO */ },
  "participantes": { /* ParticipantesDTO */ },
  "estudiantes": { /* EstudiantesDTO */ },
  "tieneProyecto": true
}
```

#### HistorialEventoDTO

DTO para un evento del historial.

```java
{
  "eventoId": 3,
  "proyectoId": 1,
  "tipoEvento": "FORMATO_A_EVALUADO",
  "fecha": "2025-11-01T16:00:00",
  "descripcion": "Formato A evaluado",
  "version": 1,
  "resultado": "APROBADO",
  "observaciones": "Muy bien estructurado",
  "responsable": { /* PersonaDTO */ }
}
```

#### HistorialResponseDTO

DTO para respuestas de historial con paginación.

```java
{
  "proyectoId": 1,
  "estudianteId": 1001,
  "tituloProyecto": "Sistema de IA",
  "estadoActual": "FORMATO_A_APROBADO",
  "estadoLegible": "Formato A Aprobado ✅",
  "fase": "FORMATO_A",
  "estudiantes": { /* EstudiantesDTO */ },
  "historial": [ /* Lista de HistorialEventoDTO */ ],
  "paginaActual": 0,
  "tamanoPagina": 20,
  "totalEventos": 15,
  "totalPaginas": 1
}
```

#### ProyectoResumenDTO

DTO para listados y búsquedas de proyectos.

```java
{
  "proyectoId": 1,
  "titulo": "Sistema de IA",
  "estadoActual": "FORMATO_A_APROBADO",
  "estadoLegible": "Formato A Aprobado ✅",
  "fase": "FORMATO_A",
  "modalidad": "INVESTIGACION",
  "programa": "INGENIERIA_SISTEMAS",
  "ultimaActualizacion": "2025-11-01T16:00:00",
  "rol": "DIRECTOR",
  "director": { /* PersonaDTO */ },
  "codirector": { /* PersonaDTO */ },
  "estudiantes": { /* EstudiantesDTO */ }
}
```

#### DTOs Auxiliares

- **FormatoAInfoDTO**: Información del estado del Formato A
- **AnteproyectoInfoDTO**: Información del estado del Anteproyecto
- **ParticipantesDTO**: Información de director y codirector
- **EstudiantesDTO**: Información de estudiante1 y estudiante2
- **PersonaDTO**: Información básica de una persona (id, nombre)
- **EstudianteDTO**: Información de estudiante (id, nombre, email)

### Mapper

El componente `ProyectoMapper` se encarga de convertir entidades a DTOs:

- `toEstadoProyectoDTO(ProyectoEstado)` → EstadoProyectoResponseDTO
- `toHistorialEventoDTO(HistorialEvento)` → HistorialEventoDTO
- `toProyectoResumenDTO(ProyectoEstado)` → ProyectoResumenDTO
- `toEstadoProyectoNoEncontradoDTO(Long)` → DTO de error
- `toEstadoSinProyectoDTO(Long)` → DTO para estudiante sin proyecto

### Beneficios de Usar DTOs

✅ **Desacoplamiento**: Las respuestas no dependen de la estructura de la BD

✅ **Claridad**: Estructura clara y documentada para el frontend

✅ **Flexibilidad**: Fácil agregar campos calculados o derivados

✅ **Versionado**: Permite mantener compatibilidad con versiones anteriores

✅ **Validación**: Facilita validación de datos en la capa de presentación

✅ **Seguridad**: No expone campos sensibles o internos de las entidades

---

## 📡 Eventos Consumidos

El servicio escucha la cola `progress.tracking.queue` y consume los siguientes eventos:

### 1. FormatoAEnviadoEvent

```json
{
  "proyectoId": 1,
  "version": 1,
  "descripcion": "Primera versión del Formato A",
  "timestamp": "2025-11-01T10:00:00",
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR"
}
```

**Origen**: Submission Service (RF2)

**Actualiza estado a**: `FORMATO_A_EN_EVALUACION_1`

### 2. FormatoAReenviadoEvent

```json
{
  "proyectoId": 1,
  "version": 2,
  "descripcion": "Correcciones aplicadas",
  "timestamp": "2025-11-01T14:00:00",
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR"
}
```

**Origen**: Submission Service (RF4)

**Actualiza estado a**: `FORMATO_A_EN_EVALUACION_2` o `FORMATO_A_EN_EVALUACION_3`

### 3. FormatoAEvaluadoEvent

```json
{
  "proyectoId": 1,
  "resultado": "APROBADO",
  "observaciones": "Proyecto bien estructurado",
  "version": 1,
  "rechazadoDefinitivo": false,
  "timestamp": "2025-11-01T16:00:00",
  "usuarioResponsableId": 45,
  "usuarioResponsableNombre": "Dr. Coordinador",
  "usuarioResponsableRol": "COORDINADOR"
}
```

**Origen**: Review Service (RF3)

**Actualiza estado a**:

- `FORMATO_A_APROBADO` (si resultado = APROBADO)
- `FORMATO_A_RECHAZADO_1/2/3` (si resultado = RECHAZADO)
- `FORMATO_A_RECHAZADO_DEFINITIVO` (si rechazadoDefinitivo = true)

### 4. AnteproyectoEnviadoEvent

```json
{
  "proyectoId": 1,
  "titulo": "Sistema de IA para Agricultura",
  "modalidad": "DUPLA",
  "programa": "INGENIERIA_SISTEMAS",
  "directorId": 12,
  "directorNombre": "Dr. Juan Pérez",
  "codirectorId": 15,
  "codirectorNombre": "Dra. Ana Martínez",
  "estudiante1Id": 1001,
  "estudiante1Nombre": "María García",
  "estudiante1Email": "maria.garcia@unicauca.edu.co",
  "estudiante2Id": 1002,
  "estudiante2Nombre": "Carlos López",
  "estudiante2Email": "carlos.lopez@unicauca.edu.co",
  "timestamp": "2025-11-05T10:00:00",
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR"
}
```

**Origen**: Submission Service (RF6)

**Actualiza estado a**: `ANTEPROYECTO_ENVIADO`

**Nota**: Este evento debe incluir información completa del proyecto (director, codirector, estudiantes) para que progress-tracking pueda actualizar/completar toda la información.

### 5. EvaluadoresAsignadosEvent

**Origen**: Review Service (Jefe de Departamento asigna evaluadores)
{
  "proyectoId": 1,
  "evaluadores": [
**Actualiza campo**: `anteproyectoEvaluadoresAsignados = true`

**Estado legible**: "Anteproyecto en evaluación"

**Siguiente paso**: "Esperar evaluación de evaluadores"

    {"id": 20, "nombre": "Dr. Evaluador 1"},
    {"id": 21, "nombre": "Dr. Evaluador 2"}
  ],
  "timestamp": "2025-11-06T09:00:00",
  "usuarioResponsableId": 50,
  "usuarioResponsableNombre": "Jefe de Departamento",
  "usuarioResponsableRol": "JEFE_DEPARTAMENTO"
}
```

**Origen**: Review Service (RF7)

**Actualiza estado a**: `ANTEPROYECTO_EN_EVALUACION`

### 6. AnteproyectoEvaluadoEvent

```json
{
  "proyectoId": 1,
  "resultado": "APROBADO",
  "observaciones": "Excelente propuesta",
  "timestamp": "2025-11-10T15:00:00",
  "usuarioResponsableId": 20,
  "usuarioResponsableNombre": "Dr. Evaluador 1",
  "usuarioResponsableRol": "EVALUADOR"
}
```

**Origen**: Review Service

**Actualiza estado a**: `ANTEPROYECTO_APROBADO` o `ANTEPROYECTO_RECHAZADO`

---

## 🔌 API REST - Endpoints

### 1. Consultar Estado Actual del Proyecto (RF5)

```http
GET /api/progress/proyectos/{id}/estado
```

**Descripción**: Obtiene el estado actual completo de un proyecto.

**Respuesta** (EstadoProyectoResponseDTO):

```json
{
  "proyectoId": 1,
  "titulo": "Sistema de IA para Agricultura",
  "modalidad": "INVESTIGACION",
  "programa": "INGENIERIA_SISTEMAS",
  "estadoActual": "FORMATO_A_EN_EVALUACION_1",
  "estadoLegible": "En primera evaluación - Formato A",
  "fase": "FORMATO_A",
  "ultimaActualizacion": "2025-11-01T10:00:00",
  "siguientePaso": "Esperar evaluación del coordinador",
  "tieneProyecto": true,
  "formatoA": {
    "estado": "EN_EVALUACION",
    "versionActual": 1,
    "intentoActual": 1,
    "maxIntentos": 3,
    "fechaUltimoEnvio": "2025-11-01T10:00:00",
    "fechaUltimaEvaluacion": null
  },
  "anteproyecto": {
    "estado": null,
    "fechaEnvio": null,
    "evaluadoresAsignados": false
  },
  "participantes": {
    "director": {
      "id": 12,
      "nombre": "Dr. Juan Pérez"
    }
  },
  "estudiantes": {
    "estudiante1": {
      "id": 1001,
      "nombre": "María García",
      "email": "maria.garcia@unicauca.edu.co"
    }
  }
}
```

**Usado en**:

- **Frontend**: Dashboard de estudiante (RF5)
- **Frontend**: Dashboard de docente

---

### 2. Consultar Historial de Eventos

```http
GET /api/progress/proyectos/{id}/historial?page=0&size=20&tipoEvento=FORMATO_A_ENVIADO
```

**Parámetros**:

- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)
- `tipoEvento`: Filtro por tipo (opcional, separado por comas)

**Respuesta** (HistorialResponseDTO):

```json
{
  "proyectoId": 1,
  "historial": [
    {
      "eventoId": 3,
      "proyectoId": 1,
      "tipoEvento": "FORMATO_A_EVALUADO",
      "fecha": "2025-11-01T16:00:00",
      "descripcion": "Formato A evaluado: APROBADO",
      "version": 1,
      "resultado": "APROBADO",
      "observaciones": "Muy bien estructurado",
      "responsable": {
        "id": 45,
        "nombre": "Dr. Coordinador"
      }
    },
    {
      "eventoId": 1,
      "proyectoId": 1,
      "tipoEvento": "FORMATO_A_ENVIADO",
      "fecha": "2025-11-01T10:00:00",
      "descripcion": "Primera versión del Formato A",
      "version": 1,
      "resultado": null,
      "observaciones": null,
      "responsable": {
        "id": 12,
        "nombre": "Dr. Juan Pérez"
      }
    }
  ],
  "paginaActual": 0,
  "tamanoPagina": 20,
  "totalEventos": 2,
  "totalPaginas": 1
}
```

**Usado en**:

- **Frontend**: Timeline del proyecto
- **Frontend**: Auditoría completa

---

### 3. Obtener Proyectos del Usuario

```http
GET /api/progress/proyectos/mis-proyectos
Headers: X-User-Id: 12
```

**Descripción**: Lista todos los proyectos donde el usuario es director o codirector.

**Respuesta** (MisProyectosResponseDTO):

```json
{
  "proyectos": [
    {
      "proyectoId": 1,
      "titulo": "Sistema de IA para Agricultura",
      "estadoActual": "FORMATO_A_APROBADO",
      "estadoLegible": "Formato A Aprobado ✅",
      "fase": "FORMATO_A",
      "modalidad": "INVESTIGACION",
      "programa": "INGENIERIA_SISTEMAS",
      "ultimaActualizacion": "2025-11-01T16:00:00",
      "rol": "DIRECTOR",
      "director": {
        "id": 12,
        "nombre": "Dr. Juan Pérez"
      },
      "estudiantes": {
        "estudiante1": {
          "id": 1001,
          "nombre": "María García",
          "email": "maria.garcia@unicauca.edu.co"
        }
      }
    },
    {
      "proyectoId": 5,
      "titulo": "App Móvil para Telemedicina",
      "estadoActual": "ANTEPROYECTO_EN_EVALUACION",
      "estadoLegible": "Anteproyecto en evaluación",
      "fase": "ANTEPROYECTO",
      "modalidad": "PRACTICA_PROFESIONAL",
      "programa": "INGENIERIA_SISTEMAS",
      "ultimaActualizacion": "2025-11-05T14:00:00",
      "rol": "CODIRECTOR",
      "director": {
        "id": 15,
        "nombre": "Dra. Ana Martínez"
      },
      "codirector": {
        "id": 12,
        "nombre": "Dr. Juan Pérez"
      },
      "estudiantes": {
        "estudiante1": {
          "id": 1002,
          "nombre": "Carlos López",
          "email": "carlos.lopez@unicauca.edu.co"
        }
      }
    }
  ],
  "total": 2
}
```

**Usado en**:

- **Frontend**: Dashboard principal del docente/estudiante
- **User Management Service**: Para mostrar proyectos del usuario

---

### 4. Buscar y Filtrar Proyectos

```http
GET /api/progress/proyectos/buscar?estado=FORMATO_A_EN_EVALUACION_1&fase=FORMATO_A&programa=INGENIERIA_SISTEMAS
```

**Parámetros**:

- `estado`: Estado actual (opcional)
- `fase`: Fase del proyecto (opcional)
- `programa`: Programa académico (opcional)

**Respuesta** (BusquedaProyectosResponseDTO):

```json
{
  "resultados": [
    {
      "proyectoId": 3,
      "titulo": "Blockchain para Supply Chain",
      "estadoActual": "FORMATO_A_EN_EVALUACION_1",
      "estadoLegible": "En primera evaluación - Formato A",
      "fase": "FORMATO_A",
      "modalidad": "INVESTIGACION",
      "programa": "INGENIERIA_SISTEMAS",
      "ultimaActualizacion": "2025-11-01T09:00:00",
      "director": {
        "id": 15,
        "nombre": "Dra. María López"
      },
      "estudiantes": {
        "estudiante1": {
          "id": 1003,
          "nombre": "Pedro Martínez",
          "email": "pedro.martinez@unicauca.edu.co"
        }
      }
    }
  ],
  "total": 1,
  "filtros": {
    "estado": "FORMATO_A_EN_EVALUACION_1",
    "fase": "FORMATO_A",
    "programa": "INGENIERIA_SISTEMAS"
  }
}
```

**Usado en**:

- **Frontend**: Panel del coordinador para listar proyectos pendientes (RF3)
- **Frontend**: Panel del jefe de departamento para listar anteproyectos (RF7)

---

### 5. Obtener Estado del Proyecto de un Estudiante

```http
GET /api/progress/estudiantes/{estudianteId}/estado
```

**Descripción**: Obtiene el estado del proyecto de un estudiante sin necesidad de conocer el ID del proyecto.

**Respuesta** (EstadoProyectoResponseDTO):

```json
{
  "tieneProyecto": true,
  "estudianteId": 1001,
  "proyectoId": 1,
  "titulo": "Sistema de IA para Agricultura",
  "modalidad": "INVESTIGACION",
  "programa": "INGENIERIA_SISTEMAS",
  "estadoActual": "FORMATO_A_APROBADO",
  "estadoLegible": "Formato A Aprobado ✅",
  "fase": "FORMATO_A",
  "ultimaActualizacion": "2025-11-01T16:00:00",
  "siguientePaso": "Preparar anteproyecto",
  "formatoA": {
    "estado": "APROBADO",
    "versionActual": 1,
    "intentoActual": 1,
    "maxIntentos": 3,
    "fechaUltimoEnvio": "2025-11-01T10:00:00",
    "fechaUltimaEvaluacion": "2025-11-01T16:00:00"
  },
  "participantes": {
    "director": {
      "id": 12,
      "nombre": "Dr. Juan Pérez"
    }
  },
  "estudiantes": {
    "estudiante1": {
      "id": 1001,
      "nombre": "María García",
      "email": "maria.garcia@unicauca.edu.co"
    }
  }
}
```

**Caso sin proyecto**:

```json
{
  "error": false,
  "mensaje": "El estudiante no tiene proyectos asignados actualmente",
  "estudianteId": 1001,
  "tieneProyecto": false
}
```

**Usado en**:

- **Frontend**: Dashboard del estudiante (RF5)

---

### 6. Obtener Historial del Proyecto de un Estudiante

```http
GET /api/progress/estudiantes/{estudianteId}/historial?page=0&size=20
```

**Descripción**: Obtiene el historial del proyecto de un estudiante sin necesidad de conocer el ID del proyecto.

**Parámetros**:

- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 20)
- `tipoEvento`: Filtro por tipo (opcional)

**Respuesta** (HistorialResponseDTO):

```json
{
  "estudianteId": 1001,
  "proyectoId": 1,
  "tituloProyecto": "Sistema de IA para Agricultura",
  "estadoActual": "FORMATO_A_APROBADO",
  "estadoLegible": "Formato A Aprobado ✅",
  "fase": "FORMATO_A",
  "estudiantes": {
    "estudiante1": {
      "id": 1001,
      "nombre": "María García",
      "email": "maria.garcia@unicauca.edu.co"
    }
  },
  "historial": [
    {
      "eventoId": 3,
      "proyectoId": 1,
      "tipoEvento": "FORMATO_A_EVALUADO",
      "fecha": "2025-11-01T16:00:00",
      "descripcion": "Formato A evaluado: APROBADO",
      "version": 1,
      "resultado": "APROBADO",
      "observaciones": "Muy bien estructurado",
      "responsable": {
        "id": 45,
        "nombre": "Dr. Coordinador"
      }
    }
  ],
  "paginaActual": 0,
  "tamanoPagina": 20,
  "totalEventos": 3,
  "totalPaginas": 1
}
```

**Usado en**:

- **Frontend**: Timeline del proyecto para estudiantes

---

## 🚀 Instalación y Configuración

### Requisitos Previos

- Java 17+
- Docker y Docker Compose
- Maven 3.8+

### Variables de Entorno

Crear `application.yml`:

```yaml
spring:
  application:
    name: progress-tracking-service

  datasource:
    url: jdbc:postgresql://localhost:5432/progress_tracking_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

rabbitmq:
  exchange:
    name: project.events.exchange
  queue:
    name: progress.tracking.queue
  routing:
    key: project.#

server:
  port: 8084
```

### Iniciar con Docker Compose

```bash
# 1. Iniciar infraestructura
docker-compose up -d

# 2. Verificar que los servicios estén corriendo
docker-compose ps

# 3. Compilar y ejecutar el servicio
mvn clean install
mvn spring-boot:run
```

### Verificar Funcionamiento

```bash
# Health check
curl http://localhost:8084/actuator/health

# RabbitMQ Management UI
open http://localhost:15672
# Credenciales: guest / guest

# PgAdmin UI
open http://localhost:5050
# Credenciales: admin@admin.com / admin
```

---

## 🔗 Integración con Otros Microservicios

### Submission Service

**Debe publicar eventos cuando**:

- Un docente sube el Formato A (RF2) → `FormatoAEnviadoEvent`
- Un docente reenvía el Formato A (RF4) → `FormatoAReenviadoEvent`
- Un docente sube el anteproyecto (RF6) → `AnteproyectoEnviadoEvent`

**Configuración RabbitMQ**:

```java
rabbitTemplate.convertAndSend(
    "project.events.exchange",
    "project.formatoa.submitted",
    formatoAEnviadoEvent
);
```

### Review Service

**Debe publicar eventos cuando**:

- Un coordinador evalúa el Formato A (RF3) → `FormatoAEvaluadoEvent`
- Se asignan evaluadores al anteproyecto (RF7) → `EvaluadoresAsignadosEvent`
- Un evaluador evalúa el anteproyecto → `AnteproyectoEvaluadoEvent`

**Configuración RabbitMQ**:

```java
rabbitTemplate.convertAndSend(
    "project.events.exchange",
    "project.formatoa.evaluated",
    formatoAEvaluadoEvent
);
```

### Frontend

**Debe consumir los siguientes endpoints**:

1. **Dashboard de Estudiante (RF5)**:

```javascript
GET /api/progress/proyectos/{id}/estado
```

2. **Dashboard de Docente**:

```javascript
GET /api/progress/proyectos/mis-proyectos
Headers: { 'X-User-Id': userId }
```

3. **Panel de Coordinador (RF3)**:

```javascript
GET /api/progress/proyectos/buscar?fase=FORMATO_A&estado=FORMATO_A_EN_EVALUACION_1
```

4. **Panel Jefe de Departamento (RF7)**:

```javascript
GET /api/progress/proyectos/buscar?fase=ANTEPROYECTO&estado=ANTEPROYECTO_ENVIADO
```

5. **Timeline del Proyecto**:

```javascript
GET /api/progress/proyectos/{id}/historial?page=0&size=20
```

---

## 📊 Estados del Proyecto

### Estados de Formato A

| Estado | Descripción | Siguiente Paso |
|--------|-------------|----------------|
| FORMATO_A_EN_EVALUACION_1 | Primera evaluación en curso | Esperar evaluación del coordinador |
| FORMATO_A_RECHAZADO_1 | Primera evaluación rechazada | Corregir y reenviar |
| FORMATO_A_EN_EVALUACION_2 | Segunda evaluación en curso | Esperar evaluación del coordinador |
| FORMATO_A_RECHAZADO_2 | Segunda evaluación rechazada | Corregir y reenviar (última oportunidad) |
| FORMATO_A_EN_EVALUACION_3 | Tercera evaluación en curso | Esperar evaluación (última oportunidad) |
| FORMATO_A_RECHAZADO_3 | Tercera evaluación rechazada | Consultar con coordinador |
| FORMATO_A_APROBADO | Formato A aprobado | Subir anteproyecto |
| FORMATO_A_RECHAZADO_DEFINITIVO | Rechazado definitivamente | Iniciar nuevo proyecto |

### Estados de Anteproyecto

| Estado | Descripción | Siguiente Paso |
|--------|-------------|----------------|
| ANTEPROYECTO_ENVIADO | Anteproyecto enviado | Esperar asignación de evaluadores |
| ANTEPROYECTO_EN_EVALUACION | En evaluación | Esperar evaluación de evaluadores |
| ANTEPROYECTO_APROBADO | Anteproyecto aprobado | Preparar defensa |

---

## 🧪 Testing

### Endpoint Temporal (Solo para Desarrollo)

```http
POST /api/progress/eventos
Content-Type: application/json

{
  "proyectoId": 1,
  "tipoEvento": "FORMATO_A_ENVIADO",
  "descripcion": "Prueba manual",
  "version": 1,
  "usuarioResponsableId": 12,
  "nuevoEstado": "FORMATO_A_EN_EVALUACION_1"
}
```

⚠️ **Advertencia**: Este endpoint será eliminado en producción. En producción, todos los eventos deben llegar vía RabbitMQ.

---

## 🔒 Consideraciones de Seguridad

1. **Autenticación**: Usar JWT tokens en el header `Authorization`
2. **Autorización**: Validar que el usuario solo pueda ver sus propios proyectos
3. **Rate Limiting**: Implementar límites de consultas por usuario
4. **CORS**: Configurar orígenes permitidos en producción

---

## 📝 Notas Importantes

- ✅ Este servicio **NO modifica datos directamente** (solo consulta)
- ✅ Todos los cambios de estado provienen de **eventos de RabbitMQ**
- ✅ El historial de eventos es **inmutable** (nunca se borra ni modifica)
- ✅ La vista materializada se **recalcula automáticamente** con cada evento
- ✅ Soporta **auditoría completa** del ciclo de vida del proyecto

---

## 🆘 Troubleshooting

### El servicio no recibe eventos

1. Verificar que RabbitMQ esté corriendo:

```bash
docker-compose ps rabbitmq
```

2. Verificar el exchange y la cola en RabbitMQ UI:
   [http://localhost:15672/#/queues](http://localhost:15672/#/queues)

3. Verificar logs del consumer:

```bash
docker-compose logs -f progress-tracking-service
```

### Los estados no se actualizan correctamente

1. Verificar que el evento tenga todos los campos requeridos
2. Revisar logs de `ProjectStateService`
3. Verificar que el `proyectoId` exista en la base de datos

---

## 📚 Referencias

- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP](https://spring.io/projects/spring-amqp)

---

**Versión**: 1.0.0

**Última actualización**: Noviembre 2025
