# 📋 Review Service - Sistema de Evaluación Académica

Microservicio de evaluación académica que implementa el **Patrón Template Method** para gestionar la evaluación de Formato A y Anteproyectos en el sistema de gestión de trabajos de grado.

## 🎯 Características Principales

- ✅ **Patrón Template Method**: Algoritmo común con pasos específicos por tipo de documento
- ✅ **Evaluación de Formato A**: Por coordinadores del programa
- ✅ **Evaluación de Anteproyectos**: Por dos evaluadores independientes
- ✅ **Comunicación HTTP**: WebClient para consultar Submission Service
- ✅ **Mensajería Asíncrona**: RabbitMQ para notificaciones
- ✅ **Base de Datos**: PostgreSQL para persistencia de evaluaciones
- ✅ **Java 21**: Records, Virtual Threads, Pattern Matching

## 🏗️ Arquitectura del Patrón Template Method

```
┌─────────────────────────────────────────────────────────────┐
│              EvaluationTemplate (Abstract)                   │
├─────────────────────────────────────────────────────────────┤
│  + evaluate(request): EvaluationResultDTO [FINAL]           │
│  # validatePermissions(request): void [COMMON]               │
│  # saveEvaluation(request, doc): Evaluation [COMMON]         │
│  # buildSuccessResult(eval, notified): DTO [COMMON]          │
│                                                               │
│  # fetchDocument(id): DocumentInfo [ABSTRACT]                │
│  # validateDocumentState(doc): void [ABSTRACT]               │
│  # updateSubmissionService(id, decision, obs) [ABSTRACT]     │
│  # publishNotificationEvent(eval, doc): boolean [ABSTRACT]   │
│  # getDocumentType(): DocumentType [ABSTRACT]                │
│  # getRequiredRole(): EvaluatorRole [ABSTRACT]               │
└─────────────────────────────────────────────────────────────┘
                          ▲                ▲
                          │                │
         ┌────────────────┘                └────────────────┐
         │                                                  │
┌────────────────────────┐                  ┌──────────────────────────┐
│FormatoAEvaluationService│                  │AnteproyectoEvaluation   │
├────────────────────────┤                  │Service                   │
│ + getDocumentType()    │                  ├──────────────────────────┤
│   → FORMATO_A          │                  │ + getDocumentType()      │
│ + getRequiredRole()    │                  │   → ANTEPROYECTO         │
│   → COORDINADOR        │                  │ + getRequiredRole()      │
│ + fetchDocument()      │                  │   → EVALUADOR            │
│ + validateState()      │                  │ + fetchDocument()        │
│   → EN_REVISION        │                  │ + validateState()        │
│ + updateSubmission()   │                  │   → Verificar asignación │
│ + publishEvent()       │                  │ + updateSubmission()     │
│   → Siempre publica    │                  │   → Solo si ambos eval.  │
└────────────────────────┘                  │ + publishEvent()         │
                                            │   → Solo si ambos eval.  │
                                            └──────────────────────────┘
```

## 📊 Modelo de Datos

### Tabla: evaluaciones
```sql
CREATE TABLE evaluaciones (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(50) NOT NULL,
    document_id BIGINT NOT NULL,
    decision VARCHAR(20) NOT NULL,
    observaciones TEXT,
    evaluator_id BIGINT NOT NULL,
    evaluator_role VARCHAR(50) NOT NULL,
    fecha_evaluacion TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_eval_document (document_type, document_id),
    INDEX idx_eval_evaluator (evaluator_id)
);
```

### Tabla: asignaciones_evaluadores
```sql
CREATE TABLE asignaciones_evaluadores (
    id BIGSERIAL PRIMARY KEY,
    anteproyecto_id BIGINT NOT NULL UNIQUE,
    evaluador1_id BIGINT NOT NULL,
    evaluador2_id BIGINT NOT NULL,
    evaluador1_decision VARCHAR(20),
    evaluador2_decision VARCHAR(20),
    evaluador1_observaciones TEXT,
    evaluador2_observaciones TEXT,
    fecha_asignacion TIMESTAMP NOT NULL,
    fecha_completado TIMESTAMP,
    estado VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_asig_anteproyecto (anteproyecto_id),
    INDEX idx_asig_eval1 (evaluador1_id),
    INDEX idx_asig_eval2 (evaluador2_id)
);
```

## 🔌 API Endpoints

### 1. Formato A - Evaluación

#### Listar Formatos A Pendientes
```bash
GET /api/review/formatoA/pendientes?page=0&size=10
Headers:
  X-User-Role: COORDINADOR

Response:
{
  "success": true,
  "message": null,
  "data": {
    "content": [
      {
        "formatoAId": 1,
        "titulo": "Implementación de IA en agricultura",
        "docenteDirectorNombre": "Dr. Juan Pérez",
        "docenteDirectorEmail": "juan.perez@unicauca.edu.co",
        "estudiantesEmails": ["estudiante1@unicauca.edu.co"],
        "fechaCarga": "2025-10-24T10:30:00",
        "estado": "EN_REVISION"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  },
  "errors": null
}
```

#### Evaluar Formato A
```bash
POST /api/review/formatoA/{id}/evaluar
Headers:
  X-User-Id: 5
  X-User-Role: COORDINADOR
Content-Type: application/json

Body:
{
  "decision": "APROBADO",
  "observaciones": "El formato cumple con todos los requisitos"
}

Response:
{
  "success": true,
  "message": "Formato A evaluado exitosamente",
  "data": {
    "evaluationId": 123,
    "documentId": 1,
    "documentType": "FORMATO_A",
    "decision": "APROBADO",
    "observaciones": "El formato cumple con todos los requisitos",
    "fechaEvaluacion": "2025-10-26T14:30:00",
    "notificacionEnviada": true
  },
  "errors": null
}
```

### 2. Anteproyectos - Gestión y Evaluación

#### Asignar Evaluadores
```bash
POST /api/review/anteproyectos/asignar
Headers:
  X-User-Id: 10
  X-User-Role: JEFE_DEPARTAMENTO
Content-Type: application/json

Body:
{
  "anteproyectoId": 5,
  "evaluador1Id": 15,
  "evaluador2Id": 20
}

Response:
{
  "success": true,
  "message": "Evaluadores asignados exitosamente",
  "data": {
    "asignacionId": 1,
    "anteproyectoId": 5,
    "tituloAnteproyecto": "Sistema de recomendación basado en ML",
    "evaluador1": {
      "id": 15,
      "nombre": "Evaluador 15",
      "email": "evaluador15@unicauca.edu.co",
      "decision": null,
      "observaciones": null
    },
    "evaluador2": {
      "id": 20,
      "nombre": "Evaluador 20",
      "email": "evaluador20@unicauca.edu.co",
      "decision": null,
      "observaciones": null
    },
    "estado": "PENDIENTE",
    "fechaAsignacion": "2025-10-26T15:00:00",
    "fechaCompletado": null,
    "finalDecision": null
  },
  "errors": null
}
```

#### Listar Asignaciones
```bash
GET /api/review/anteproyectos/asignaciones?estado=PENDIENTE&page=0&size=10
Headers:
  X-User-Id: 15
  X-User-Role: EVALUADOR

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "asignacionId": 1,
        "anteproyectoId": 5,
        "tituloAnteproyecto": "Sistema de recomendación basado en ML",
        "evaluador1": {...},
        "evaluador2": {...},
        "estado": "PENDIENTE"
      }
    ],
    "totalElements": 1
  }
}
```

#### Evaluar Anteproyecto
```bash
POST /api/review/anteproyectos/{id}/evaluar
Headers:
  X-User-Id: 15
  X-User-Role: EVALUADOR
Content-Type: application/json

Body:
{
  "decision": "APROBADO",
  "observaciones": "El anteproyecto presenta una metodología sólida"
}

Response:
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 124,
    "documentId": 5,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "El anteproyecto presenta una metodología sólida",
    "fechaEvaluacion": "2025-10-26T16:00:00",
    "notificacionEnviada": false  // Aún falta el segundo evaluador
  }
}
```

## 🔄 Flujos de Evaluación

### Flujo 1: Evaluación de Formato A

```
1. Coordinador solicita lista de Formatos A pendientes
   → GET /api/review/formatoA/pendientes
   
2. Sistema consulta Submission Service
   → GET http://submission:8082/api/submissions/formatoA/pendientes
   
3. Coordinador evalúa un Formato A
   → POST /api/review/formatoA/{id}/evaluar
   
4. Sistema ejecuta Template Method:
   a) Valida permisos (rol = COORDINADOR)
   b) Obtiene documento de Submission Service
   c) Valida estado (debe ser EN_REVISION)
   d) Guarda evaluación en BD local
   e) Actualiza estado en Submission Service
      → PATCH http://submission:8082/api/submissions/formatoA/{id}/estado
   f) Publica evento en RabbitMQ
      → exchange: evaluation.exchange
      → routing-key: evaluation.completed
   
5. Notification Service recibe evento y notifica
```

### Flujo 2: Evaluación de Anteproyecto

```
1. Jefe de Departamento asigna evaluadores
   → POST /api/review/anteproyectos/asignar
   → Se crea registro en asignaciones_evaluadores
   
2. Evaluador 1 consulta sus asignaciones
   → GET /api/review/anteproyectos/asignaciones
   
3. Evaluador 1 evalúa el anteproyecto
   → POST /api/review/anteproyectos/{id}/evaluar
   → Se guarda evaluación en BD
   → Se actualiza evaluador1_decision en asignación
   → Estado cambia a EN_EVALUACION
   → NO se publica evento (falta evaluador 2)
   
4. Evaluador 2 evalúa el anteproyecto
   → POST /api/review/anteproyectos/{id}/evaluar
   → Se guarda evaluación en BD
   → Se actualiza evaluador2_decision en asignación
   → Se calcula decisión final:
      * AMBOS APROBADO → APROBADO
      * AL MENOS UNO RECHAZADO → RECHAZADO
   → Se actualiza estado en Submission Service
   → Estado cambia a COMPLETADA
   → SÍ se publica evento en RabbitMQ
   
5. Notification Service notifica resultado final
```

## 🚀 Ejecución Local

### Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL 15
- RabbitMQ 3.12

### Configuración

1. **Base de datos**
```bash
psql -U postgres
CREATE DATABASE review_db;
CREATE USER review_user WITH PASSWORD 'review_pass';
GRANT ALL PRIVILEGES ON DATABASE review_db TO review_user;
```

2. **Variables de entorno**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/review_db
export DATABASE_USERNAME=review_user
export DATABASE_PASSWORD=review_pass
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
export SUBMISSION_URL=http://localhost:8082
export IDENTITY_URL=http://localhost:8081
export JWT_SECRET=your-secret-key-here
```

3. **Compilar y ejecutar**
```bash
cd review-service
mvn clean package
java -jar target/review-service-1.0.0.jar
```

El servicio estará disponible en: `http://localhost:8084`

## 🐋 Ejecución con Docker

### Compilar imagen
```bash
docker build -t review-service:latest .
```

### Ejecutar con docker-compose (desde raíz del proyecto)
```bash
docker-compose up review postgres-review rabbitmq -d
```

### Ver logs
```bash
docker logs -f review-service
```

## 🧪 Tests

### Ejecutar tests unitarios
```bash
mvn test
```

### Ejecutar tests de integración
```bash
mvn verify
```

### Tests incluidos
- ✅ Template Method pattern validation
- ✅ Formato A evaluation flow
- ✅ Anteproyecto evaluation flow (2 evaluadores)
- ✅ Permission validation
- ✅ State validation
- ✅ RabbitMQ event publishing
- ✅ WebClient communication

## 📈 Monitoreo

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

### Métricas
```bash
curl http://localhost:8084/actuator/metrics
```

### RabbitMQ Management UI
```
http://localhost:15672
Usuario: admin
Contraseña: admin123
```

## 🔐 Seguridad

- **NO valida JWT**: Confía en headers X-User-* del Gateway
- **Validación de roles**: A nivel de controlador y Template Method
- **Headers requeridos**:
  - `X-User-Id`: ID del usuario autenticado
  - `X-User-Role`: Rol del usuario (COORDINADOR, EVALUADOR, JEFE_DEPARTAMENTO)
  - `X-User-Email`: Email del usuario

## 📝 Logs

Los logs incluyen:
- Inicio y fin de evaluaciones
- Comunicación con servicios externos
- Publicación de eventos RabbitMQ
- Errores y excepciones con stack traces

Ejemplo:
```
2025-10-26 14:30:00 - Iniciando evaluación - Documento: 1, Tipo: FORMATO_A
2025-10-26 14:30:01 - Permisos validados correctamente para rol: COORDINADOR
2025-10-26 14:30:02 - Estado actualizado en Submission Service: formatoAId=1
2025-10-26 14:30:03 - ✓ Evento FORMATO_A_EVALUATED publicado en RabbitMQ
2025-10-26 14:30:03 - Evaluación completada exitosamente - ID: 123
```

## 🛠️ Tecnologías Utilizadas

- **Java 21**: Records, Virtual Threads
- **Spring Boot 3.2.0**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **PostgreSQL 15**: Base de datos
- **RabbitMQ 3.12**: Mensajería asíncrona
- **WebClient**: Cliente HTTP reactivo
- **Docker**: Containerización
- **Maven**: Gestión de dependencias

## 📚 Referencias

- [Patrón Template Method - Gang of Four](https://refactoring.guru/design-patterns/template-method)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)

## 👥 Contribución

Este servicio forma parte del sistema de gestión de trabajos de grado de la Universidad del Cauca.

## 📄 Licencia

Proyecto académico - Universidad del Cauca 2025

