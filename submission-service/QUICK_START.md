# 📋 Guía Rápida - Submission Service

## 🎯 ¿Qué hace este microservicio?

Gestiona la **creación y envío de documentos** (Formato A y Anteproyectos) para trabajos de grado.

---

## 🔑 Autenticación (IMPORTANTE)

### ⚠️ Este servicio NO maneja login directamente

**Flujo correcto:**
1. Usuario se autentica en el **API Gateway** → recibe JWT
2. Cliente envía peticiones al Gateway con: `Authorization: Bearer {jwt}`
3. Gateway valida JWT y propaga headers al Submission Service:
   - `X-User-Id`
   - `X-User-Role` 
   - `X-User-Email`

**NO necesitas** enviar el JWT directamente al Submission Service si pasas por el Gateway.

---

## 📡 Endpoints por Requisito Funcional

### RF2: Crear Formato A Inicial

```http
POST /api/submissions/formatoA
Headers:
  X-User-Role: DOCENTE
  X-User-Id: 101
Content-Type: multipart/form-data

Form Data:
  data: {
    "titulo": "Título del proyecto",
    "modalidad": "INVESTIGACION",  // o "PRACTICA_PROFESIONAL"
    "objetivoGeneral": "...",
    "objetivosEspecificos": ["obj1", "obj2"],
    "directorId": 101,
    "codirectorId": 205,  // opcional
    "estudiante1Id": 1001,
    "estudiante2Id": 1002  // opcional, solo para INVESTIGACION
  }
  pdf: <archivo.pdf>
  carta: <carta.pdf>  // OBLIGATORIO si modalidad es PRACTICA_PROFESIONAL
```

**Respuesta:** `201 Created`
```json
{ "id": 1 }
```

---

### RF4: Reenviar Formato A (tras rechazo)

```http
POST /api/submissions/formatoA/{proyectoId}/nueva-version
Headers:
  X-User-Role: DOCENTE
  X-User-Id: 101
Content-Type: multipart/form-data

Form Data:
  pdf: <archivo_v2.pdf>
  carta: <carta.pdf>  // si aplica
```

**Condiciones:**
- ✅ Proyecto debe estar RECHAZADO
- ✅ Intentos < 3
- ✅ Usuario debe ser el director

**Respuesta:** `201 Created`
```json
{ "id": 2 }
```

---

### RF3: Evaluar Formato A (Coordinador vía Review Service)

```http
PATCH /api/submissions/formatoA/{versionId}/estado
Headers:
  X-Service: review
Content-Type: application/json

Body:
{
  "estado": "APROBADO",  // o "RECHAZADO"
  "observaciones": "Comentarios del coordinador",
  "evaluadoPor": 50
}
```

**Respuesta:** `200 OK`

---

### RF6: Subir Anteproyecto

```http
POST /api/submissions/anteproyecto
Headers:
  X-User-Role: DOCENTE
  X-User-Id: 101
Content-Type: multipart/form-data

Form Data:
  data: { "proyectoId": 1 }
  pdf: <anteproyecto.pdf>
```

**Condiciones:**
- ✅ Formato A debe estar APROBADO
- ✅ Usuario debe ser el director del proyecto
- ✅ No debe existir anteproyecto previo

**Respuesta:** `201 Created`
```json
{ "id": 1 }
```

---

### RF5: Ver Estado de Proyecto (Estudiante)

```http
GET /api/submissions/formatoA/{id}
```

**Respuesta:** `200 OK`
```json
{
  "id": 1,
  "proyectoId": 1,
  "version": 1,
  "estado": "PENDIENTE",
  "observaciones": null,
  "fechaEnvio": "2025-11-03T10:30:00"
}
```

**Estados posibles:**
- `PENDIENTE` → En primera/segunda/tercera evaluación
- `APROBADO` → Aceptado formato A
- `RECHAZADO` → Rechazado (puede reenviar)
- Proyecto con 3 rechazos → Estado `RECHAZADO_DEFINITIVO`

---

### RF7: Listar Anteproyectos (Jefe Departamento)

```http
GET /api/submissions/anteproyecto?page=0&size=20
```

**Respuesta:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "proyectoId": 1,
      "pdfUrl": "/app/uploads/anteproyectos/1/documento.pdf",
      "fechaEnvio": "2025-11-03T15:45:00",
      "estado": "PENDIENTE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5
}
```

---

## 🔄 Flujo Completo de un Proyecto

```
1. DOCENTE crea Formato A v1 (RF2)
   └─> Estado: EN_PROCESO
   └─> Evento: formato-a.enviado → Notifica COORDINADOR

2. COORDINADOR evalúa (RF3, vía Review Service)
   
   Si APRUEBA:
   └─> Estado: APROBADO
   └─> Evento: Notifica DOCENTE y ESTUDIANTES
   └─> DOCENTE puede subir Anteproyecto (RF6)
   
   Si RECHAZA (intento 1 o 2):
   └─> Estado: RECHAZADO
   └─> Evento: Notifica DOCENTE con observaciones
   └─> DOCENTE puede reenviar (RF4)
   
   Si RECHAZA (intento 3):
   └─> Estado: RECHAZADO_DEFINITIVO
   └─> Evento: proyecto.rechazado-definitivamente
   └─> FIN - No puede continuar

3. Si APROBADO → DOCENTE sube Anteproyecto (RF6)
   └─> Evento: anteproyecto.enviado → Notifica JEFE_DEPARTAMENTO

4. JEFE_DEPARTAMENTO ve listado (RF7)
   └─> Asigna evaluadores (en Review Service)
```

---

## 🚨 Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| `403 Forbidden: Solo DOCENTE puede...` | Header `X-User-Role` incorrecto | Verificar que el header sea `X-User-Role: DOCENTE` |
| `400 Bad Request: Carta obligatoria` | Falta carta para PRACTICA_PROFESIONAL | Incluir archivo `carta` en el multipart |
| `400 Bad Request: Ya alcanzó límite` | 3 intentos agotados | Proyecto está RECHAZADO_DEFINITIVO, crear nuevo proyecto |
| `400 Bad Request: Formato A no aprobado` | Intentando subir anteproyecto sin aprobación | Esperar aprobación del Formato A |
| `403 Forbidden: No es el director` | Usuario no es el director del proyecto | Solo el director puede subir documentos |

---

## 📊 Estados del Proyecto (RF5)

Para que el **estudiante vea su estado**:

| Estado del Proyecto | Significado para el Estudiante |
|---------------------|--------------------------------|
| `EN_PROCESO` | Formato A está en evaluación (1ra, 2da o 3ra evaluación) |
| `APROBADO` | ✅ Formato A aprobado - Director puede subir anteproyecto |
| `RECHAZADO` | ❌ Formato A rechazado - Director puede reenviar |
| `RECHAZADO_DEFINITIVO` | ❌❌❌ 3 rechazos - Debe iniciar nuevo proyecto |

**Versiones del Formato A:**
- `version: 1` → Primera evaluación
- `version: 2` → Segunda evaluación
- `version: 3` → Tercera evaluación (última oportunidad)

---

## 🐰 Eventos de Notificación

El servicio **publica** eventos a RabbitMQ para que **Notification Service** envíe correos:

| Evento | Cuándo | A quién notifica |
|--------|--------|------------------|
| `formato-a.enviado` | Se crea Formato A v1 | Coordinador |
| `formato-a.reenviado` | Se envía nueva versión | Coordinador |
| `anteproyecto.enviado` | Se sube anteproyecto | Jefe de Departamento |
| `proyecto.rechazado-definitivamente` | 3 rechazos | Director y Estudiantes |

---

## 🔧 Configuración Rápida (Docker)

```bash
# 1. Clonar y navegar
cd submission-service

# 2. Iniciar todo (PostgreSQL + RabbitMQ + App)
docker-compose up -d

# 3. Verificar
curl http://localhost:8082/actuator/health

# 4. Ver logs
docker-compose logs -f submission-service
```

**Puertos:**
- Submission Service: `8082`
- PostgreSQL: `5432`
- RabbitMQ: `5672` (AMQP)
- RabbitMQ Management: `15672` (http://localhost:15672)

---

## 📝 Ejemplo Completo con cURL

### 1. Crear Formato A

```bash
curl -X POST http://localhost:8082/api/submissions/formatoA \
  -H "X-User-Role: DOCENTE" \
  -H "X-User-Id: 101" \
  -F 'data={
    "titulo":"Sistema de Gestión IoT",
    "modalidad":"INVESTIGACION",
    "objetivoGeneral":"Desarrollar sistema IoT para inventarios",
    "objetivosEspecificos":["Diseñar arquitectura","Implementar sensores"],
    "directorId":101,
    "estudiante1Id":1001
  };type=application/json' \
  -F "pdf=@formato_a.pdf"
```

### 2. Listar Formatos A de un docente

```bash
curl "http://localhost:8082/api/submissions/formatoA?docenteId=101&page=0&size=10"
```

### 3. Aprobar Formato A (como Review Service)

```bash
curl -X PATCH http://localhost:8082/api/submissions/formatoA/1/estado \
  -H "Content-Type: application/json" \
  -H "X-Service: review" \
  -d '{
    "estado": "APROBADO",
    "observaciones": "Excelente propuesta, aprobado",
    "evaluadoPor": 50
  }'
```

### 4. Subir Anteproyecto

```bash
curl -X POST http://localhost:8082/api/submissions/anteproyecto \
  -H "X-User-Role: DOCENTE" \
  -H "X-User-Id: 101" \
  -F 'data={"proyectoId":1};type=application/json' \
  -F "pdf=@anteproyecto.pdf"
```

---

## 🎓 Reglas de Negocio Clave

1. **Modalidad INVESTIGACION**
   - ✅ Hasta 2 estudiantes
   - ✅ Carta opcional

2. **Modalidad PRACTICA_PROFESIONAL**
   - ✅ 1 estudiante
   - ✅ Carta OBLIGATORIA

3. **Máximo 3 intentos**
   - Intento 1, 2 → Puede reenviar si rechazado
   - Intento 3 → Última oportunidad
   - Si 3ro es rechazado → RECHAZADO_DEFINITIVO (fin)

4. **Anteproyecto**
   - Solo si Formato A está APROBADO
   - Solo el director puede subirlo
   - Solo 1 anteproyecto por proyecto

---

## 📞 Soporte

- **README completo**: Ver `README.md` para documentación detallada
- **Logs**: `docker-compose logs -f submission-service`
- **Health**: http://localhost:8082/actuator/health
- **RabbitMQ UI**: http://localhost:15672

---

**Versión:** 1.0.0 | **Fecha:** Noviembre 2025 | **Universidad del Cauca**

