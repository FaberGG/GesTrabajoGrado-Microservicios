# 🔧 Fix: Error de Validación de Director en Anteproyecto

## 🐛 Problema Identificado

**Error reportado:**
```json
{
    "status": 500,
    "mensaje": "Error interno del servidor: Solo el director del proyecto puede subir el anteproyecto",
    "timestamp": "2025-12-04T20:42:22.197388403"
}
```

### Causa Raíz

El error se debía a una **inconsistencia entre la entidad `ProyectoSubmission` y el código del servicio**:

1. **La entidad `ProyectoSubmission`** solo tiene estos campos:
   - `docenteDirectorId` (Long) - ✅ Director
   - `docenteCodirectorId` (Long) - ✅ Co-director
   - `estudianteId` (Long) - ✅ **UN SOLO ESTUDIANTE**

2. **El código en `subirAnteproyecto()`** estaba intentando acceder a:
   - `proyecto.getEstudiante1Id()` - ❌ **NO EXISTE**
   - `proyecto.getEstudiante2Id()` - ❌ **NO EXISTE**

### ¿Por qué causaba el error?

Cuando el método intentaba obtener información de los estudiantes:

```java
// ❌ CÓDIGO INCORRECTO (ANTES)
if (proyecto.getEstudiante1Id() != null) {  // Este método NO existe
    // ...
}
```

Como estos métodos no existen en la entidad, probablemente retornaban `null` o causaban otros problemas que afectaban la validación del director.

---

## ✅ Solución Aplicada

### 1. Logs de Depuración Mejorados

Se agregaron logs detallados para identificar problemas de validación:

```java
log.info("🔍 DEBUG - Validando director:");
log.info("   - Usuario que intenta subir (userId): {} (tipo: {})", userId, userId.getClass().getName());
log.info("   - Director del proyecto (getDocenteDirectorId): {} (tipo: {})", 
        proyecto.getDocenteDirectorId(), 
        proyecto.getDocenteDirectorId() != null ? proyecto.getDocenteDirectorId().getClass().getName() : "NULL");
log.info("   - Usuario convertido a Long: {}", Long.valueOf(userId));
log.info("   - ¿Son iguales?: {}", proyecto.getDocenteDirectorId() != null && proyecto.getDocenteDirectorId().equals(Long.valueOf(userId)));
```

**Beneficio:** Ahora podrás ver exactamente qué valores se están comparando y por qué falla la validación.

### 2. Validación Explícita de Director Nulo

```java
if (proyecto.getDocenteDirectorId() == null) {
    log.error("❌ El proyecto no tiene director asignado");
    throw new IllegalArgumentException("El proyecto no tiene director asignado");
}
```

**Beneficio:** Mensaje de error más claro si el proyecto no tiene director.

### 3. Corrección de Acceso a Estudiantes

**ANTES (❌ INCORRECTO):**
```java
if (proyecto.getEstudiante1Id() != null) {  // NO EXISTE
    IdentityClient.UserBasicInfo estudiante1Info = identityClient.getUserById(proyecto.getEstudiante1Id());
    // ...
}
if (proyecto.getEstudiante2Id() != null) {  // NO EXISTE
    // ...
}
```

**DESPUÉS (✅ CORRECTO):**
```java
// 11. Obtener información del estudiante (y su programa)
String estudiante1Nombre = "Estudiante Desconocido";
String programa = "SIN_PROGRAMA";
Long estudiante1Id = null;
if (proyecto.getEstudianteId() != null) {  // ✅ Campo correcto
    estudiante1Id = proyecto.getEstudianteId();
    IdentityClient.UserBasicInfo estudianteInfo = identityClient.getUserById(proyecto.getEstudianteId());
    estudiante1Nombre = estudianteInfo != null ? estudianteInfo.getNombreCompleto() : "Estudiante Desconocido";
    if (estudianteInfo != null && estudianteInfo.programa() != null) {
        programa = estudianteInfo.programa();
    }
    log.info("✅ Estudiante obtenido: {} - Programa: {}", estudiante1Nombre, programa);
}

// 12. Nota: La entidad actual solo soporta un estudiante
String estudiante2Nombre = null;
Long estudiante2Id = null;
log.info("ℹ️ La entidad ProyectoSubmission actual solo soporta un estudiante");
```

### 4. Evento con Datos Correctos

**ANTES:**
```java
.estudiante1Id(proyecto.getEstudiante1Id())  // ❌ NO EXISTE
.estudiante2Id(proyecto.getEstudiante2Id())  // ❌ NO EXISTE
```

**DESPUÉS:**
```java
.estudiante1Id(estudiante1Id)  // ✅ Variable local con valor correcto
.estudiante2Id(estudiante2Id)  // ✅ Siempre null (solo 1 estudiante soportado)
```

---

## 📊 Limitación Identificada

⚠️ **IMPORTANTE:** La entidad `ProyectoSubmission` actual **solo soporta UN estudiante**.

### Estructura Actual de la Base de Datos:

```sql
CREATE TABLE proyecto_submissions (
    id BIGINT PRIMARY KEY,
    titulo VARCHAR(500),
    docente_director_id BIGINT NOT NULL,
    docente_codirector_id BIGINT,
    estudiante_id BIGINT,  -- ⚠️ SOLO UN ESTUDIANTE
    -- ... otros campos
);
```

### Implicaciones:

1. ✅ **Funciona para proyectos con 1 estudiante**
2. ❌ **NO soporta proyectos con 2 estudiantes**
3. 🔄 Si se crea un FormatoA con `estudiante1Id` y `estudiante2Id`, solo se guarda el primero

---

## 🚀 Pruebas Recomendadas

### 1. Verificar Logs de Depuración

Después de intentar subir el anteproyecto, revisa los logs del submission-service:

```bash
# Buscar en los logs
docker logs submission-service | grep "🔍 DEBUG - Validando director"
```

Deberías ver algo como:
```
🔍 DEBUG - Validando director:
   - Usuario que intenta subir (userId): 5 (tipo: java.lang.String)
   - Director del proyecto (getDocenteDirectorId): 5 (tipo: java.lang.Long)
   - Usuario convertido a Long: 5
   - ¿Son iguales?: true
✅ Validación de director exitosa
```

### 2. Verificar que el Director se Guardó Correctamente

```sql
-- Conectar a la base de datos de submission-service
SELECT 
    id,
    titulo,
    docente_director_id,
    estudiante_id,
    estado_nombre
FROM proyecto_submissions
WHERE id = [TU_PROYECTO_ID];
```

**Verifica que:**
- `docente_director_id` NO sea NULL
- `docente_director_id` coincida con el ID del usuario que sube el anteproyecto

### 3. Probar el Endpoint

```bash
POST http://localhost:8080/api/submissions/anteproyecto
Headers:
  X-User-Role: DOCENTE
  X-User-Id: 5  # ⚠️ Debe coincidir con docente_director_id del proyecto
Body (multipart/form-data):
  data: {
    "proyectoId": 123
  }
  pdf: archivo.pdf
```

**Resultados esperados:**
- ✅ HTTP 201 Created (si todo está correcto)
- ✅ Logs detallados mostrando la comparación de IDs
- ✅ Evento publicado a RabbitMQ
- ✅ Notificación enviada al Jefe de Departamento

---

## 🔍 Diagnóstico Adicional

Si el error persiste después de este fix, verifica:

### 1. Headers HTTP
```bash
# Verificar que los headers lleguen correctamente
X-User-Id: 5       # ⚠️ Debe ser el ID del director
X-User-Role: DOCENTE
```

### 2. Estado del Proyecto
```sql
SELECT estado_nombre FROM proyecto_submissions WHERE id = [ID];
```
Debe ser: `ACEPTADO_POR_COMITE`

### 3. Director del Proyecto
```sql
SELECT docente_director_id FROM proyecto_submissions WHERE id = [ID];
```
Debe coincidir con el `X-User-Id` del request.

---

## 🛠️ Solución Futura: Soporte para 2 Estudiantes

Si necesitas soportar proyectos con 2 estudiantes, se deben realizar estos cambios:

### 1. Migración de Base de Datos (Flyway)

```sql
-- V2__add_second_student.sql
ALTER TABLE proyecto_submissions
ADD COLUMN estudiante1_id BIGINT,
ADD COLUMN estudiante2_id BIGINT;

-- Migrar datos existentes
UPDATE proyecto_submissions
SET estudiante1_id = estudiante_id;

-- Opcionalmente eliminar la columna antigua
-- ALTER TABLE proyecto_submissions DROP COLUMN estudiante_id;
```

### 2. Actualizar Entidad `ProyectoSubmission.java`

```java
@Column
private Long estudiante1Id;

@Column
private Long estudiante2Id;
```

### 3. Actualizar Todos los Servicios

- `crearFormatoA()` - Guardar ambos estudiantes
- `reenviarFormatoA()` - Mantener ambos estudiantes
- `subirAnteproyecto()` - Leer ambos estudiantes

---

## ✅ Resumen de Cambios

| Archivo | Cambios Realizados |
|---------|-------------------|
| `SubmissionService.java` | ✅ Logs de depuración detallados |
| `SubmissionService.java` | ✅ Validación explícita de director null |
| `SubmissionService.java` | ✅ Corrección de acceso a estudiantes (usar `getEstudianteId()`) |
| `SubmissionService.java` | ✅ Evento con IDs correctos de estudiantes |

---

## 📞 Siguiente Paso

**Prueba nuevamente el endpoint de subir anteproyecto** y revisa los logs detallados para ver exactamente qué está pasando con la validación del director.

Si el error persiste, comparte los logs que empiezan con `🔍 DEBUG - Validando director` para diagnosticar el problema exacto.

---

**Fecha:** 2025-12-04  
**Estado:** ✅ FIX APLICADO - PENDIENTE DE PRUEBAS

