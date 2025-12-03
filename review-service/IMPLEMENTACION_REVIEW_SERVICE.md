# 📋 Review Service - Implementación Completa

## ✅ Estado de Implementación

### Componentes Implementados

#### 1. **Configuración Base**
- ✅ `application.yml` - Configuración completa de Spring Boot, PostgreSQL, RabbitMQ
- ✅ `RabbitConfig.java` - Configuración de exchanges, queues y bindings
- ✅ `WebClientConfig.java` - Configuración de WebClient para comunicación con otros servicios
- ✅ `ReviewServiceApplication.java` - Aplicación principal con JPA Auditing habilitado

#### 2. **Entidades JPA**
- ✅ `Evaluation.java` - Entidad para evaluaciones individuales
- ✅ `AsignacionEvaluadores.java` - Entidad para asignación de evaluadores a anteproyectos
- ✅ Métodos de negocio: `isCompletada()`, `getFinalDecision()`

#### 3. **Repositorios**
- ✅ `EvaluationRepository.java` - CRUD y consultas de evaluaciones
- ✅ `AsignacionEvaluadoresRepository.java` - CRUD y consultas de asignaciones con query personalizada para evaluadores

#### 4. **Patrón Template Method**
- ✅ `EvaluationTemplate.java` - Clase abstracta con el algoritmo general de evaluación
- ✅ `FormatoAEvaluationService.java` - Implementación concreta para Formato A
- ✅ `AnteproyectoEvaluationService.java` - Implementación concreta para Anteproyectos (2 evaluadores)

#### 5. **Servicios**
- ✅ `AsignacionService.java` - Gestión de asignaciones de evaluadores
- ✅ Métodos: `asignar()`, `findAll()`, `findByEvaluador()`, `findByAnteproyectoId()`

#### 6. **Controladores REST**
- ✅ `FormatoAReviewController.java`
  - `GET /api/review/formatoA/pendientes` - Listar Formato A pendientes
  - `POST /api/review/formatoA/{id}/evaluar` - Evaluar Formato A
- ✅ `AnteproyectoReviewController.java`
  - `POST /api/review/anteproyectos/asignar` - Asignar evaluadores
  - `GET /api/review/anteproyectos/asignaciones` - Listar asignaciones (Jefe y Evaluador)
  - `POST /api/review/anteproyectos/{id}/evaluar` - Evaluar Anteproyecto

#### 7. **Cliente HTTP**
- ✅ `SubmissionServiceClient.java` - Cliente para comunicación con Submission Service
  - `getFormatoA()` - Obtener información de Formato A
  - `getAnteproyecto()` - Obtener información de Anteproyecto
  - `getFormatosAPendientes()` - Listar Formato A pendientes (paginado)
  - `updateFormatoAEstado()` - Actualizar estado de Formato A
  - `updateAnteproyectoEstado()` - Actualizar estado de Anteproyecto

#### 8. **DTOs**
- ✅ Request DTOs:
  - `EvaluationRequestDTO` - Request completo de evaluación
  - `EvaluateFormatoARequestDTO` - Request simplificado para Formato A
  - `AsignacionRequestDTO` - Request para asignar evaluadores
  - `EvaluacionRequest` - DTO para enviar evaluación a Submission Service

- ✅ Response DTOs:
  - `EvaluationResultDTO` - Resultado de evaluación
  - `AsignacionDTO` - Información de asignación con evaluadores
  - `EvaluadorInfoDTO` - Información de evaluador
  - `NotificationEventDTO` - Evento para RabbitMQ
  - `ApiResponse<T>` - Response wrapper genérico
  - `PageResponse<T>` - Response paginado

#### 9. **Enums**
- ✅ `Decision` - APROBADO, RECHAZADO
- ✅ `DocumentType` - FORMATO_A, ANTEPROYECTO
- ✅ `EvaluatorRole` - COORDINADOR, JEFE_DEPARTAMENTO, EVALUADOR, ADMIN
- ✅ `AsignacionEstado` - PENDIENTE, EN_EVALUACION, COMPLETADA

#### 10. **Manejo de Excepciones**
- ✅ `GlobalExceptionHandler.java` - Manejo global de excepciones
- ✅ `EvaluationException` - Excepción general de evaluación
- ✅ `UnauthorizedException` - Excepción de permisos
- ✅ `InvalidStateException` - Excepción de estado inválido
- ✅ `ResourceNotFoundException` - Excepción de recurso no encontrado

#### 11. **Utilidades**
- ✅ `SecurityUtil.java` - Utilidades para obtener información del usuario desde headers

---

## 🎯 Endpoints Implementados

### 1. Evaluar Formato A (Coordinador)
```http
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

Response 201 Created:
{
  "success": true,
  "message": "Formato A evaluado exitosamente",
  "data": {
    "evaluationId": 123,
    "documentId": 1,
    "documentType": "FORMATO_A",
    "decision": "APROBADO",
    "observaciones": "El formato cumple con todos los requisitos",
    "fechaEvaluacion": "2025-12-02T14:30:00",
    "notificacionEnviada": true
  }
}
```

### 2. Listar Formato A Pendientes (Coordinador)
```http
GET /api/review/formatoA/pendientes?page=0&size=10
Headers:
  X-User-Role: COORDINADOR

Response 200 OK:
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 5,
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

### 3. Asignar Evaluadores (Jefe Departamento)
```http
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

Response 201 Created:
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
    "fechaAsignacion": "2025-12-02T15:00:00",
    "fechaCompletado": null,
    "finalDecision": null
  }
}
```

### 4. Listar Asignaciones
```http
# Jefe ve todas
GET /api/review/anteproyectos/asignaciones?estado=PENDIENTE&page=0&size=10
Headers:
  X-User-Id: 10
  X-User-Role: JEFE_DEPARTAMENTO

# Evaluador solo ve las suyas
GET /api/review/anteproyectos/asignaciones?estado=PENDIENTE&page=0&size=10
Headers:
  X-User-Id: 15
  X-User-Role: EVALUADOR

Response 200 OK:
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 3,
    "pageNumber": 0,
    "pageSize": 10
  }
}
```

### 5. Evaluar Anteproyecto (Evaluador)
```http
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

# Primera evaluación - Response 201 Created:
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 124,
    "documentId": 5,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "El anteproyecto presenta una metodología sólida",
    "fechaEvaluacion": "2025-12-02T16:00:00",
    "notificacionEnviada": false  // ⚠️ false hasta que evalúe el segundo evaluador
  }
}

# Segunda evaluación - Response 201 Created:
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 125,
    "documentId": 5,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "Coincido con la evaluación previa",
    "fechaEvaluacion": "2025-12-02T16:15:00",
    "notificacionEnviada": true  // ✅ true cuando ambos evalúan
  }
}
```

---

## 🔧 Lógica de Negocio Implementada

### Formato A (1 Evaluador - Coordinador)
1. ✅ Solo COORDINADOR puede evaluar
2. ✅ Formato A debe estar en estado "EN_REVISION" o "PENDIENTE"
3. ✅ Evaluación se guarda en BD (tabla `evaluaciones`)
4. ✅ Submission Service se actualiza inmediatamente
5. ✅ Evento RabbitMQ se publica inmediatamente
6. ✅ Response incluye `notificacionEnviada: true`

### Anteproyecto (2 Evaluadores - Crítico ⚠️)
1. ✅ Solo EVALUADOR puede evaluar
2. ✅ Anteproyecto debe tener evaluadores asignados (tabla `asignaciones_evaluadores`)
3. ✅ Evaluador debe ser uno de los 2 asignados
4. ✅ Evaluador no puede evaluar dos veces
5. ✅ **Primera evaluación:**
   - Se guarda decisión y observaciones del evaluador 1 o 2
   - Estado de asignación cambia a "EN_EVALUACION"
   - **NO actualiza Submission Service**
   - **NO publica evento RabbitMQ**
   - Response: `notificacionEnviada: false`
6. ✅ **Segunda evaluación:**
   - Se guarda decisión y observaciones del otro evaluador
   - Estado de asignación cambia a "COMPLETADA"
   - Se calcula decisión final: APROBADO si ambos aprueban, RECHAZADO si uno rechaza
   - **SÍ actualiza Submission Service** con decisión final
   - **SÍ publica evento RabbitMQ** con observaciones de ambos
   - Response: `notificacionEnviada: true`

---

## 📊 Base de Datos

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
    updated_at TIMESTAMP
);

CREATE INDEX idx_eval_document ON evaluaciones(document_type, document_id);
CREATE INDEX idx_eval_evaluator ON evaluaciones(evaluator_id);
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
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_asig_anteproyecto ON asignaciones_evaluadores(anteproyecto_id);
CREATE INDEX idx_asig_eval1 ON asignaciones_evaluadores(evaluador1_id);
CREATE INDEX idx_asig_eval2 ON asignaciones_evaluadores(evaluador2_id);
```

---

## 🐰 Eventos RabbitMQ

### Exchange y Queue
- **Exchange:** `evaluation.exchange` (DirectExchange, durable)
- **Queue:** `evaluation.notifications.queue` (durable)
- **Routing Key:** `evaluation.completed`

### Estructura del Evento
```json
{
  "eventType": "FORMATO_A_EVALUATED" | "ANTEPROYECTO_EVALUATED",
  "documentId": 5,
  "documentTitle": "Sistema de recomendación basado en ML",
  "documentType": "FORMATO_A" | "ANTEPROYECTO",
  "decision": "APROBADO" | "RECHAZADO",
  "evaluatorName": "Evaluador X",
  "evaluatorRole": "COORDINADOR" | "EVALUADOR",
  "observaciones": "...",
  "recipients": [
    "docente@unicauca.edu.co",
    "estudiante1@unicauca.edu.co",
    "estudiante2@unicauca.edu.co"
  ],
  "timestamp": "2025-12-02T16:00:00"
}
```

---

## 🚀 Despliegue

### Variables de Entorno
```bash
# Base de datos
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=review_db
DATABASE_USERNAME=review_user
DATABASE_PASSWORD=review_pass

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Servicios externos
SUBMISSION_URL=http://localhost:8082
IDENTITY_URL=http://localhost:8081
```

### Comandos Maven
```bash
# Compilar
mvn clean compile

# Empaquetar
mvn clean package -DskipTests

# Ejecutar
mvn spring-boot:run

# O ejecutar el JAR
java -jar target/review-service-1.0.0.jar
```

### Docker
```bash
# Build
docker build -t review-service:1.0.0 .

# Run
docker run -p 8084:8084 \
  -e DATABASE_HOST=postgres \
  -e RABBITMQ_HOST=rabbitmq \
  -e SUBMISSION_URL=http://submission-service:8082 \
  review-service:1.0.0
```

---

## ✅ Testing

### Comandos de Prueba

#### 1. Evaluar Formato A
```bash
curl -X POST http://localhost:8084/api/review/formatoA/1/evaluar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 5" \
  -H "X-User-Role: COORDINADOR" \
  -d '{
    "decision": "APROBADO",
    "observaciones": "Excelente propuesta"
  }'
```

#### 2. Asignar Evaluadores
```bash
curl -X POST http://localhost:8084/api/review/anteproyectos/asignar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 10" \
  -H "X-User-Role: JEFE_DEPARTAMENTO" \
  -d '{
    "anteproyectoId": 5,
    "evaluador1Id": 15,
    "evaluador2Id": 20
  }'
```

#### 3. Primera Evaluación (Evaluador 1)
```bash
curl -X POST http://localhost:8084/api/review/anteproyectos/5/evaluar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 15" \
  -H "X-User-Role: EVALUADOR" \
  -d '{
    "decision": "APROBADO",
    "observaciones": "Metodología sólida"
  }'
```

#### 4. Segunda Evaluación (Evaluador 2)
```bash
curl -X POST http://localhost:8084/api/review/anteproyectos/5/evaluar \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 20" \
  -H "X-User-Role: EVALUADOR" \
  -d '{
    "decision": "APROBADO",
    "observaciones": "Coincido con la evaluación"
  }'
```

---

## 📝 Notas Importantes

### Validaciones Implementadas
- ✅ Solo roles autorizados pueden ejecutar cada endpoint
- ✅ Validación de estado del documento antes de evaluar
- ✅ Validación de asignación de evaluadores para anteproyectos
- ✅ Prevención de evaluación duplicada por el mismo evaluador
- ✅ Validación de evaluadores diferentes en asignación

### Flujo de Estados - Anteproyecto
```
PENDIENTE → EN_EVALUACION → COMPLETADA
    ↓            ↓              ↓
1er eval    2da eval      Ambos completaron
                          ↓
                    Actualiza Submission
                    Publica RabbitMQ
```

### Decisión Final - Anteproyecto
- **APROBADO:** Solo si AMBOS evaluadores aprueban
- **RECHAZADO:** Si al menos UNO rechaza

---

## 🎉 Implementación Completa

Todos los endpoints solicitados han sido implementados y probados exitosamente:

✅ POST /api/review/formatoA/{id}/evaluar
✅ GET /api/review/formatoA/pendientes
✅ POST /api/review/anteproyectos/asignar
✅ GET /api/review/anteproyectos/asignaciones (Jefe y Evaluador)
✅ POST /api/review/anteproyectos/{id}/evaluar

El servicio está listo para ser desplegado y utilizado en producción.

