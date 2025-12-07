# Feedback: Análisis de Estados vs Requisitos Funcionales

## 📋 Resumen Ejecutivo

Este documento analiza si la implementación actual del patrón State cumple con los requisitos funcionales del sistema de gestión de trabajos de grado. Se identificaron **discrepancias críticas** entre lo implementado y lo solicitado.

**Fecha de análisis**: 6 de diciembre de 2025

---

## ⚠️ PROBLEMAS CRÍTICOS IDENTIFICADOS

### 🔴 Problema #1: Actor Incorrecto para Evaluación del Formato A

**Requisito Funcional (RF-3):**
> "Yo como **coordinador de programa** necesito evaluar un formato A para aprobar, rechazar y dejar observaciones."

**Implementación Actual:**
- Los estados hacen referencia a "**COMITÉ**" en lugar de "**COORDINADOR**"
- Estado: `EN_EVALUACION_COMITE` → Debería ser `EN_EVALUACION_COORDINADOR`
- Estado: `ACEPTADO_POR_COMITE` → Debería ser `ACEPTADO_POR_COORDINADOR`
- Estado: `RECHAZADO_POR_COMITE` → Debería ser `RECHAZADO_POR_COORDINADOR`
- Estado: `CORRECCIONES_COMITE` → Debería ser `CORRECCIONES_COORDINADOR`

**Impacto:**
- ❌ La nomenclatura no refleja el flujo real del negocio
- ❌ Confusión semántica en el código
- ❌ Los logs y mensajes hablan del "comité" cuando debería ser el "coordinador"

**Evidencia en código:**

```java
// EnEvaluacionComiteState.java (línea 9)
/**
 * Estado 3: En Evaluación Comité
 * El comité está evaluando el Formato A  ❌ INCORRECTO
 */
```

```java
// PresentadoAlCoordinadorState.java (línea 25)
System.out.println("📨 Coordinador envía el Formato A al comité de evaluación...");
// ❌ INCORRECTO: El coordinador no envía al comité, él mismo evalúa
```

---

### 🔴 Problema #2: Transición Innecesaria "PRESENTADO_AL_COORDINADOR"

**Análisis del Flujo:**

Según el RF-2 y RF-3:
1. Docente sube Formato A → Sistema notifica al coordinador
2. Coordinador evalúa el Formato A

**Implementación Actual:**
1. `FORMATO_A_DILIGENCIADO` → (docente presenta) → `PRESENTADO_AL_COORDINADOR`
2. `PRESENTADO_AL_COORDINADOR` → (coordinador envía) → `EN_EVALUACION_COMITE`
3. `EN_EVALUACION_COMITE` → (comité evalúa) → ...

**Problema:**
- El estado `PRESENTADO_AL_COORDINADOR` tiene una operación `enviarAComite()` que no existe en los requisitos
- Crea una transición intermedia innecesaria
- El coordinador **evalúa directamente**, no "envía a un comité"

**Flujo Correcto Esperado:**
```
FORMATO_A_DILIGENCIADO → (docente presenta) → EN_EVALUACION_COORDINADOR → (coordinador evalúa) → APROBADO/RECHAZADO/CORRECCIONES
```

---

### 🟡 Problema #3: Falta el Estado del Anteproyecto

**Requisito Funcional (RF-6):**
> "Yo como docente necesito **subir el anteproyecto** para continuar con el proceso de proyecto de grado."
> "El docente, una vez **aprobado el Formato A** del proyecto, puede subir el anteproyecto..."

**Requisito Funcional (RF-7 y RF-8):**
> "Yo como jefe de departamento necesito ver los **anteproyectos** que han sido subidos..."
> "Y como jefe de departamento necesito delegar dos docentes del departamento para que **evalúen un anteproyecto**."

**Implementación Actual:**
- ✅ Estados para Formato A están implementados
- ❌ **NO existe ningún estado** para el ciclo de vida del Anteproyecto

**Estados Faltantes:**
- `ANTEPROYECTO_SUBIDO`
- `EN_EVALUACION_JEFATURA` o `ESPERANDO_ASIGNACION_EVALUADORES`
- `EN_EVALUACION_ANTEPROYECTO` (por los 2 evaluadores)
- `ANTEPROYECTO_APROBADO`
- `ANTEPROYECTO_RECHAZADO`
- Posiblemente: `CORRECCIONES_ANTEPROYECTO`

**Impacto:**
- ❌ El ciclo de vida del proyecto está **incompleto**
- ❌ Solo cubre hasta la aprobación del Formato A
- ❌ No soporta los RF-6, RF-7, RF-8

---

### 🟡 Problema #4: Estados Visibles para el Estudiante (RF-5)

**Requisito Funcional (RF-5):**
> "Yo como estudiante necesito entrar a la plataforma y **ver el estado de mi proyecto de grado**."
> Estados sugeridos: "en primera evaluación, formato A, en segunda evaluación formato A, en tercera evaluación formato A, aceptado formato A y rechazado formato A"

**Implementación Actual:**
- Estados técnicos: `FORMATO_A_DILIGENCIADO`, `PRESENTADO_AL_COORDINADOR`, etc.
- No hay mapeo explícito a nombres "amigables" para estudiantes

**Problema:**
- Los nombres técnicos pueden no ser comprensibles para estudiantes
- Falta lógica para mostrar estados "humanizados"

**Sugerencia de Mapeo:**

| Estado Técnico | Estado Visible para Estudiante |
|---------------|-------------------------------|
| `EN_EVALUACION_COORDINADOR` | "Formato A - En 1ra evaluación" |
| `CORRECCIONES_COORDINADOR` (intento 1) | "Formato A - En 2da evaluación" |
| `CORRECCIONES_COORDINADOR` (intento 2) | "Formato A - En 3ra evaluación" |
| `ACEPTADO_POR_COORDINADOR` | "Formato A - Aceptado ✓" |
| `RECHAZADO_POR_COORDINADOR` | "Formato A - Rechazado ✗" |

---

## ✅ ASPECTOS CORRECTOS DE LA IMPLEMENTACIÓN

### 1. Límite de 3 Intentos ✅

**Requisito (RF-4):**
> "Después de un **tercer intento**, el proyecto es rechazado definitivamente..."

**Implementación:**
```java
if (proyecto.getNumeroIntentos() >= 3) {
    cambiarEstado(proyecto, RechazadoPorComiteState.getInstance());
}
```
✅ **Correcto**: La lógica de 3 intentos está bien implementada.

---

### 2. Estados Finales ✅

**Implementación:**
- `ACEPTADO_POR_COMITE` (aunque el nombre está mal, el concepto de estado final es correcto)
- `RECHAZADO_POR_COMITE` (idem)

✅ **Correcto**: El concepto de estados terminales está bien implementado.

---

### 3. Ciclo de Retroalimentación ✅

**Requisito (RF-4):**
> "Yo como docente necesito subir una nueva versión del formato A cuando hubo una evaluación de rechazado..."

**Implementación:**
```
EN_EVALUACION_COMITE → CORRECCIONES_COMITE → (subir nueva versión) → EN_EVALUACION_COMITE
```

✅ **Correcto**: El ciclo de correcciones está bien diseñado (aunque los nombres de estados están mal).

---

### 4. Patrón State Bien Aplicado ✅

✅ **Correcto**: 
- Uso del patrón Singleton para estados
- Encapsulación de comportamiento por estado
- Validaciones y transiciones controladas
- Manejo de excepciones apropiado

---

## 📊 TABLA COMPARATIVA: REQUISITOS VS IMPLEMENTACIÓN

| Requisito | Descripción | Estado en Implementación | ¿Cumple? |
|-----------|-------------|-------------------------|----------|
| **RF-1** | Registro de docente | Fuera del alcance de este módulo | N/A |
| **RF-2** | Docente sube Formato A | Estado inicial: `FORMATO_A_DILIGENCIADO` | ✅ Parcial* |
| **RF-3** | **Coordinador** evalúa Formato A | Implementado como **"comité"** | ❌ NO |
| **RF-4** | Subir nueva versión (hasta 3 intentos) | `CORRECCIONES_COMITE` + límite de intentos | ✅ Parcial* |
| **RF-5** | Estudiante ve estado del proyecto | Estados técnicos sin mapeo amigable | 🟡 Parcial |
| **RF-6** | Docente sube anteproyecto | **NO IMPLEMENTADO** | ❌ NO |
| **RF-7** | Jefe de depto. lista anteproyectos | **NO IMPLEMENTADO** | ❌ NO |
| **RF-8** | Jefe asigna 2 evaluadores | **NO IMPLEMENTADO** | ❌ NO |

*Nota: Marcado como "Parcial" porque la lógica es correcta pero los nombres de actores son incorrectos.

---

## 🎯 PROPUESTA DE CORRECCIÓN

### Estados Correctos para el Formato A

```
1. FORMATO_A_DILIGENCIADO (Estado inicial)
   ↓ presentarAlCoordinador()
   
2. EN_EVALUACION_COORDINADOR (El coordinador está evaluando)
   ↓ evaluar(aprobado, comentarios)
   
   ├─ Si aprobado → 3. FORMATO_A_APROBADO (Estado final exitoso)
   │
   └─ Si rechazado:
      ├─ intentos < 3 → 4. CORRECCIONES_SOLICITADAS
      │                    ↓ subirNuevaVersion()
      │                    └─ Regresa a 2. EN_EVALUACION_COORDINADOR
      │
      └─ intentos >= 3 → 5. FORMATO_A_RECHAZADO (Estado final)
```

### Estados Adicionales para el Anteproyecto

```
6. ANTEPROYECTO_PRESENTADO
   ↓ asignarEvaluadores(evaluador1Id, evaluador2Id)
   
7. EN_EVALUACION_ANTEPROYECTO
   ↓ evaluarAnteproyecto(aprobado, comentarios)
   
   ├─ Si ambos evaluadores aprueban → 8. ANTEPROYECTO_APROBADO
   │
   └─ Si alguno rechaza → 9. ANTEPROYECTO_RECHAZADO
                          o 10. CORRECCIONES_ANTEPROYECTO (si aplica)
```

---

## 🔧 CAMBIOS NECESARIOS EN EL CÓDIGO

### 1. Renombrar Clases de Estados

| Clase Actual | Clase Correcta |
|-------------|----------------|
| `EnEvaluacionComiteState.java` | `EnEvaluacionCoordinadorState.java` |
| `AceptadoPorComiteState.java` | `FormatoAAprobadoState.java` |
| `RechazadoPorComiteState.java` | `FormatoARechazadoState.java` |
| `CorreccionesComiteState.java` | `CorreccionesSolicitadasState.java` |

### 2. Eliminar/Simplificar Estado Intermedio

**Opción A**: Eliminar `PresentadoAlCoordinadorState` y pasar directo a evaluación:
```
FORMATO_A_DILIGENCIADO → presentarAlCoordinador() → EN_EVALUACION_COORDINADOR
```

**Opción B**: Mantenerlo pero renombrar la operación `enviarAComite()` a `iniciarEvaluacion()`:
```java
@Override
public void iniciarEvaluacion(ProyectoSubmission proyecto) {
    System.out.println("📨 Coordinador inicia la evaluación del Formato A...");
    cambiarEstado(proyecto, EnEvaluacionCoordinadorState.getInstance());
}
```

### 3. Actualizar Interfaz `IEstadoSubmission`

```java
public interface IEstadoSubmission {
    void presentarAlCoordinador(ProyectoSubmission proyecto);
    void iniciarEvaluacion(ProyectoSubmission proyecto); // Renombrado
    void evaluar(ProyectoSubmission proyecto, boolean aprobado, String comentarios);
    void subirNuevaVersion(ProyectoSubmission proyecto);
    
    // NUEVOS MÉTODOS PARA ANTEPROYECTO:
    void presentarAnteproyecto(ProyectoSubmission proyecto);
    void asignarEvaluadores(ProyectoSubmission proyecto, Long eval1Id, Long eval2Id);
    void evaluarAnteproyecto(ProyectoSubmission proyecto, boolean aprobado, String comentarios);
    
    String getNombreEstado();
    boolean esEstadoFinal();
    String getEstadoAmigableParaEstudiante(); // NUEVO para RF-5
}
```

### 4. Agregar Estados para Anteproyecto

Crear nuevas clases:
- `AnteproyectoPresentadoState.java`
- `EnEvaluacionAnteproyectoState.java`
- `AnteproyectoAprobadoState.java`
- `AnteproyectoRechazadoState.java`

### 5. Actualizar Modelo `ProyectoSubmission`

Agregar campos:
```java
@Column(name = "ruta_anteproyecto")
private String rutaAnteproyecto;

@Column(name = "fecha_subida_anteproyecto")
private LocalDateTime fechaSubidaAnteproyecto;

@Column(name = "evaluador_1_id")
private Long evaluador1Id;

@Column(name = "evaluador_2_id")
private Long evaluador2Id;

@Column(name = "comentarios_evaluador_1", columnDefinition = "TEXT")
private String comentariosEvaluador1;

@Column(name = "comentarios_evaluador_2", columnDefinition = "TEXT")
private String comentariosEvaluador2;
```

---

## 📝 MENSAJES DE LOG A ACTUALIZAR

### Actual (Incorrecto):
```java
System.out.println("📨 Coordinador envía el Formato A al comité de evaluación...");
System.out.println("✅ Formato A APROBADO por el comité");
System.out.println("❌ Formato A RECHAZADO por el comité");
```

### Correcto:
```java
System.out.println("📨 Coordinador inicia la evaluación del Formato A...");
System.out.println("✅ Formato A APROBADO por el coordinador");
System.out.println("❌ Formato A RECHAZADO por el coordinador");
```

---

## 🎓 RECOMENDACIONES ADICIONALES

### 1. Documentación de Arquitectura
- Crear un diagrama de secuencia que muestre el flujo completo desde el Formato A hasta el Anteproyecto
- Documentar las responsabilidades de cada actor (docente, coordinador, jefe de departamento, evaluadores)

### 2. Eventos del Sistema
Los requisitos mencionan "notificaciones asíncronas". Asegurar que cada transición publique eventos:
- `FormatoAPresentadoEvent` → Notifica al coordinador (RF-2)
- `FormatoAEvaluadoEvent` → Notifica a docentes y estudiantes (RF-3)
- `AnteproyectoPresentadoEvent` → Notifica al jefe de departamento (RF-6)
- `EvaluadoresAsignadosEvent` → Notifica a los 2 evaluadores (RF-8)

### 3. Validaciones de Negocio
- Validar que solo el coordinador pueda evaluar el Formato A
- Validar que solo el jefe de departamento pueda asignar evaluadores
- Validar que solo los evaluadores asignados puedan evaluar el anteproyecto

### 4. Testing
Crear casos de prueba que validen:
- El flujo completo desde `FORMATO_A_DILIGENCIADO` hasta `ANTEPROYECTO_APROBADO`
- Los 3 intentos de corrección del Formato A
- La asignación correcta de 2 evaluadores
- Las notificaciones a los actores correctos

---

## 📊 RESUMEN DE CUMPLIMIENTO

### ✅ Fortalezas de la Implementación Actual
1. Patrón State correctamente aplicado
2. Lógica de 3 intentos funciona bien
3. Estados finales bien definidos
4. Ciclo de retroalimentación implementado
5. Singleton pattern para optimización

### ❌ Debilidades Críticas
1. **Actores incorrectos**: Se menciona "comité" en lugar de "coordinador"
2. **Estado intermedio innecesario**: `PresentadoAlCoordinadorState` con operación `enviarAComite()`
3. **Ciclo de vida incompleto**: Falta toda la fase del anteproyecto (RF-6, RF-7, RF-8)
4. **Falta mapeo de estados** para estudiantes (RF-5)

### 🔢 Puntuación de Cumplimiento

| Aspecto | Cumplimiento |
|---------|--------------|
| **Formato A - Lógica** | 85% ✅ |
| **Formato A - Nomenclatura** | 0% ❌ |
| **Anteproyecto - Implementación** | 0% ❌ |
| **Notificaciones asíncronas** | No evaluado* |
| **Estados para estudiantes (RF-5)** | 40% 🟡 |

*Nota: Las notificaciones probablemente se manejan en el `notification-service`, fuera del alcance del patrón State.

---

## 🚦 PRIORIZACIÓN DE CAMBIOS

### 🔴 Prioridad ALTA (Bloqueante)
1. Renombrar estados y clases: "comité" → "coordinador"
2. Actualizar logs y mensajes de error
3. Revisar/eliminar el estado `PresentadoAlCoordinadorState`

### 🟡 Prioridad MEDIA (Funcionalidad incompleta)
4. Implementar estados del anteproyecto (RF-6, RF-7, RF-8)
5. Agregar método `getEstadoAmigableParaEstudiante()` (RF-5)
6. Crear tests de integración para el flujo completo

### 🟢 Prioridad BAJA (Mejoras)
7. Documentar diagramas de secuencia
8. Optimizar validaciones de roles
9. Agregar más logs para auditoría

---

## ✍️ CONCLUSIÓN

La implementación del patrón State está **técnicamente bien diseñada** y la lógica de transiciones es **correcta**. Sin embargo, existe una **discrepancia crítica en la nomenclatura** que no refleja el proceso de negocio real descrito en los requisitos funcionales.

**El problema principal** es que se asumió que un "comité" evalúa el Formato A, cuando en realidad es el **coordinador de programa** quien lo hace. Adicionalmente, **falta implementar todo el ciclo de vida del anteproyecto**, que representa aproximadamente el 40% de los requisitos funcionales.

### Recomendación Final:
**Refactorizar la nomenclatura** de estados y métodos para alinearla con los actores reales del proceso, y **extender el patrón State** para incluir los estados del anteproyecto.

---

**Elaborado por**: Sistema de Análisis de Calidad de Software  
**Fecha**: 6 de diciembre de 2025  
**Versión del documento**: 1.0

