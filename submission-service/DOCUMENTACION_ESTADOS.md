# Documentación: Estados del Proyecto y Transiciones

## Descripción General

El sistema de gestión de trabajos de grado implementa el **Patrón State** para manejar el ciclo de vida del **Formato A** (anteproyecto). Este patrón permite que el proyecto cambie su comportamiento según su estado actual, garantizando transiciones válidas y controladas.

## Arquitectura del Patrón State

### Componentes Principales

1. **IEstadoSubmission**: Interfaz que define las operaciones disponibles
2. **EstadoSubmissionBase**: Clase abstracta base que implementa comportamientos por defecto
3. **Estados Concretos**: Implementaciones específicas para cada estado del proyecto

## Estados del Sistema

El proyecto puede estar en uno de los siguientes **6 estados**:

### 1️⃣ FORMATO_A_DILIGENCIADO (Estado Inicial)

**Descripción**: El docente ha completado el llenado del Formato A y está listo para presentarlo.

**Características**:
- Estado inicial del proyecto
- No es estado final
- Requiere validación de datos básicos (título completo)

**Operaciones Permitidas**:
- ✅ `presentarAlCoordinador()`: Presenta el formato al coordinador

**Operaciones Bloqueadas**:
- ❌ `enviarAComite()`
- ❌ `evaluar()`
- ❌ `subirNuevaVersion()`

**Transiciones Válidas**:
```
FORMATO_A_DILIGENCIADO → PRESENTADO_AL_COORDINADOR
```

**Validaciones**:
- El título del proyecto debe estar completo
- Lanza `IllegalStateException` si el título está vacío

---

### 2️⃣ PRESENTADO_AL_COORDINADOR

**Descripción**: El Formato A ha sido presentado y está esperando que el coordinador lo revise y envíe al comité.

**Características**:
- Estado intermedio de revisión
- No es estado final
- El coordinador tiene la autoridad para enviar al comité

**Operaciones Permitidas**:
- ✅ `enviarAComite()`: El coordinador envía el formato al comité de evaluación

**Operaciones Bloqueadas**:
- ❌ `presentarAlCoordinador()`
- ❌ `evaluar()`
- ❌ `subirNuevaVersion()`

**Transiciones Válidas**:
```
PRESENTADO_AL_COORDINADOR → EN_EVALUACION_COMITE
```

---

### 3️⃣ EN_EVALUACION_COMITE

**Descripción**: El comité está evaluando el Formato A para decidir si lo aprueba o solicita correcciones.

**Características**:
- Estado de evaluación activa
- No es estado final
- Punto de decisión crítico del sistema
- Controla el número de intentos del proyecto

**Operaciones Permitidas**:
- ✅ `evaluar(aprobado, comentarios)`: El comité emite su decisión

**Operaciones Bloqueadas**:
- ❌ `presentarAlCoordinador()`
- ❌ `enviarAComite()`
- ❌ `subirNuevaVersion()`

**Transiciones Válidas**:
```
EN_EVALUACION_COMITE → ACEPTADO_POR_COMITE (si aprobado = true)
EN_EVALUACION_COMITE → CORRECCIONES_COMITE (si aprobado = false && intentos < 3)
EN_EVALUACION_COMITE → RECHAZADO_POR_COMITE (si aprobado = false && intentos >= 3)
```

**Lógica de Evaluación**:

```java
if (aprobado) {
    // Proyecto aprobado → Estado final exitoso
    cambiarEstado(ACEPTADO_POR_COMITE);
} else {
    numeroIntentos++;
    if (numeroIntentos >= 3) {
        // Se alcanzó el límite → Estado final de rechazo
        cambiarEstado(RECHAZADO_POR_COMITE);
    } else {
        // Se permiten más intentos → Solicitar correcciones
        cambiarEstado(CORRECCIONES_COMITE);
    }
}
```

**Registro de Información**:
- Guarda los comentarios del comité
- Incrementa el contador de intentos en caso de rechazo
- Máximo de intentos permitidos: **3**

---

### 4️⃣ CORRECCIONES_COMITE

**Descripción**: El comité ha solicitado correcciones al Formato A. El docente puede subir una nueva versión mejorada.

**Características**:
- Estado de retroalimentación
- No es estado final
- Permite al docente corregir y reintentar
- Número de intento actual visible en los logs

**Operaciones Permitidas**:
- ✅ `subirNuevaVersion()`: El docente sube la versión corregida del Formato A

**Operaciones Bloqueadas**:
- ❌ `presentarAlCoordinador()`
- ❌ `enviarAComite()`
- ❌ `evaluar()`

**Transiciones Válidas**:
```
CORRECCIONES_COMITE → EN_EVALUACION_COMITE (al subir nueva versión)
```

**Información en Logs**:
- Muestra el número de intento actual (X/3)
- Indica que se envía nuevamente al comité para evaluación

---

### 5️⃣ ACEPTADO_POR_COMITE (Estado Final Exitoso) ✅

**Descripción**: El Formato A ha sido aprobado por el comité. El proyecto puede continuar con las siguientes fases.

**Características**:
- **Estado final** (no permite más transiciones)
- Resultado exitoso del proceso
- El proyecto cumplió con todos los requisitos

**Operaciones Permitidas**:
- ❌ Ninguna (estado terminal)

**Operaciones Bloqueadas**:
- ❌ `presentarAlCoordinador()`
- ❌ `enviarAComite()`
- ❌ `evaluar()`
- ❌ `subirNuevaVersion()`

**Transiciones Válidas**:
```
Ninguna - Estado terminal
```

**Nota**: Cualquier intento de cambiar el estado lanzará `IllegalStateException`

---

### 6️⃣ RECHAZADO_POR_COMITE (Estado Final de Rechazo) ❌

**Descripción**: El Formato A ha sido rechazado definitivamente después de alcanzar el límite de 3 intentos.

**Características**:
- **Estado final** (no permite más transiciones)
- Se alcanza cuando `numeroIntentos >= 3`
- El proyecto no puede continuar
- Resultado final negativo

**Operaciones Permitidas**:
- ❌ Ninguna (estado terminal)

**Operaciones Bloqueadas**:
- ❌ `presentarAlCoordinador()`
- ❌ `enviarAComite()`
- ❌ `evaluar()`
- ❌ `subirNuevaVersion()`

**Transiciones Válidas**:
```
Ninguna - Estado terminal
```

**Nota**: Cualquier intento de cambiar el estado lanzará `IllegalStateException`

---

## Diagrama de Flujo de Estados

```
┌──────────────────────────┐
│  FORMATO_A_DILIGENCIADO  │ (Estado Inicial)
└────────────┬─────────────┘
             │ presentarAlCoordinador()
             ▼
┌──────────────────────────┐
│ PRESENTADO_AL_COORDINADOR│
└────────────┬─────────────┘
             │ enviarAComite()
             ▼
┌──────────────────────────┐
│   EN_EVALUACION_COMITE   │
└────────────┬─────────────┘
             │ evaluar()
             │
      ┌──────┴──────┬───────────────┐
      │             │               │
aprobado=true  rechazado      rechazado
              intentos<3    intentos>=3
      │             │               │
      ▼             ▼               ▼
┌─────────────┐ ┌──────────────┐ ┌──────────────────┐
│  ACEPTADO   │ │ CORRECCIONES │ │   RECHAZADO      │
│ POR_COMITE  │ │   _COMITE    │ │  POR_COMITE      │
│   (FINAL)   │ └──────┬───────┘ │     (FINAL)      │
└─────────────┘        │         └──────────────────┘
                       │ subirNuevaVersion()
                       │
                       └──────────────┐
                                      │
                                      ▼
                          ┌──────────────────────────┐
                          │   EN_EVALUACION_COMITE   │
                          └──────────────────────────┘
                                 (Ciclo de retroalimentación)
```

## Matriz de Transiciones

| Estado Actual | presentarAlCoordinador() | enviarAComite() | evaluar() | subirNuevaVersion() |
|--------------|-------------------------|----------------|-----------|-------------------|
| **FORMATO_A_DILIGENCIADO** | ✅ → PRESENTADO_AL_COORDINADOR | ❌ | ❌ | ❌ |
| **PRESENTADO_AL_COORDINADOR** | ❌ | ✅ → EN_EVALUACION_COMITE | ❌ | ❌ |
| **EN_EVALUACION_COMITE** | ❌ | ❌ | ✅ → Ver lógica* | ❌ |
| **CORRECCIONES_COMITE** | ❌ | ❌ | ❌ | ✅ → EN_EVALUACION_COMITE |
| **ACEPTADO_POR_COMITE** | ❌ | ❌ | ❌ | ❌ |
| **RECHAZADO_POR_COMITE** | ❌ | ❌ | ❌ | ❌ |

*Lógica de `evaluar()`:
- Si aprobado → ACEPTADO_POR_COMITE
- Si rechazado y intentos < 3 → CORRECCIONES_COMITE
- Si rechazado y intentos >= 3 → RECHAZADO_POR_COMITE

## Reglas de Negocio

### 1. Límite de Intentos
- El proyecto tiene un máximo de **3 intentos** de evaluación
- El contador se incrementa cada vez que el comité rechaza el formato
- Al alcanzar 3 intentos, el proyecto se rechaza definitivamente

### 2. Estados Finales
Hay dos estados finales posibles:
- ✅ **ACEPTADO_POR_COMITE**: El proyecto fue aprobado
- ❌ **RECHAZADO_POR_COMITE**: El proyecto fue rechazado definitivamente

Una vez en un estado final:
- No se permiten más transiciones
- Cualquier intento de cambiar el estado lanza `IllegalStateException`

### 3. Validaciones
- **FORMATO_A_DILIGENCIADO**: Requiere que el título esté completo antes de presentar
- **Estados intermedios**: Solo permiten las operaciones específicas de cada estado
- **Operaciones inválidas**: Lanzan `IllegalStateException` con mensaje descriptivo

### 4. Ciclo de Retroalimentación
El sistema permite un ciclo de mejora continua:
```
EN_EVALUACION_COMITE → CORRECCIONES_COMITE → (subir nueva versión) → EN_EVALUACION_COMITE
```
Este ciclo puede repetirse hasta 3 veces antes de un rechazo definitivo.

## Implementación Técnica

### Patrón Singleton
Todos los estados concretos implementan el **patrón Singleton**:
- Cada estado tiene una única instancia en memoria
- Reduce el consumo de recursos
- Facilita la comparación de estados

```java
public static AceptadoPorComiteState getInstance() {
    if (instance == null) {
        instance = new AceptadoPorComiteState();
    }
    return instance;
}
```

### Manejo de Excepciones
La clase base `EstadoSubmissionBase` proporciona implementaciones por defecto que lanzan excepciones:
- Cada estado concreto solo sobrescribe los métodos permitidos
- Los métodos no permitidos heredan el comportamiento de lanzar `IllegalStateException`
- Mensajes de error descriptivos que incluyen el estado actual

### Logs del Sistema
El método `cambiarEstado()` registra todas las transiciones:
```java
protected void cambiarEstado(ProyectoSubmission proyecto, IEstadoSubmission nuevoEstado) {
    System.out.println("Transición de estado: " + getNombreEstado() + " -> " + nuevoEstado.getNombreEstado());
    proyecto.setEstadoActual(nuevoEstado);
}
```

Esto permite:
- Trazabilidad completa del flujo del proyecto
- Debugging y auditoría
- Monitoreo del ciclo de vida

## Ejemplos de Uso

### Ejemplo 1: Flujo Exitoso (Aprobado en primer intento)

```java
// 1. Crear proyecto con estado inicial
ProyectoSubmission proyecto = new ProyectoSubmission();
proyecto.setTitulo("Sistema de Gestión Académica");
proyecto.setEstadoActual(FormatoADiligenciadoState.getInstance());

// 2. Presentar al coordinador
proyecto.getEstadoActual().presentarAlCoordinador(proyecto);
// Estado: PRESENTADO_AL_COORDINADOR

// 3. Coordinador envía al comité
proyecto.getEstadoActual().enviarAComite(proyecto);
// Estado: EN_EVALUACION_COMITE

// 4. Comité aprueba
proyecto.getEstadoActual().evaluar(proyecto, true, "Excelente propuesta");
// Estado: ACEPTADO_POR_COMITE (FINAL)
```

### Ejemplo 2: Flujo con Correcciones (Aprobado en segundo intento)

```java
// ... Estados 1-3 igual que Ejemplo 1 ...

// 4. Comité solicita correcciones
proyecto.getEstadoActual().evaluar(proyecto, false, "Mejorar metodología");
// Estado: CORRECCIONES_COMITE (Intento 1/3)

// 5. Docente sube nueva versión
proyecto.getEstadoActual().subirNuevaVersion(proyecto);
// Estado: EN_EVALUACION_COMITE

// 6. Comité aprueba la nueva versión
proyecto.getEstadoActual().evaluar(proyecto, true, "Correcciones satisfactorias");
// Estado: ACEPTADO_POR_COMITE (FINAL)
```

### Ejemplo 3: Flujo con Rechazo Definitivo (3 intentos fallidos)

```java
// ... Estados 1-3 igual que Ejemplo 1 ...

// Intento 1: Rechazado
proyecto.getEstadoActual().evaluar(proyecto, false, "Mejorar objetivos");
// Estado: CORRECCIONES_COMITE (Intento 1/3)
proyecto.getEstadoActual().subirNuevaVersion(proyecto);
// Estado: EN_EVALUACION_COMITE

// Intento 2: Rechazado
proyecto.getEstadoActual().evaluar(proyecto, false, "Mejorar justificación");
// Estado: CORRECCIONES_COMITE (Intento 2/3)
proyecto.getEstadoActual().subirNuevaVersion(proyecto);
// Estado: EN_EVALUACION_COMITE

// Intento 3: Rechazado definitivamente
proyecto.getEstadoActual().evaluar(proyecto, false, "No cumple requisitos mínimos");
// Estado: RECHAZADO_POR_COMITE (FINAL)
// numeroIntentos = 3
```

### Ejemplo 4: Operación Inválida (Lanza Excepción)

```java
ProyectoSubmission proyecto = new ProyectoSubmission();
proyecto.setEstadoActual(FormatoADiligenciadoState.getInstance());

// Intento de evaluar en estado inicial
try {
    proyecto.getEstadoActual().evaluar(proyecto, true, "...");
} catch (IllegalStateException e) {
    // Mensaje: "No se puede evaluar desde el estado: FORMATO_A_DILIGENCIADO"
}
```

## Ventajas del Patrón State

1. **Encapsulación**: Cada estado encapsula su propio comportamiento
2. **Mantenibilidad**: Fácil agregar nuevos estados sin modificar los existentes
3. **Claridad**: El código es más legible y expresa claramente las transiciones válidas
4. **Seguridad**: Previene transiciones inválidas mediante excepciones
5. **Testabilidad**: Cada estado puede ser testeado independientemente

## Consideraciones de Diseño

### ¿Por qué usar el Patrón State?

Este patrón es ideal cuando:
- Un objeto tiene múltiples estados con comportamientos diferentes
- Las transiciones entre estados siguen reglas específicas
- Se quiere evitar código con múltiples `if-else` o `switch`

### Alternativas Consideradas

1. **Enum con switch**: Menos mantenible y escalable
2. **Flags booleanos**: Dificulta el manejo de estados complejos
3. **Patrón State**: ✅ Elegido por flexibilidad y claridad

## Integración con el Sistema

### Persistencia
- El estado actual se almacena en la base de datos como String
- Al recuperar un proyecto, se reconstruye el objeto de estado correspondiente
- El contador `numeroIntentos` también se persiste

### API REST
Los endpoints deben validar el estado antes de realizar operaciones:
```java
@PostMapping("/{id}/presentar-coordinador")
public ResponseEntity<?> presentarAlCoordinador(@PathVariable Long id) {
    ProyectoSubmission proyecto = proyectoService.findById(id);
    try {
        proyecto.getEstadoActual().presentarAlCoordinador(proyecto);
        proyectoService.save(proyecto);
        return ResponseEntity.ok(proyecto);
    } catch (IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

### Eventos del Sistema
Cada transición de estado puede publicar eventos para notificar a otros microservicios:
- `ProyectoPresentadoEvent`
- `ProyectoEnviadoAComiteEvent`
- `ProyectoEvaluadoEvent`
- `ProyectoAprobadoEvent`
- `ProyectoRechazadoEvent`

## Testing

### Casos de Prueba Recomendados

1. **Transiciones Válidas**: Verificar que todas las transiciones permitidas funcionen
2. **Transiciones Inválidas**: Verificar que lance `IllegalStateException`
3. **Límite de Intentos**: Verificar que rechace después de 3 intentos
4. **Estados Finales**: Verificar que no permitan cambios
5. **Validaciones**: Verificar que las reglas de negocio se cumplan

### Ejemplo de Test

```java
@Test
public void testCicloCompletoAprobacion() {
    ProyectoSubmission proyecto = new ProyectoSubmission();
    proyecto.setTitulo("Test");
    proyecto.setEstadoActual(FormatoADiligenciadoState.getInstance());
    
    proyecto.getEstadoActual().presentarAlCoordinador(proyecto);
    assertEquals("PRESENTADO_AL_COORDINADOR", proyecto.getEstadoActual().getNombreEstado());
    
    proyecto.getEstadoActual().enviarAComite(proyecto);
    assertEquals("EN_EVALUACION_COMITE", proyecto.getEstadoActual().getNombreEstado());
    
    proyecto.getEstadoActual().evaluar(proyecto, true, "Aprobado");
    assertEquals("ACEPTADO_POR_COMITE", proyecto.getEstadoActual().getNombreEstado());
    assertTrue(proyecto.getEstadoActual().esEstadoFinal());
}
```

## Resumen Rápido

| Aspecto | Descripción |
|---------|-------------|
| **Total de Estados** | 6 |
| **Estados Iniciales** | 1 (FORMATO_A_DILIGENCIADO) |
| **Estados Finales** | 2 (ACEPTADO_POR_COMITE, RECHAZADO_POR_COMITE) |
| **Estados Intermedios** | 3 (PRESENTADO_AL_COORDINADOR, EN_EVALUACION_COMITE, CORRECCIONES_COMITE) |
| **Intentos Máximos** | 3 |
| **Patrón de Diseño** | State + Singleton |
| **Manejo de Errores** | IllegalStateException |

---

## Contacto y Soporte

Para preguntas sobre la implementación del patrón State, contactar al equipo de desarrollo.

**Última actualización**: Diciembre 2025

