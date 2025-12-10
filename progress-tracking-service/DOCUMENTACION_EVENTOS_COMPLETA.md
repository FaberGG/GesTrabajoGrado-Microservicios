# 📤 Documentación Completa de Eventos - Sistema de Gestión de Trabajos de Grado

**Fecha:** Diciembre 6, 2025  
**Versión:** 2.0  
**Estado:** Documentación Centralizada y Completa

---

## 📋 Tabla de Contenidos

1. [Introducción](#-introducción)
2. [Estado Actual de Implementación](#️-estado-actual-de-implementación)
3. [Arquitectura de Eventos](#️-arquitectura-de-eventos)
4. [Eventos por Microservicio](#-eventos-por-microservicio)
   - [Submission Service](#submission-service)
   - [Review Service](#review-service)
5. [Estructura Completa de Eventos](#-estructura-completa-de-eventos)
6. [Información de Participantes](#-información-de-participantes)
7. [Configuración Necesaria](#️-configuración-necesaria)
8. [Validación y Pruebas](#-validación-y-pruebas)
9. [Checklist de Implementación](#-checklist-de-implementación)

---

## 🎯 Introducción

Este documento centraliza **TODA** la información sobre los eventos del sistema de gestión de trabajos de grado. Es la **fuente única de verdad** para:

- ✅ **Cuándo publicar** cada evento desde cada microservicio
- ✅ **Estructura completa** de cada evento (payloads con todos los campos)
- ✅ **Información de participantes** (director, codirector, estudiantes, evaluadores)
- ✅ **Configuración** necesaria en RabbitMQ y cada servicio
- ✅ **Validación** y pruebas end-to-end

### Objetivo del Sistema de Eventos

El sistema utiliza **Event-Driven Architecture** con patrón **CQRS (Command Query Responsibility Segregation)** para:

- ✅ **Desacoplar microservicios**: Los servicios se comunican mediante eventos asíncronos
- ✅ **Historial completo**: Mantener registro inmutable de todos los eventos (Event Sourcing)
- ✅ **Vistas materializadas**: Proporcionar consultas rápidas del estado actual
- ✅ **Auditoría y trazabilidad**: Saber qué pasó, cuándo y quién lo hizo

---

## ⚠️ Estado Actual de Implementación

**Actualizado:** Diciembre 6, 2025

| Microservicio | Consume Eventos | Publica Eventos | Estado Global |
|---------------|----------------|-----------------|---------------|
| **progress-tracking-service** | ✅ 6 tipos | ❌ No publica | ✅ **100% COMPLETO** |
| **notification-service** | ✅ Notificaciones | ❌ No publica | ✅ **COMPLETO** |
| **submission-service** | ❌ No consume | ⚠️ Implementar | ⚠️ **PENDIENTE** |
| **review-service** | ❌ No consume | ⚠️ Implementar | ⚠️ **PENDIENTE** |

### Progress Tracking Service - ✅ COMPLETADO

**Funcionalidades implementadas:**

- ✅ Consume 6 tipos de eventos desde RabbitMQ
- ✅ Guarda historial completo en tabla `historial_eventos` (Event Store)
- ✅ Mantiene vista materializada en tabla `proyecto_estado` (Read Model)
- ✅ Expone 5 endpoints REST de consulta
- ✅ Soporta información completa de todos los participantes:
  - Director (ID + Nombre)
  - Codirector (ID + Nombre)
  - Estudiante 1 (ID + Nombre + Email)
  - Estudiante 2 (ID + Nombre + Email)
  - Coordinador (en historial)
  - Evaluadores (en historial)

### Servicios que Deben Publicar Eventos - ⚠️ PENDIENTE

**Submission Service:**
- ⚠️ `formato-a.enviado` - Cuando se crea Formato A v1
- ⚠️ `formato-a.reenviado` - Cuando se reenvía Formato A v2/v3
- ⚠️ `anteproyecto.enviado` - Cuando se envía el anteproyecto

**Review Service:**
- ⚠️ `formatoa.evaluado` - Cuando coordinador evalúa Formato A
- ⚠️ `evaluadores.asignados` - Cuando se asignan evaluadores
- ⚠️ `anteproyecto.evaluado` - Cuando evaluadores evalúan anteproyecto

---

## 🏗️ Arquitectura de Eventos

### Flujo General del Sistema

```
┌─────────────────────────────┐
│   SUBMISSION SERVICE        │
│  (Commands - Write Model)   │
│                             │
│  • POST /formatoA           │
│  • POST /formatoA/reenviar  │
│  • POST /anteproyecto       │
└─────────────────────────────┘
              │
              │ Publica eventos
              │ (formato-a.*, anteproyecto.*)
              ▼
        ┌──────────┐
        │ RabbitMQ │
        │ Message  │
        │  Broker  │
        └──────────┘
              │
              │ Consume eventos
              ▼
┌─────────────────────────────┐
│  PROGRESS TRACKING SERVICE  │
│   (Queries - Read Model)    │
│                             │
│  • Historial eventos        │
│  • Vista materializada      │
│  • GET /proyectos/{id}      │
└─────────────────────────────┘


┌─────────────────────────────┐
│      REVIEW SERVICE         │
│  (Commands - Write Model)   │
│                             │
│  • POST /evaluar            │
│  • POST /asignar-evaluadores│
└─────────────────────────────┘
              │
              │ Publica eventos
              │ (formatoa.evaluado, etc.)
              ▼
        ┌──────────┐
        │ RabbitMQ │
        └──────────┘
```

### Exchanges y Routing Keys

| Exchange | Tipo | Routing Keys | Servicios Publicadores | Servicios Consumidores |
|----------|------|-------------|----------------------|----------------------|
| `formato-a-exchange` | Direct | `formato-a.enviado`<br>`formato-a.reenviado` | submission-service | progress-tracking, notification |
| `anteproyecto-exchange` | Direct | `anteproyecto.enviado` | submission-service | progress-tracking, notification |
| `evaluacion-exchange` | Direct | `formatoa.evaluado`<br>`evaluadores.asignados`<br>`anteproyecto.evaluado` | review-service | progress-tracking, notification |
| `proyecto-exchange` | Direct | `proyecto.rechazado-definitivamente` | review-service | progress-tracking, notification |

---

## 📡 Eventos por Microservicio

## SUBMISSION SERVICE

### 1️⃣ Evento: `formato-a.enviado`

**Cuándo publicar:** Al crear la primera versión del Formato A

**Endpoint:** `POST /api/submissions/formatoA`  
**Ubicación:** `FormatoAController.crearFormatoA()`  
**Momento:** Inmediatamente **DESPUÉS** de guardar en BD

**Exchange:** `formato-a-exchange`  
**Routing Key:** `formato-a.enviado`

#### Estructura Completa del Payload

```json
{
  // ========== INFORMACIÓN DEL PROYECTO ==========
  "proyectoId": 123,
  "version": 1,
  "titulo": "Sistema de IA para análisis de datos educativos",
  "modalidad": "INDIVIDUAL",
  "programa": "INGENIERIA_SISTEMAS",
  "descripcion": "Primera versión del Formato A",
  
  // ========== TIMESTAMP ==========
  "timestamp": "2025-12-06T10:30:00",
  
  // ========== USUARIO RESPONSABLE (quien ejecuta la acción) ==========
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR",
  
  // ========== DIRECTOR DEL PROYECTO ==========
  "directorId": 12,
  "directorNombre": "Dr. Juan Pérez",
  
  // ========== ESTUDIANTE 1 (OBLIGATORIO) ==========
  "estudiante1Id": 1001,
  "estudiante1Nombre": "María García López",
  "estudiante1Email": "maria.garcia@unicauca.edu.co",
  
  // ========== ESTUDIANTE 2 (OPCIONAL - solo si es DUPLA) ==========
  "estudiante2Id": null,
  "estudiante2Nombre": null,
  "estudiante2Email": null,
  
  // ========== ARRAY DE ESTUDIANTES (formato alternativo) ==========
  "estudiantes": [
    {
      "id": 1001,
      "nombre": "María García López",
      "email": "maria.garcia@unicauca.edu.co"
    }
  ]
}
```

#### Campos Obligatorios

- ✅ `proyectoId` (Long) - ID único del proyecto
- ✅ `version` (Integer) - Siempre 1 para este evento
- ✅ `titulo` (String) - Título del proyecto
- ✅ `modalidad` (String) - "INDIVIDUAL" o "DUPLA"
- ✅ `programa` (String) - Programa académico
- ✅ `directorId` (Long) - ID del director
- ✅ `directorNombre` (String) - **IMPORTANTE:** Nombre completo del director
- ✅ `estudiante1Id` (Long) - ID del estudiante
- ✅ `estudiante1Nombre` (String) - Nombre del estudiante
- ✅ `estudiante1Email` (String) - Email del estudiante
- ✅ `timestamp` (String ISO 8601) - Fecha/hora del evento

#### Código de Implementación

```java
@PostMapping("/formatoA")
public ResponseEntity<?> crearFormatoA(@RequestBody FormatoARequest request) {
    // 1. Validaciones
    // 2. Guardar en BD
    FormatoA formatoAGuardado = formatoARepository.save(formatoA);
    
    // 3. Obtener información del director
    Usuario director = usuarioService.findById(userId);
    
    // 4. Construir lista de estudiantes
    List<Map<String, Object>> estudiantes = new ArrayList<>();
    if (request.getEstudiante1Id() != null) {
        Usuario estudiante1 = usuarioService.findById(request.getEstudiante1Id());
        estudiantes.add(Map.of(
            "id", estudiante1.getId(),
            "nombre", estudiante1.getNombreCompleto(),
            "email", estudiante1.getEmail()
        ));
    }
    
    if (request.getEstudiante2Id() != null) {
        Usuario estudiante2 = usuarioService.findById(request.getEstudiante2Id());
        estudiantes.add(Map.of(
            "id", estudiante2.getId(),
            "nombre", estudiante2.getNombreCompleto(),
            "email", estudiante2.getEmail()
        ));
    }
    
    // 5. ✅ PUBLICAR EVENTO
    rabbitTemplate.convertAndSend(
        "formato-a-exchange",
        "formato-a.enviado",
        Map.of(
            "proyectoId", proyecto.getId(),
            "version", 1,
            "titulo", request.getTitulo(),
            "modalidad", request.getModalidad(),
            "programa", request.getPrograma(),
            "timestamp", LocalDateTime.now().toString(),
            "usuarioResponsableId", director.getId(),
            "usuarioResponsableNombre", director.getNombreCompleto(),
            "usuarioResponsableRol", "DIRECTOR",
            "directorId", director.getId(),
            "directorNombre", director.getNombreCompleto(),
            "estudiante1Id", request.getEstudiante1Id(),
            "estudiante1Nombre", estudiantes.get(0).get("nombre"),
            "estudiante1Email", estudiantes.get(0).get("email"),
            "estudiante2Id", estudiantes.size() > 1 ? estudiantes.get(1).get("id") : null,
            "estudiante2Nombre", estudiantes.size() > 1 ? estudiantes.get(1).get("nombre") : null,
            "estudiante2Email", estudiantes.size() > 1 ? estudiantes.get(1).get("email") : null,
            "estudiantes", estudiantes
        )
    );
    
    log.info("✉️ Evento publicado: formato-a.enviado - Proyecto: {}", proyecto.getId());
    
    return ResponseEntity.ok(formatoA);
}
```

#### Efecto en Progress Tracking Service

- **Estado actualizado:** `EN_PRIMERA_EVALUACION_FORMATO_A`
- **Fase:** `FORMATO_A`
- **Campos guardados en `proyecto_estado`:**
  - `proyecto_id`, `titulo`, `modalidad`, `programa`
  - `director_id`, `director_nombre`
  - `estudiante1_id`, `estudiante1_nombre`, `estudiante1_email`
  - `estudiante2_id`, `estudiante2_nombre`, `estudiante2_email` (si existe)
  - `formato_a_version = 1`
  - `formato_a_intento_actual = 1`
  - `formato_a_estado = "EN_EVALUACION"`

---

### 2️⃣ Evento: `formato-a.reenviado`

**Cuándo publicar:** Al reenviar el Formato A con correcciones (versión 2 o 3)

**Endpoint:** `POST /api/submissions/formatoA/reenviar/{proyectoId}`  
**Ubicación:** `FormatoAController.reenviarFormatoA()`  
**Momento:** Inmediatamente **DESPUÉS** de guardar la nueva versión

**Exchange:** `formato-a-exchange`  
**Routing Key:** `formato-a.reenviado`

#### Estructura del Payload

```json
{
  "proyectoId": 123,
  "version": 2,  // o 3
  "titulo": "Sistema de IA para análisis de datos educativos",
  "modalidad": "INDIVIDUAL",
  "programa": "INGENIERIA_SISTEMAS",
  "descripcion": "Correcciones aplicadas según observaciones",
  "timestamp": "2025-12-06T14:15:00",
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR",
  "directorId": 12,
  "directorNombre": "Dr. Juan Pérez",
  "estudiante1Id": 1001,
  "estudiante1Nombre": "María García López",
  "estudiante1Email": "maria.garcia@unicauca.edu.co",
  "estudiante2Id": null,
  "estudiante2Nombre": null,
  "estudiante2Email": null,
  "estudiantes": [
    {
      "id": 1001,
      "nombre": "María García López",
      "email": "maria.garcia@unicauca.edu.co"
    }
  ]
}
```

**Nota:** La estructura es idéntica a `formato-a.enviado`, solo cambia el `version` (2 o 3)

#### Código de Implementación

```java
@PostMapping("/formatoA/reenviar/{proyectoId}")
public ResponseEntity<?> reenviarFormatoA(
        @PathVariable Long proyectoId,
        @RequestBody FormatoARequest request) {
    
    // 1. Validaciones
    // 2. Guardar nueva versión
    FormatoA nuevoFormatoA = formatoARepository.save(formatoA);
    int versionActual = nuevoFormatoA.getVersion(); // 2 o 3
    
    // 3. Obtener información
    Usuario director = usuarioService.findById(userId);
    // ... construir lista de estudiantes (igual que antes)
    
    // 4. ✅ PUBLICAR EVENTO
    rabbitTemplate.convertAndSend(
        "formato-a-exchange",
        "formato-a.reenviado",  // ⚠️ Routing key diferente
        Map.of(
            "proyectoId", proyectoId,
            "version", versionActual,  // 2 o 3
            "titulo", proyecto.getTitulo(),
            "timestamp", LocalDateTime.now().toString(),
            "directorId", director.getId(),
            "directorNombre", director.getNombreCompleto(),
            // ... resto de campos igual que formato-a.enviado
        )
    );
    
    log.info("✉️ Evento publicado: formato-a.reenviado v{} - Proyecto: {}", 
             versionActual, proyectoId);
    
    return ResponseEntity.ok(nuevoFormatoA);
}
```

#### Efecto en Progress Tracking Service

- **Estado actualizado:** 
  - Si version=2: `EN_SEGUNDA_EVALUACION_FORMATO_A`
  - Si version=3: `EN_TERCERA_EVALUACION_FORMATO_A`
- **Campos actualizados:**
  - `formato_a_version = 2 o 3`
  - `formato_a_intento_actual = 2 o 3`
  - `formato_a_estado = "EN_EVALUACION"`

---

### 3️⃣ Evento: `anteproyecto.enviado`

**Cuándo publicar:** Al enviar el anteproyecto (después de Formato A aprobado)

**Endpoint:** `POST /api/submissions/anteproyecto`  
**Ubicación:** `AnteproyectoController.crearAnteproyecto()`  
**Momento:** Inmediatamente **DESPUÉS** de guardar en BD

**Exchange:** `anteproyecto-exchange`  
**Routing Key:** `anteproyecto.enviado`

#### Estructura del Payload

```json
{
  // ========== INFORMACIÓN DEL PROYECTO ==========
  "proyectoId": 123,
  "titulo": "Sistema de IA para análisis de datos educativos",
  "modalidad": "DUPLA",
  "programa": "INGENIERIA_SISTEMAS",
  "descripcion": "Anteproyecto completo con todos los anexos",
  
  // ========== TIMESTAMP ==========
  "timestamp": "2025-12-06T16:45:00",
  
  // ========== USUARIO RESPONSABLE ==========
  "usuarioResponsableId": 12,
  "usuarioResponsableNombre": "Dr. Juan Pérez",
  "usuarioResponsableRol": "DIRECTOR",
  
  // ========== DIRECTOR ==========
  "directorId": 12,
  "directorNombre": "Dr. Juan Pérez",
  
  // ========== CODIRECTOR (OPCIONAL - asignado en esta fase) ==========
  "codirectorId": 15,
  "codirectorNombre": "Dra. Ana Martínez",
  
  // ========== ESTUDIANTES ==========
  "estudiante1Id": 1001,
  "estudiante1Nombre": "María García López",
  "estudiante1Email": "maria.garcia@unicauca.edu.co",
  
  "estudiante2Id": 1002,
  "estudiante2Nombre": "Carlos López Ramírez",
  "estudiante2Email": "carlos.lopez@unicauca.edu.co",
  
  "estudiantes": [
    {
      "id": 1001,
      "nombre": "María García López",
      "email": "maria.garcia@unicauca.edu.co"
    },
    {
      "id": 1002,
      "nombre": "Carlos López Ramírez",
      "email": "carlos.lopez@unicauca.edu.co"
    }
  ]
}
```

**IMPORTANTE:** En esta fase se incluye el codirector (si existe).

#### Código de Implementación

```java
@PostMapping("/anteproyecto")
public ResponseEntity<?> crearAnteproyecto(@RequestBody AnteproyectoRequest request) {
    // 1. Validaciones
    // 2. Guardar en BD
    Anteproyecto anteproyectoGuardado = anteproyectoRepository.save(anteproyecto);
    
    // 3. Obtener información de director y codirector
    Usuario director = usuarioService.findById(userId);
    Usuario codirector = request.getCodirectorId() != null 
        ? usuarioService.findById(request.getCodirectorId()) 
        : null;
    
    // 4. Construir lista de estudiantes
    // ... (igual que en formato-a.enviado)
    
    // 5. ✅ PUBLICAR EVENTO
    Map<String, Object> payload = new HashMap<>();
    payload.put("proyectoId", proyecto.getId());
    payload.put("titulo", request.getTitulo());
    payload.put("modalidad", request.getModalidad());
    payload.put("programa", request.getPrograma());
    payload.put("timestamp", LocalDateTime.now().toString());
    payload.put("usuarioResponsableId", director.getId());
    payload.put("usuarioResponsableNombre", director.getNombreCompleto());
    payload.put("usuarioResponsableRol", "DIRECTOR");
    payload.put("directorId", director.getId());
    payload.put("directorNombre", director.getNombreCompleto());
    
    // Codirector (opcional)
    if (codirector != null) {
        payload.put("codirectorId", codirector.getId());
        payload.put("codirectorNombre", codirector.getNombreCompleto());
    }
    
    // Estudiantes
    payload.put("estudiante1Id", request.getEstudiante1Id());
    payload.put("estudiante1Nombre", estudiantes.get(0).get("nombre"));
    payload.put("estudiante1Email", estudiantes.get(0).get("email"));
    if (estudiantes.size() > 1) {
        payload.put("estudiante2Id", estudiantes.get(1).get("id"));
        payload.put("estudiante2Nombre", estudiantes.get(1).get("nombre"));
        payload.put("estudiante2Email", estudiantes.get(1).get("email"));
    }
    payload.put("estudiantes", estudiantes);
    
    rabbitTemplate.convertAndSend(
        "anteproyecto-exchange",
        "anteproyecto.enviado",
        payload
    );
    
    log.info("✉️ Evento publicado: anteproyecto.enviado - Proyecto: {}", proyecto.getId());
    
    return ResponseEntity.ok(anteproyecto);
}
```

#### Efecto en Progress Tracking Service

- **Estado actualizado:** `ANTEPROYECTO_ENVIADO`
- **Fase:** `ANTEPROYECTO`
- **Campos guardados/actualizados:**
  - `codirector_id`, `codirector_nombre` (nuevo)
  - Actualiza toda la información del proyecto si cambió

---

## REVIEW SERVICE

### 4️⃣ Evento: `formatoa.evaluado`

**Cuándo publicar:** Cuando el coordinador evalúa el Formato A

**Endpoint:** `POST /api/reviews/formatoA/{proyectoId}/evaluar`  
**Ubicación:** `FormatoAReviewController.evaluarFormatoA()`  
**Momento:** Inmediatamente **DESPUÉS** de guardar la evaluación

**Exchange:** `evaluacion-exchange`  
**Routing Key:** `formatoa.evaluado`

#### Estructura del Payload

```json
{
  // ========== INFORMACIÓN DE LA EVALUACIÓN ==========
  "proyectoId": 123,
  "resultado": "RECHAZADO",  // "APROBADO" o "RECHAZADO"
  "observaciones": "Falta claridad en los objetivos específicos. La metodología debe ser más detallada.",
  "version": 1,  // 1, 2, o 3
  "rechazadoDefinitivo": false,  // true si es el tercer rechazo
  
  // ========== TIMESTAMP ==========
  "timestamp": "2025-12-06T11:00:00",
  
  // ========== USUARIO RESPONSABLE (Coordinador) ==========
  "usuarioResponsableId": 45,
  "usuarioResponsableNombre": "Dr. Carlos Coordinador",
  "usuarioResponsableRol": "COORDINADOR",
  
  // ========== ESTUDIANTES (opcional pero recomendado) ==========
  "estudiantes": [
    {
      "id": 1001,
      "nombre": "María García López",
      "email": "maria.garcia@unicauca.edu.co"
    }
  ]
}
```

#### Código de Implementación

```java
@PostMapping("/{proyectoId}/evaluar")
public ResponseEntity<?> evaluarFormatoA(
        @PathVariable Long proyectoId,
        @RequestBody EvaluacionRequest request) {
    
    // 1. Validaciones
    // 2. Guardar evaluación en BD
    Evaluacion evaluacionGuardada = evaluacionRepository.save(evaluacion);
    
    // 3. Obtener proyecto con información de estudiantes
    Proyecto proyecto = proyectoRepository.findById(proyectoId)
        .orElseThrow(() -> new NotFoundException("Proyecto no encontrado"));
    
    // 4. Construir lista de estudiantes
    List<Map<String, Object>> estudiantes = new ArrayList<>();
    if (proyecto.getEstudiante1() != null) {
        estudiantes.add(Map.of(
            "id", proyecto.getEstudiante1().getId(),
            "nombre", proyecto.getEstudiante1().getNombreCompleto(),
            "email", proyecto.getEstudiante1().getEmail()
        ));
    }
    if (proyecto.getEstudiante2() != null) {
        estudiantes.add(Map.of(
            "id", proyecto.getEstudiante2().getId(),
            "nombre", proyecto.getEstudiante2().getNombreCompleto(),
            "email", proyecto.getEstudiante2().getEmail()
        ));
    }
    
    // 5. Determinar si es rechazo definitivo
    boolean rechazadoDefinitivo = request.getResultado().equals("RECHAZADO") 
                                   && proyecto.getFormatoAVersion() == 3;
    
    // 6. ✅ PUBLICAR EVENTO
    rabbitTemplate.convertAndSend(
        "evaluacion-exchange",
        "formatoa.evaluado",
        Map.of(
            "proyectoId", proyectoId,
            "resultado", request.getResultado(),
            "observaciones", request.getObservaciones(),
            "version", proyecto.getFormatoAVersion(),
            "rechazadoDefinitivo", rechazadoDefinitivo,
            "timestamp", LocalDateTime.now().toString(),
            "usuarioResponsableId", coordinador.getId(),
            "usuarioResponsableNombre", coordinador.getNombreCompleto(),
            "usuarioResponsableRol", "COORDINADOR",
            "estudiantes", estudiantes
        )
    );
    
    log.info("✉️ Evento publicado: formatoa.evaluado - Proyecto: {}, Resultado: {}", 
             proyectoId, request.getResultado());
    
    return ResponseEntity.ok(evaluacion);
}
```

#### Efecto en Progress Tracking Service

**Si APROBADO:**
- Estado: `FORMATO_A_APROBADO`
- Campo: `formato_a_estado = "APROBADO"`

**Si RECHAZADO (v1):**
- Estado: `FORMATO_A_RECHAZADO_1`
- Campo: `formato_a_estado = "RECHAZADO"`

**Si RECHAZADO (v2):**
- Estado: `FORMATO_A_RECHAZADO_2`

**Si RECHAZADO (v3):**
- Estado: `FORMATO_A_RECHAZADO_3`

**Si rechazadoDefinitivo=true:**
- Estado: `FORMATO_A_RECHAZADO_DEFINITIVO`

---

### 5️⃣ Evento: `evaluadores.asignados`

**Cuándo publicar:** Cuando el Jefe de Departamento asigna evaluadores al anteproyecto

**Endpoint:** `POST /api/reviews/anteproyecto/{proyectoId}/asignar-evaluadores`  
**Ubicación:** `AnteproyectoReviewController.asignarEvaluadores()`  
**Momento:** Inmediatamente **DESPUÉS** de guardar los evaluadores

**Exchange:** `evaluacion-exchange`  
**Routing Key:** `evaluadores.asignados`

#### Estructura del Payload

```json
{
  // ========== INFORMACIÓN DEL PROYECTO ==========
  "proyectoId": 123,
  
  // ========== EVALUADORES ASIGNADOS ==========
  "evaluadores": [
    {
      "id": 20,
      "nombre": "Dr. Roberto Evaluador Pérez"
    },
    {
      "id": 21,
      "nombre": "Dra. Laura Evaluadora Gómez"
    }
  ],
  
  // ========== TIMESTAMP ==========
  "timestamp": "2025-12-06T18:00:00",
  
  // ========== USUARIO RESPONSABLE (Jefe de Departamento) ==========
  "usuarioResponsableId": 50,
  "usuarioResponsableNombre": "Ing. Jorge Jefe de Departamento",
  "usuarioResponsableRol": "JEFE_DEPARTAMENTO"
}
```

#### Código de Implementación

```java
@PostMapping("/anteproyecto/{proyectoId}/asignar-evaluadores")
public ResponseEntity<?> asignarEvaluadores(
        @PathVariable Long proyectoId,
        @RequestBody AsignarEvaluadoresRequest request) {
    
    // 1. Validaciones
    // 2. Guardar evaluadores en BD
    List<Evaluador> evaluadoresAsignados = evaluadorRepository.saveAll(evaluadores);
    
    // 3. Construir lista de evaluadores para el evento
    List<Map<String, Object>> evaluadoresInfo = evaluadoresAsignados.stream()
        .map(e -> Map.of(
            "id", e.getId(),
            "nombre", e.getNombreCompleto()
        ))
        .collect(Collectors.toList());
    
    // 4. ✅ PUBLICAR EVENTO
    rabbitTemplate.convertAndSend(
        "evaluacion-exchange",
        "evaluadores.asignados",
        Map.of(
            "proyectoId", proyectoId,
            "evaluadores", evaluadoresInfo,
            "timestamp", LocalDateTime.now().toString(),
            "usuarioResponsableId", jefeDepartamento.getId(),
            "usuarioResponsableNombre", jefeDepartamento.getNombreCompleto(),
            "usuarioResponsableRol", "JEFE_DEPARTAMENTO"
        )
    );
    
    log.info("✉️ Evento publicado: evaluadores.asignados - Proyecto: {}, Cantidad: {}", 
             proyectoId, evaluadoresInfo.size());
    
    return ResponseEntity.ok("Evaluadores asignados correctamente");
}
```

#### Efecto en Progress Tracking Service

- **Estado actualizado:** `ANTEPROYECTO_EN_EVALUACION`
- **Campos actualizados:**
  - `anteproyecto_evaluadores_asignados = true`
- **Historial:** "Se asignaron 2 evaluadores al anteproyecto"

---

### 6️⃣ Evento: `anteproyecto.evaluado`

**Cuándo publicar:** Cuando los evaluadores completan la evaluación del anteproyecto

**Endpoint:** `POST /api/reviews/anteproyecto/{proyectoId}/evaluar`  
**Ubicación:** `AnteproyectoReviewController.evaluarAnteproyecto()`  
**Momento:** Inmediatamente **DESPUÉS** de guardar la evaluación

**Exchange:** `evaluacion-exchange`  
**Routing Key:** `anteproyecto.evaluado`

#### Estructura del Payload

```json
{
  // ========== INFORMACIÓN DE LA EVALUACIÓN ==========
  "proyectoId": 123,
  "resultado": "APROBADO",  // "APROBADO" o "RECHAZADO"
  "observaciones": "Excelente propuesta. El marco teórico está bien fundamentado.",
  
  // ========== TIMESTAMP ==========
  "timestamp": "2025-12-06T20:30:00",
  
  // ========== USUARIO RESPONSABLE (Evaluador) ==========
  "usuarioResponsableId": 20,
  "usuarioResponsableNombre": "Dr. Roberto Evaluador Pérez",
  "usuarioResponsableRol": "EVALUADOR",
  
  // ========== ESTUDIANTES (opcional) ==========
  "estudiantes": [
    {
      "id": 1001,
      "nombre": "María García López",
      "email": "maria.garcia@unicauca.edu.co"
    },
    {
      "id": 1002,
      "nombre": "Carlos López Ramírez",
      "email": "carlos.lopez@unicauca.edu.co"
    }
  ]
}
```

#### Código de Implementación

```java
@PostMapping("/anteproyecto/{proyectoId}/evaluar")
public ResponseEntity<?> evaluarAnteproyecto(
        @PathVariable Long proyectoId,
        @RequestBody EvaluacionRequest request) {
    
    // 1. Validaciones
    // 2. Guardar evaluación
    Evaluacion evaluacionGuardada = evaluacionRepository.save(evaluacion);
    
    // 3. Obtener información de estudiantes
    Proyecto proyecto = proyectoRepository.findById(proyectoId).orElseThrow();
    List<Map<String, Object>> estudiantes = obtenerEstudiantes(proyecto);
    
    // 4. ✅ PUBLICAR EVENTO
    rabbitTemplate.convertAndSend(
        "evaluacion-exchange",
        "anteproyecto.evaluado",
        Map.of(
            "proyectoId", proyectoId,
            "resultado", request.getResultado(),
            "observaciones", request.getObservaciones(),
            "timestamp", LocalDateTime.now().toString(),
            "usuarioResponsableId", evaluador.getId(),
            "usuarioResponsableNombre", evaluador.getNombreCompleto(),
            "usuarioResponsableRol", "EVALUADOR",
            "estudiantes", estudiantes
        )
    );
    
    log.info("✉️ Evento publicado: anteproyecto.evaluado - Proyecto: {}, Resultado: {}", 
             proyectoId, request.getResultado());
    
    return ResponseEntity.ok(evaluacion);
}
```

#### Efecto en Progress Tracking Service

**Si APROBADO:**
- Estado: `ANTEPROYECTO_APROBADO`
- Campo: `anteproyecto_estado = "APROBADO"`

**Si RECHAZADO:**
- Estado: `ANTEPROYECTO_RECHAZADO`
- Campo: `anteproyecto_estado = "RECHAZADO"`

---

### 7️⃣ Evento Especial: `proyecto.rechazado-definitivamente`

**Cuándo publicar:** Automáticamente cuando el Formato A es rechazado por tercera vez

**Puede publicarse desde:** review-service o submission-service

**Exchange:** `proyecto-exchange`  
**Routing Key:** `proyecto.rechazado-definitivamente`

#### Estructura del Payload

```json
{
  "proyectoId": 123,
  "titulo": "Sistema de IA para análisis de datos educativos",
  "intentosRealizados": 3,
  "timestamp": "2025-12-06T12:00:00",
  "usuarioResponsableId": 45,
  "usuarioResponsableNombre": "Dr. Carlos Coordinador",
  "usuarioResponsableRol": "COORDINADOR"
}
```

**Nota:** Este evento puede publicarse automáticamente desde review-service cuando detecta que es la tercera evaluación rechazada.

---

## 📊 Información de Participantes

### Tabla de Participantes por Fase

| Participante | Formato A | Anteproyecto | Evaluación | Almacenado en |
|--------------|-----------|--------------|------------|---------------|
| **Director** | ✅ ID + Nombre | ✅ ID + Nombre | - | `proyecto_estado` |
| **Codirector** | ❌ No | ✅ ID + Nombre | - | `proyecto_estado` |
| **Estudiante 1** | ✅ ID + Nombre + Email | ✅ ID + Nombre + Email | - | `proyecto_estado` |
| **Estudiante 2** | ✅ ID + Nombre + Email (si DUPLA) | ✅ ID + Nombre + Email (si DUPLA) | - | `proyecto_estado` |
| **Coordinador** | - | - | ✅ ID + Nombre | `historial_eventos` |
| **Evaluadores** | - | - | ✅ ID + Nombre | `historial_eventos` |
| **Jefe Dept.** | - | - | ✅ ID + Nombre | `historial_eventos` |

### Campos en Base de Datos

#### Tabla: `proyecto_estado`

```sql
-- Participantes permanentes
director_id BIGINT,
director_nombre VARCHAR(200),
codirector_id BIGINT,
codirector_nombre VARCHAR(200),
estudiante1_id BIGINT,
estudiante1_nombre VARCHAR(200),
estudiante1_email VARCHAR(200),
estudiante2_id BIGINT,
estudiante2_nombre VARCHAR(200),
estudiante2_email VARCHAR(200)
```

#### Tabla: `historial_eventos`

```sql
-- Usuario responsable del evento
usuario_responsable_id BIGINT,
usuario_responsable_nombre VARCHAR(200),
usuario_responsable_rol VARCHAR(50),
-- Información adicional en campo metadata (JSON)
metadata TEXT
```

### Ejemplo de Consulta con Participantes

```bash
GET /api/progress/proyectos/123/estado
```

**Respuesta:**
```json
{
  "proyectoId": 123,
  "titulo": "Sistema de IA...",
  "estadoActual": "ANTEPROYECTO_EN_EVALUACION",
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
  }
}
```

---

## 🔧️ Configuración Necesaria

### RabbitMQ Configuration (Submission Service)

```java
@Configuration
public class RabbitConfig {

    // ========== EXCHANGES ==========
    
    @Bean
    public DirectExchange formatoAExchange() {
        return new DirectExchange("formato-a-exchange", true, false);
    }
    
    @Bean
    public DirectExchange anteproyectoExchange() {
        return new DirectExchange("anteproyecto-exchange", true, false);
    }
    
    // ========== RABBIT TEMPLATE ==========
    
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
```

### RabbitMQ Configuration (Review Service)

```java
@Configuration
public class RabbitConfig {

    // ========== EXCHANGES ==========
    
    @Bean
    public DirectExchange evaluacionExchange() {
        return new DirectExchange("evaluacion-exchange", true, false);
    }
    
    @Bean
    public DirectExchange proyectoExchange() {
        return new DirectExchange("proyecto-exchange", true, false);
    }
    
    // ========== RABBIT TEMPLATE ==========
    
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
```

### Inyección de RabbitTemplate

```java
@RestController
@RequestMapping("/api/submissions/formatoA")
public class FormatoAController {
    
    private final FormatoAService formatoAService;
    private final RabbitTemplate rabbitTemplate; // ✅ INYECTAR
    
    public FormatoAController(
        FormatoAService formatoAService,
        RabbitTemplate rabbitTemplate
    ) {
        this.formatoAService = formatoAService;
        this.rabbitTemplate = rabbitTemplate;
    }
    
    // ... métodos del controller
}
```

---

## 🧪 Validación y Pruebas

### Paso 1: Verificar RabbitMQ

```bash
# Acceder a RabbitMQ Management
http://localhost:15672
# Usuario: admin
# Password: admin_password (según .env)

# Verificar que existan los exchanges:
- formato-a-exchange
- anteproyecto-exchange
- evaluacion-exchange
- proyecto-exchange

# Verificar que existan las queues:
- progress.formato-a.queue
- progress.anteproyecto.queue
- progress.evaluacion.queue
- progress.proyecto.queue
```

### Paso 2: Prueba de Formato A v1

```bash
# 1. Enviar Formato A
POST http://localhost:8082/api/submissions/formatoA
Headers:
  X-User-Id: 12
  X-User-Role: DIRECTOR
Body:
{
  "titulo": "Proyecto Test",
  "modalidad": "INDIVIDUAL",
  "programa": "INGENIERIA_SISTEMAS",
  "estudiante1Id": 1001
}

# 2. Verificar en RabbitMQ Management
# Exchange: formato-a-exchange
# Should show activity in "Message rates"

# 3. Verificar en progress-tracking
GET http://localhost:8085/api/progress/proyectos/1/estado

# Respuesta esperada:
{
  "proyectoId": 1,
  "estadoActual": "EN_PRIMERA_EVALUACION_FORMATO_A",
  "estadoLegible": "En primera evaluación - Formato A",
  "siguientePaso": "Esperar evaluación del coordinador",
  "formatoA": {
    "version": 1,
    "intentoActual": 1,
    "estado": "EN_EVALUACION"
  }
}
```

### Paso 3: Verificar Logs

**Logs de submission-service:**
```
✉️ Evento publicado: formato-a.enviado - Proyecto: 1
```

**Logs de progress-tracking-service:**
```
📥 [FORMATO A] Evento recibido: formato-a.enviado - Payload: {...}
✅ Evento guardado en historial: ID=1
✅ [FORMATO A] Proyecto 1 actualizado a: EN_PRIMERA_EVALUACION_FORMATO_A
```

### Paso 4: Prueba Completa de Flujo

```bash
# 1. Crear Formato A v1
POST /api/submissions/formatoA

# 2. Rechazar (Coordinador)
POST /api/reviews/formatoA/1/evaluar
Body: { "resultado": "RECHAZADO", "observaciones": "..." }

# 3. Reenviar Formato A v2
POST /api/submissions/formatoA/reenviar/1

# 4. Aprobar (Coordinador)
POST /api/reviews/formatoA/1/evaluar
Body: { "resultado": "APROBADO" }

# 5. Enviar Anteproyecto
POST /api/submissions/anteproyecto

# 6. Asignar Evaluadores (Jefe Dept.)
POST /api/reviews/anteproyecto/1/asignar-evaluadores

# 7. Evaluar Anteproyecto (Evaluador)
POST /api/reviews/anteproyecto/1/evaluar
Body: { "resultado": "APROBADO" }

# 8. Verificar estado final
GET /api/progress/proyectos/1/estado
# Debe mostrar: "estadoActual": "ANTEPROYECTO_APROBADO"
```

---

## ✅ Checklist de Implementación

### Submission Service

- [ ] Actualizar `RabbitConfig.java` con exchanges
- [ ] Inyectar `RabbitTemplate` en `FormatoAController`
- [ ] Publicar `formato-a.enviado` en método `crearFormatoA()`
- [ ] Publicar `formato-a.reenviado` en método `reenviarFormatoA()`
- [ ] Inyectar `RabbitTemplate` en `AnteproyectoController`
- [ ] Publicar `anteproyecto.enviado` en método `crearAnteproyecto()`
- [ ] Incluir **directorNombre** en todos los eventos
- [ ] Incluir información completa de estudiantes (ID + Nombre + Email)
- [ ] Agregar logs informativos: `log.info("✉️ Evento publicado: ...")`
- [ ] Probar con Postman/curl

### Review Service

- [ ] Actualizar `RabbitConfig.java` con exchanges
- [ ] Inyectar `RabbitTemplate` en `FormatoAReviewController`
- [ ] Publicar `formatoa.evaluado` en método `evaluarFormatoA()`
- [ ] Incluir información de estudiantes en evento `formatoa.evaluado`
- [ ] Inyectar `RabbitTemplate` en `AnteproyectoReviewController`
- [ ] Publicar `evaluadores.asignados` en método `asignarEvaluadores()`
- [ ] Publicar `anteproyecto.evaluado` en método `evaluarAnteproyecto()`
- [ ] Incluir información de estudiantes en evento `anteproyecto.evaluado`
- [ ] Agregar logs informativos
- [ ] Probar con Postman/curl

### Progress Tracking Service

- [x] ✅ Consumir eventos (ya implementado)
- [x] ✅ Guardar historial completo (ya implementado)
- [x] ✅ Vista materializada (ya implementado)
- [x] ✅ APIs REST de consulta (ya implementado)
- [x] ✅ Soporte para información de participantes (ya implementado)

### Validación End-to-End

- [ ] Verificar que RabbitMQ recibe eventos
- [ ] Verificar que progress-tracking consume eventos
- [ ] Verificar que el estado se actualiza correctamente
- [ ] Verificar que el historial se guarda
- [ ] Verificar información de participantes
- [ ] Probar flujo completo: Formato A v1 → Rechazo → v2 → Aprobación → Anteproyecto → Evaluación
- [ ] Verificar logs en todos los servicios

---

## 📚 Documentos Relacionados

Este documento centraliza la información de los siguientes archivos:

- ✅ `CUANDO_PUBLICAR_EVENTOS.md` - Cuándo publicar eventos
- ✅ `ESTRUCTURA_EVENTOS.md` - Estructura de payloads
- ✅ `EVENTOS_CON_ESTUDIANTES.md` - Información de estudiantes
- ✅ `INFO_DIRECTOR_CODIRECTOR.md` - Información de director/codirector

**Este documento es la ÚNICA FUENTE DE VERDAD para eventos del sistema.**

---

## 📞 Contacto y Soporte

Si tienes dudas sobre la implementación de eventos:

1. Revisa este documento completo
2. Verifica los logs de RabbitMQ Management
3. Consulta los ejemplos de código
4. Prueba con Postman siguiendo las guías

**Última actualización:** Diciembre 6, 2025  
**Versión:** 2.0 - Documentación Centralizada

---

## 🎉 Conclusión

Una vez que **submission-service** y **review-service** implementen la publicación de estos eventos:

✅ **Progress Tracking Service** consumirá automáticamente  
✅ **Historial completo** se guardará en `historial_eventos`  
✅ **Estado actualizado** se proyectará en `proyecto_estado`  
✅ **Vista de estudiante** funcionará completamente  
✅ **Auditoría completa** estará disponible  

**La arquitectura Event-Driven CQRS estará 100% funcional! 🚀**

