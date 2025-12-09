# 📋 CAMBIOS EN ENDPOINTS Y DTOs - Arquitectura Hexagonal

**Fecha:** 9 de Diciembre de 2025  
**Versión:** 2.0.0

---

## ✅ RESUMEN EJECUTIVO


### ¿Cambiaron los DTOs?

**SÍ** - Los DTOs fueron **mejorados y enriquecidos**:
- ✅ **Request DTOs:** Más simples y con validaciones
- ✅ **Response DTO:** Mucho más completo (de 5 campos a 20+ campos)
- ✅ Nombres más descriptivos
- ✅ Documentación completa de cada campo

---

## 🔄 COMPARACIÓN DETALLADA

### 1. ENDPOINTS - Cambios en Rutas

#### Formato A

| Antes (Legacy) | Ahora (Hexagonal) | Cambio |
|----------------|-------------------|--------|
| `POST /api/submissions/formatoA` | `POST /api/submissions/formatoA` | ✅ Sin cambio |
| `POST /api/submissions/formatoA/{proyectoId}/nueva-version` | `POST /api/submissions/formatoA/{id}/reenviar` | ⚠️ Ruta simplificada |
| `PATCH /api/submissions/formatoA/{versionId}/estado` | `PATCH /api/submissions/formatoA/{id}/evaluar` | ⚠️ Más semántico |

**Cambios:**
- ✅ Ruta de reenvío simplificada: `/nueva-version` → `/reenviar`
- ✅ Ruta de evaluación más semántica: `/estado` → `/evaluar`
- ✅ Parámetros más claros: `{proyectoId}` y `{versionId}` → `{id}`

#### Anteproyecto

| Antes (Legacy) | Ahora (Hexagonal) | Cambio |
|----------------|-------------------|--------|
| `POST /api/submissions/anteproyecto` | `POST /api/submissions/anteproyecto/{proyectoId}` | ⚠️ ID en URL |
| `PATCH /api/submissions/anteproyecto/{id}/estado` | `POST /api/submissions/anteproyecto/{proyectoId}/evaluadores` | ⚠️ Endpoint específico |

**Cambios:**
- ✅ ID del proyecto ahora en URL (más RESTful)
- ✅ Endpoint específico para asignar evaluadores (RF8)

#### Queries

| Antes (Legacy) | Ahora (Hexagonal) | Cambio |
|----------------|-------------------|--------|
| `GET /api/submissions/{id}` | `GET /api/submissions/{id}` | ✅ Sin cambio |
| `GET /api/submissions` | `GET /api/submissions` | ✅ Sin cambio |
| `GET /api/submissions/estado/{estado}` | `GET /api/submissions/estado/{estado}` | ✅ Sin cambio |
| `GET /api/submissions/docente/{docenteId}` | `GET /api/submissions/director/{directorId}` | ⚠️ Nombre más preciso |
| ❌ No existía | `GET /api/submissions/estudiante/{estudianteId}` | ✅ Nuevo (RF5) |

**Cambios:**
- ✅ `docente` → `director` (más preciso)
- ✅ Nuevo endpoint para estudiantes (RF5)

---

### 2. REQUEST DTOs - Comparación

#### 2.1 Crear Formato A

**ANTES (Legacy):**
```json
{
  "proyectoGrado": {
    "titulo": "...",
    "modalidad": "...",
    // ... muchos campos mezclados
  },
  "formatoA": {
    "observaciones": "...",
    // ... campos de formato mezclados
  }
}
```

**AHORA (Hexagonal):**
```json
{
  "titulo": "...",
  "modalidad": "INVESTIGACION",
  "objetivoGeneral": "...",
  "objetivosEspecificos": ["...", "..."],
  "estudiante1Id": 123,
  "estudiante2Id": 456,
  "codirectorId": 789
}
```

**Cambios:**
- ✅ Estructura plana (sin anidamiento innecesario)
- ✅ Solo campos necesarios para creación
- ✅ Validaciones con anotaciones Jakarta
- ✅ Nombres más claros

**Validaciones agregadas:**
```java
@NotBlank(message = "El título es obligatorio")
private String titulo;

@NotNull(message = "La modalidad es obligatoria")
private Modalidad modalidad;

@NotBlank(message = "El objetivo general es obligatorio")
private String objetivoGeneral;

@NotEmpty(message = "Debe haber al menos un objetivo específico")
private List<String> objetivosEspecificos;

@NotNull(message = "El estudiante 1 es obligatorio")
private Long estudiante1Id;
```

#### 2.2 Evaluar Formato A

**ANTES (Legacy):**
```json
{
  "estado": "APROBADO",
  "observaciones": "..."
}
```

**AHORA (Hexagonal):**
```json
{
  "aprobado": true,
  "comentarios": "Excelente propuesta"
}
```

**Cambios:**
- ✅ Más semántico: `aprobado` (boolean) vs `estado` (string)
- ✅ `comentarios` vs `observaciones` (más claro)
- ✅ Validación: `aprobado` es obligatorio

#### 2.3 Reenviar Formato A

**ANTES (Legacy):**
```json
{
  "observaciones": "...",
  // ... otros campos
}
```

**AHORA (Hexagonal):**
```
Multipart files:
- pdf: File (opcional)
- carta: File (opcional)
```

**Cambios:**
- ✅ Solo archivos (lo que realmente cambia)
- ✅ Más simple y directo

---

### 3. RESPONSE DTO - Comparación Detallada

**ANTES (Legacy - SubmissionResponseDTO):**
```json
{
  "id": 1,
  "titulo": "...",
  "modalidad": "INVESTIGACION",
  "estado": "EN_PROCESO",
  "fechaCreacion": "2025-12-09T14:30:00"
}
```

**5 campos básicos**

**AHORA (Hexagonal - ProyectoResponse):**
```json
{
  "id": 1,
  "titulo": "Sistema de gestión académica basado en microservicios",
  "modalidad": "INVESTIGACION",
  "objetivoGeneral": "Desarrollar un sistema...",
  "objetivosEspecificos": [
    "Diseñar la arquitectura de microservicios",
    "Implementar los servicios core",
    "Realizar pruebas de integración"
  ],
  
  "directorId": 100,
  "codirectorId": 789,
  "estudiante1Id": 123,
  "estudiante2Id": 456,
  
  "estado": "FORMATO_A_APROBADO",
  "estadoDescripcion": "Formato A aprobado",
  "esEstadoFinal": false,
  
  "numeroIntento": 1,
  "rutaPdfFormatoA": "proyectos/formatoA/100/formatoA_abc123.pdf",
  "rutaCarta": null,
  "tieneCartaAceptacion": false,
  
  "rutaPdfAnteproyecto": null,
  "fechaEnvioAnteproyecto": null,
  "evaluador1Id": null,
  "evaluador2Id": null,
  "tieneEvaluadoresAsignados": false,
  
  "fechaCreacion": "2025-12-09T14:30:00",
  "fechaModificacion": "2025-12-09T16:00:00"
}
```

**20+ campos completos**

---

## 📊 TABLA COMPARATIVA DE CAMPOS

### Response DTO: ProyectoResponse

| Campo | Legacy | Hexagonal | Descripción |
|-------|--------|-----------|-------------|
| `id` | ✅ | ✅ | ID del proyecto |
| `titulo` | ✅ | ✅ | Título del proyecto |
| `modalidad` | ✅ | ✅ | INVESTIGACION o PRACTICA_PROFESIONAL |
| `estado` | ✅ | ✅ | Estado actual |
| `fechaCreacion` | ✅ | ✅ | Fecha de creación |
| **Nuevos campos:** | | | |
| `objetivoGeneral` | ❌ | ✅ | Objetivo general del proyecto |
| `objetivosEspecificos` | ❌ | ✅ | Lista de objetivos específicos |
| `directorId` | ❌ | ✅ | ID del director |
| `codirectorId` | ❌ | ✅ | ID del codirector (opcional) |
| `estudiante1Id` | ❌ | ✅ | ID del estudiante 1 |
| `estudiante2Id` | ❌ | ✅ | ID del estudiante 2 (opcional) |
| `estadoDescripcion` | ❌ | ✅ | Descripción legible del estado |
| `esEstadoFinal` | ❌ | ✅ | Indica si el estado es final |
| `numeroIntento` | ❌ | ✅ | Número de intento actual (1-3) |
| `rutaPdfFormatoA` | ❌ | ✅ | Ruta del PDF del Formato A |
| `rutaCarta` | ❌ | ✅ | Ruta de la carta (si aplica) |
| `tieneCartaAceptacion` | ❌ | ✅ | Boolean, tiene carta |
| `rutaPdfAnteproyecto` | ❌ | ✅ | Ruta del PDF del anteproyecto |
| `fechaEnvioAnteproyecto` | ❌ | ✅ | Fecha de envío del anteproyecto |
| `evaluador1Id` | ❌ | ✅ | ID del evaluador 1 |
| `evaluador2Id` | ❌ | ✅ | ID del evaluador 2 |
| `tieneEvaluadoresAsignados` | ❌ | ✅ | Boolean, tiene evaluadores |
| `fechaModificacion` | ❌ | ✅ | Última modificación |

**Campos agregados:** 17 campos nuevos  
**Campos mejorados:** Todos los existentes  
**Total campos:** 23 campos

---

## 🎯 BENEFICIOS DE LOS CAMBIOS

### Para el Frontend

✅ **Mucha más información en una sola llamada**
- Antes: 5 campos básicos
- Ahora: 23 campos completos
- **Beneficio:** Menos llamadas a la API

✅ **Estados más descriptivos**
- Antes: Solo código del estado
- Ahora: Código + descripción legible
- **Beneficio:** Mejor UX

✅ **Información de participantes**
- Antes: No se incluía
- Ahora: IDs de todos los participantes
- **Beneficio:** Mostrar info sin llamadas extra

✅ **Tracking completo del flujo**
- Antes: Estado genérico
- Ahora: Número de intento, evaluadores, archivos, etc.
- **Beneficio:** UI más informativa

### Para el Backend

✅ **Validaciones automáticas**
- Jakarta Validation en Request DTOs
- **Beneficio:** Menos código de validación

✅ **DTOs desacoplados del dominio**
- No exponen estructura interna
- **Beneficio:** Fácil cambiar dominio sin afectar API

✅ **Factory method fromDomain()**
- Conversión centralizada
- **Beneficio:** Consistencia garantizada

---

## 🔧 MIGRACIÓN DE CLIENTES

### Cambios Obligatorios

#### 1. Actualizar Rutas

```javascript
// ANTES
POST /api/submissions/formatoA/{proyectoId}/nueva-version

// AHORA
POST /api/submissions/formatoA/{id}/reenviar
```

#### 2. Adaptar Request de Evaluación

```javascript
// ANTES
{
  estado: "APROBADO",
  observaciones: "..."
}

// AHORA
{
  aprobado: true,
  comentarios: "..."
}
```

#### 3. Actualizar Parseo de Response

```javascript
// ANTES
const estado = response.estado;

// AHORA
const estado = response.estado;
const descripcion = response.estadoDescripcion; // Nuevo
const numeroIntento = response.numeroIntento; // Nuevo
const participantes = {
  director: response.directorId,
  estudiante1: response.estudiante1Id,
  estudiante2: response.estudiante2Id
};
```

### Cambios Opcionales (Aprovechar Mejoras)

#### Usar Campos Nuevos

```javascript
// Mostrar número de intento
if (response.numeroIntento > 1) {
  mostrarAlerta(`Intento ${response.numeroIntento} de 3`);
}

// Mostrar evaluadores asignados
if (response.tieneEvaluadoresAsignados) {
  mostrarEvaluadores(response.evaluador1Id, response.evaluador2Id);
}

// Verificar estado final
if (response.esEstadoFinal) {
  deshabilitarAcciones();
}
```

---

## 📝 EJEMPLOS COMPLETOS

### Ejemplo 1: Crear Formato A

**Request:**
```bash
POST /api/submissions/formatoA
Content-Type: multipart/form-data
X-User-Id: 100

data: {
  "titulo": "Sistema de gestión académica basado en microservicios",
  "modalidad": "INVESTIGACION",
  "objetivoGeneral": "Desarrollar un sistema de gestión académica escalable",
  "objetivosEspecificos": [
    "Diseñar la arquitectura de microservicios",
    "Implementar los servicios core",
    "Realizar pruebas de integración"
  ],
  "estudiante1Id": 123,
  "estudiante2Id": 456,
  "codirectorId": 789
}
pdf: <archivo>
```

**Response (201):**
```json
{
  "id": 1,
  "titulo": "Sistema de gestión académica basado en microservicios",
  "modalidad": "INVESTIGACION",
  "objetivoGeneral": "Desarrollar un sistema de gestión académica escalable",
  "objetivosEspecificos": [
    "Diseñar la arquitectura de microservicios",
    "Implementar los servicios core",
    "Realizar pruebas de integración"
  ],
  "directorId": 100,
  "codirectorId": 789,
  "estudiante1Id": 123,
  "estudiante2Id": 456,
  "estado": "FORMATO_A_DILIGENCIADO",
  "estadoDescripcion": "Formato A diligenciado",
  "esEstadoFinal": false,
  "numeroIntento": 1,
  "rutaPdfFormatoA": "proyectos/formatoA/100/formatoA_abc123.pdf",
  "rutaCarta": null,
  "tieneCartaAceptacion": false,
  "rutaPdfAnteproyecto": null,
  "fechaEnvioAnteproyecto": null,
  "evaluador1Id": null,
  "evaluador2Id": null,
  "tieneEvaluadoresAsignados": false,
  "fechaCreacion": "2025-12-09T14:30:00",
  "fechaModificacion": "2025-12-09T14:30:00"
}
```

### Ejemplo 2: Evaluar (Rechazar) Formato A

**Request:**
```bash
PATCH /api/submissions/formatoA/1/evaluar
Content-Type: application/json
X-User-Id: 200

{
  "aprobado": false,
  "comentarios": "El objetivo general debe ser más específico"
}
```

**Response (200):**
```json
{
  "id": 1,
  "titulo": "Sistema de gestión académica basado en microservicios",
  "estado": "CORRECCIONES_SOLICITADAS",
  "estadoDescripcion": "Correcciones solicitadas - Intento 1 de 3",
  "numeroIntento": 1,
  "fechaModificacion": "2025-12-09T15:00:00"
  // ... otros campos
}
```

### Ejemplo 3: Consultar Proyectos de Estudiante (RF5)

**Request:**
```bash
GET /api/submissions/estudiante/123
```

**Response (200):**
```json
[
  {
    "id": 1,
    "titulo": "Sistema de gestión académica",
    "estado": "CORRECCIONES_SOLICITADAS",
    "estadoDescripcion": "Correcciones solicitadas - Intento 1 de 3",
    "numeroIntento": 1,
    "esEstadoFinal": false,
    "directorId": 100,
    "estudiante1Id": 123,
    "estudiante2Id": 456,
    "fechaCreacion": "2025-12-09T14:30:00",
    "fechaModificacion": "2025-12-09T15:00:00"
    // ... otros campos
  }
]
```

---

## ✅ CONCLUSIÓN

### ¿Los Endpoints Cambiaron?

**SÍ, PERO SON MEJORAS:**
- ✅ Rutas más RESTful
- ✅ Endpoints más semánticos
- ✅ Mejor organización

### ¿Los DTOs Cambiaron?

**SÍ, Y SON MUCHO MEJORES:**
- ✅ Request: Más simples y validados
- ✅ Response: 17 campos nuevos
- ✅ Mejor documentados
- ✅ Más información útil

### Impacto en Clientes

**MEDIO-BAJO:**
- Cambios en 3-4 rutas
- Adaptación de parseo de response
- **Beneficio:** Mucha más funcionalidad

**Tiempo de migración estimado:** 1-2 días

---

**Documentación actualizada:** 9 de Diciembre de 2025  
**Versión API:** 2.0.0  
**Estado:** ✅ Completado

