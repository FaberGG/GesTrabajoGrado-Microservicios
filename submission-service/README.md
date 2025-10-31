# 📦 Submission Service

Microservicio para la gestión de entregas de trabajos de grado (Formato A y Anteproyectos).

## 🎯 Responsabilidades

- **RF2**: Crear Formato A inicial (hasta 3 intentos)
- **RF4**: Reenviar Formato A tras rechazo
- **RF6**: Subir Anteproyecto
- Gestión de estados de documentos
- Almacenamiento de archivos PDF
- Publicación de eventos a RabbitMQ

## 🏗 Arquitectura

### Entidades de Dominio

- **ProyectoGrado**: Información del proyecto y metadatos
- **FormatoA**: Versiones del Formato A (v1, v2, v3)
- **Anteproyecto**: Documento final del anteproyecto

### Estados

#### Estado Proyecto
- `EN_PROCESO`: Proyecto creado, esperando aprobación
- `APROBADO`: Formato A aprobado
- `RECHAZADO`: Formato A rechazado (puede reenviar)
- `RECHAZADO_DEFINITIVO`: 3 rechazos, no puede continuar

#### Estado Formato
- `PENDIENTE`: Esperando evaluación
- `APROBADO`: Aprobado por coordinador
- `RECHAZADO`: Rechazado, debe reenviar

## 📡 API Endpoints

### Formato A

```http
POST /api/submissions/formatoA
Content-Type: multipart/form-data
Headers: X-User-Role: DOCENTE, X-User-Id: {id}

Parts:
- data (JSON): { titulo, modalidad, objetivoGeneral, ... }
- pdf (File): Documento PDF del Formato A
- carta (File): Carta de aceptación (obligatoria si PRACTICA_PROFESIONAL)

Response: 201 Created { "id": 1 }
```

```http
GET /api/submissions/formatoA/{id}
Response: 200 OK { FormatoAView }
```

```http
GET /api/submissions/formatoA?docenteId=101&page=0&size=20
Response: 200 OK { FormatoAPage }
```

```http
POST /api/submissions/formatoA/{proyectoId}/nueva-version
Content-Type: multipart/form-data
Headers: X-User-Role: DOCENTE

Parts:
- pdf (File): Nueva versión del documento
- carta (File): Carta si aplica

Response: 201 Created { "id": 1 }
```

```http
PATCH /api/submissions/formatoA/{versionId}/estado
Content-Type: application/json
Headers: X-Service: review

Body: {
  "estado": "APROBADO",
  "observaciones": "Excelente propuesta",
  "evaluadoPor": 50
}

Response: 200 OK
```

### Anteproyecto

```http
POST /api/submissions/anteproyecto
Content-Type: multipart/form-data
Headers: X-User-Role: DOCENTE, X-User-Id: {id}

Parts:
- data (JSON): { "proyectoId": 1 }
- pdf (File): Documento del anteproyecto

Response: 201 Created { "id": 1 }
```

```http
GET /api/submissions/anteproyecto?page=0&size=20
Response: 200 OK { AnteproyectoPage }
```

```http
PATCH /api/submissions/anteproyecto/{id}/estado
Headers: X-Service: review
Body: { "estado": "APROBADO", "observaciones": "..." }
Response: 200 OK
```

## 🐰 Eventos RabbitMQ

### Publicados por Submission Service

| Exchange | Routing Key | Payload | Cuándo |
|----------|-------------|---------|--------|
| formato-a-exchange | formato-a.enviado | { proyectoId, version, titulo } | Formato A v1 creado |
| formato-a-exchange | formato-a.reenviado | { proyectoId, version, titulo } | Nueva versión enviada |
| anteproyecto-exchange | anteproyecto.enviado | { proyectoId, titulo } | Anteproyecto subido |
| proyecto-exchange | proyecto.rechazado-definitivamente | { proyectoId, titulo } | 3er rechazo |

## 🔐 Seguridad

### Headers Requeridos (del Gateway)

- `X-User-Id`: ID del usuario autenticado
- `X-User-Role`: Rol (DOCENTE, COORDINADOR, etc.)
- `X-User-Email`: Email del usuario

### Validaciones de Autorización

- **DOCENTE**: Puede crear y reenviar Formato A, subir Anteproyecto
- **Review Service**: Puede cambiar estados (identificado por `X-Service: review`)

## 💾 Almacenamiento

### Estructura de Archivos

```
/app/uploads/
├── formato-a/
│   └── {proyectoId}/
│       ├── v1/
│       │   ├── documento.pdf
│       │   └── carta.pdf (si PRACTICA_PROFESIONAL)
│       ├── v2/
│       └── v3/
└── anteproyectos/
    └── {proyectoId}/
        └── documento.pdf
```

### Límites de Archivos

- Formato A PDF: **10MB máximo**
- Carta de aceptación: **5MB máximo**
- Anteproyecto PDF: **15MB máximo**

## 🚀 Despliegue Local

### Prerequisitos

- Docker Desktop
- Java 21 (para desarrollo sin Docker)
- Maven 3.9+

### Con Docker Compose

```bash
# Iniciar servicios (PostgreSQL + RabbitMQ + Submission)
docker-compose up -d

# Ver logs
docker-compose logs -f submission-service

# Detener
docker-compose down
```

### Sin Docker (desarrollo)

```bash
# 1. Iniciar PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=submissiondb \
  -e POSTGRES_USER=submission_user \
  -e POSTGRES_PASSWORD=submission_pass \
  postgres:15-alpine

# 2. Iniciar RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 \
  rabbitmq:3.12-management-alpine

# 3. Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

## 🧪 Testing

```bash
# Tests unitarios
mvn test

# Tests de integración (usa Testcontainers)
mvn verify

# Solo compilar sin tests
mvn clean package -DskipTests
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

### Metrics
```bash
curl http://localhost:8082/actuator/metrics
```

### RabbitMQ Management UI
```
http://localhost:15672
Usuario: guest
Contraseña: guest
```

## 🔧 Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| DATABASE_HOST | Host de PostgreSQL | submission-postgres |
| DATABASE_PORT | Puerto de PostgreSQL | 5432 |
| DATABASE_NAME | Nombre de la BD | submissiondb |
| DATABASE_USERNAME | Usuario de BD | submission_user |
| DATABASE_PASSWORD | Contraseña de BD | submission_pass |
| RABBITMQ_HOST | Host de RabbitMQ | rabbitmq |
| RABBITMQ_PORT | Puerto de RabbitMQ | 5672 |
| RABBITMQ_USERNAME | Usuario RabbitMQ | admin |
| RABBITMQ_PASSWORD | Contraseña RabbitMQ | admin_password |
| FILE_STORAGE_PATH | Ruta de almacenamiento | /app/uploads |
| SPRING_PROFILES_ACTIVE | Perfil activo | dev |

## 🐛 Troubleshooting

### Error: "No se pudo guardar el archivo"
- Verificar permisos del directorio `/app/uploads`
- Verificar espacio en disco disponible

### Error: "Proyecto no existe"
- Verificar que el proyectoId sea correcto
- Revisar logs de base de datos

### Error: "Carta de aceptación obligatoria"
- Modalidad PRACTICA_PROFESIONAL requiere carta
- Verificar que el archivo se está enviando en el multipart

### RabbitMQ connection refused
- Verificar que RabbitMQ esté corriendo
- Verificar credenciales en variables de entorno
- Revisar logs: `docker-compose logs rabbitmq-local`

## 📚 Reglas de Negocio

1. **Máximo 3 intentos** para Formato A
2. **PRACTICA_PROFESIONAL**: 1 estudiante + carta obligatoria
3. **INVESTIGACION**: hasta 2 estudiantes
4. **Anteproyecto**: solo si Formato A está APROBADO
5. **Director**: solo el director puede subir anteproyecto

## 🔄 Flujo de Estados

```
[Crear Formato A v1] → EN_PROCESO
         ↓
    [Evaluación]
    ↙        ↘
APROBADO   RECHAZADO
   ↓           ↓
[Puede subir] [Reenviar v2] → EN_PROCESO
Anteproyecto    ↓
            [Evaluación]
            ↙        ↘
        APROBADO   RECHAZADO
           ↓           ↓
        [Fin]    [Reenviar v3] → EN_PROCESO
                    ↓
                [Evaluación]
                ↙        ↘
            APROBADO   RECHAZADO
               ↓           ↓
            [Fin]  RECHAZADO_DEFINITIVO
```

## 📞 Contacto

- **Equipo**: Desarrollo Unicauca
- **Fecha**: Octubre 2025
- **Versión**: 1.0.0

