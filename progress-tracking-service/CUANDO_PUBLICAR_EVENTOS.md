# 📤 Guía: Cuándo Submission-Service Debe Publicar Eventos

## ⚠️ **ADVERTENCIA IMPORTANTE**

> **Este documento es una GUÍA DE IMPLEMENTACIÓN FUTURA.**
> 
> **Estado actual:**
> - ✅ **progress-service**: YA ESTÁ COMPLETAMENTE IMPLEMENTADO y listo para consumir eventos
> - ❌ **submission-service**: AÚN NO PUBLICA ESTOS EVENTOS (debes implementarlo)
> 
> **Este documento te dice CÓMO y CUÁNDO implementar la publicación de eventos en submission-service.**

---

## 🎯 Objetivo

Este documento especifica **EXACTAMENTE** qué código debes **AGREGAR** a **submission-service** para que publique eventos de dominio y **progress-service** pueda consumirlos.

**📌 Nada de esto está implementado todavía en submission-service. Son cambios que DEBES HACER.**

---

## 📍 Puntos de Publicación de Eventos

### **1️⃣ RF2: Crear Formato A (Primera Versión)**

**Endpoint:** `POST /api/submissions/formatoA`

**Ubicación en el código:** `FormatoAController.crearFormatoA()`

**Cuándo publicar:** **Inmediatamente después** de guardar el FormatoA en la base de datos

**Código a agregar:**

```java
// DESPUÉS de guardar en BD
FormatoA formatoAGuardado = formatoARepository.save(formatoA);

// ✅ PUBLICAR EVENTO PARA PROGRESS-SERVICE
rabbitTemplate.convertAndSend(
    "formato-a-exchange",           // Exchange
    "formato-a.enviado",             // Routing key
    Map.of(
        "proyectoId", proyecto.getId(),
        "version", 1,
        "titulo", formatoARequest.getTitulo(),
        "directorId", userId,
        "estudiante1Id", formatoARequest.getEstudiante1Id(),
        "estudiante2Id", formatoARequest.getEstudiante2Id(), // puede ser null
        "timestamp", LocalDateTime.now().toString()
    )
);

log.info("✉️ Evento publicado: formato-a.enviado - Proyecto: {}", proyecto.getId());
```

---

### **2️⃣ RF4: Reenviar Formato A (v2 o v3)**

**Endpoint:** `POST /api/submissions/formatoA/reenviar/{proyectoId}`

**Ubicación en el código:** `FormatoAController.reenviarFormatoA()`

**Cuándo publicar:** **Inmediatamente después** de guardar el nuevo FormatoA

**Código a agregar:**

```java
// DESPUÉS de guardar en BD
FormatoA nuevoFormatoA = formatoARepository.save(formatoA);
int versionActual = nuevoFormatoA.getVersion(); // 2 o 3

// ✅ PUBLICAR EVENTO PARA PROGRESS-SERVICE
rabbitTemplate.convertAndSend(
    "formato-a-exchange",           // Exchange
    "formato-a.reenviado",          // Routing key (diferente a enviado)
    Map.of(
        "proyectoId", proyectoId,
        "version", versionActual,
        "titulo", proyecto.getTitulo(),
        "directorId", userId,
        "timestamp", LocalDateTime.now().toString()
    )
);

log.info("✉️ Evento publicado: formato-a.reenviado v{} - Proyecto: {}", versionActual, proyectoId);
```

---

### **3️⃣ RF4: Tercer Rechazo → Rechazo Definitivo**

**Ubicación:** **Review-Service** (cuando coordinar rechaza v3)

**PERO** submission-service puede publicar al detectar el estado:

**Cuándo publicar:** Cuando el proyecto alcanza estado `RECHAZADO_DEFINITIVO`

**Código a agregar en submission-service:**

```java
// Cuando se detecta que ya hay 3 rechazos
if (proyecto.getEstado() == EstadoProyecto.RECHAZADO_DEFINITIVO) {
    
    // ✅ PUBLICAR EVENTO DE RECHAZO DEFINITIVO
    rabbitTemplate.convertAndSend(
        "proyecto-exchange",                    // Exchange
        "proyecto.rechazado-definitivamente",   // Routing key
        Map.of(
            "proyectoId", proyecto.getId(),
            "titulo", proyecto.getTitulo(),
            "intentosRealizados", 3,
            "timestamp", LocalDateTime.now().toString()
        )
    );
    
    log.warn("⛔ Evento publicado: proyecto.rechazado-definitivamente - Proyecto: {}", proyecto.getId());
}
```

---

### **4️⃣ RF6: Enviar Anteproyecto**

**Endpoint:** `POST /api/submissions/anteproyecto`

**Ubicación en el código:** `AnteproyectoController.crearAnteproyecto()`

**Cuándo publicar:** **Inmediatamente después** de guardar el Anteproyecto

**Código a agregar:**

```java
// DESPUÉS de guardar en BD
Anteproyecto anteproyectoGuardado = anteproyectoRepository.save(anteproyecto);

// ✅ PUBLICAR EVENTO PARA PROGRESS-SERVICE
rabbitTemplate.convertAndSend(
    "anteproyecto-exchange",        // Exchange
    "anteproyecto.enviado",         // Routing key
    Map.of(
        "proyectoId", proyecto.getId(),
> **⚠️ IMPORTANTE:** Estos cambios NO ESTÁN IMPLEMENTADOS. Debes hacerlos manualmente.

### **1. Actualizar `RabbitConfig.java` en submission-service**
        "directorId", userId,
**Ubicación:** `submission-service/src/main/java/co/unicauca/comunicacionmicroservicios/config/RabbitConfig.java`

**Acción:** Agregar estos beans NUEVOS (sin borrar los existentes):
        "timestamp", LocalDateTime.now().toString()
    )
);

log.info("✉️ Evento publicado: anteproyecto.enviado - Proyecto: {}", proyecto.getId());
```

---

## 🏗️ Configuración Necesaria en Submission-Service

### **1. Actualizar `RabbitConfig.java`**

Agregar declaración de exchanges:

```java
@Configuration
public class RabbitConfig {

    // ========== EXCHANGE PARA EVENTOS DE DOMINIO ==========
    
    @Bean
    public DirectExchange formatoAExchange() {
        return new DirectExchange("formato-a-exchange", true, false);
    }
    
    @Bean
    public DirectExchange anteproyectoExchange() {
        return new DirectExchange("anteproyecto-exchange", true, false);
    }
    
    @Bean
    public DirectExchange proyectoExchange() {
        return new DirectExchange("proyecto-exchange", true, false);
    }
    
    // ... resto de la configuración existente
}
```

### **2. Inyectar RabbitTemplate en Controllers/Services**

```java
@RestController
@RequestMapping("/api/submissions/formatoA")
public class FormatoAController {
    
    private final FormatoAService formatoAService;
    private final RabbitTemplate rabbitTemplate; // ✅ AGREGAR
    
    public FormatoAController(
        FormatoAService formatoAService,
        RabbitTemplate rabbitTemplate  // ✅ INYECTAR
    ) {
        this.formatoAService = formatoAService;
        this.rabbitTemplate = rabbitTemplate;
    }
    
    // ... resto del código
}
```

---

## 📊 Tabla Resumen: Evento → Estado en Progress-Service

| **Acción en Submission** | **Exchange** | **Routing Key** | **Estado en Progress** |
|--------------------------|--------------|-----------------|------------------------|
| POST /formatoA (v1) | `formato-a-exchange` | `formato-a.enviado` | `EN_PRIMERA_EVALUACION_FORMATO_A` |
| POST /formatoA/reenviar (v2) | `formato-a-exchange` | `formato-a.reenviado` | `EN_SEGUNDA_EVALUACION_FORMATO_A` |
| POST /formatoA/reenviar (v3) | `formato-a-exchange` | `formato-a.reenviado` | `EN_TERCERA_EVALUACION_FORMATO_A` |
| Detectar rechazo v3 | `proyecto-exchange` | `proyecto.rechazado-definitivamente` | `FORMATO_A_RECHAZADO_DEFINITIVO` |
| POST /anteproyecto | `anteproyecto-exchange` | `anteproyecto.enviado` | `ANTEPROYECTO_ENVIADO` |

---

## 🔍 Validación del Flujo

### **Paso 1: Usuario sube Formato A v1**

```bash
POST http://localhost:8082/api/submissions/formatoA
```

**Submission-service debe:**
1. ✅ Validar datos
2. ✅ Guardar FormatoA en BD
3. ✅ **Publicar evento `formato-a.enviado` a RabbitMQ**
4. ✅ Publicar notificación a notification-service (ya existe)
5. ✅ Retornar 201 Created

### **Paso 2: Progress-service consume evento**

**Progress-service debe:**
1. ✅ Recibir evento en `ProjectEventConsumer.onFormatoAEvent()`
2. ✅ Guardar evento en `historial_eventos`
3. ✅ Actualizar `proyecto_estado`:
   - `estado_actual = "EN_PRIMERA_EVALUACION_FORMATO_A"`
   - `formato_a_version = 1`
   - `formato_a_intento_actual = 1`
   - `formato_a_estado = "EN_EVALUACION"`

### **Paso 3: Usuario consulta estado**

```bash
GET http://localhost:8085/api/progress/proyectos/123/estado
```

**Respuesta esperada:**
```json
{
  "proyectoId": 123,
  "titulo": "Sistema de IA...",
  "estadoActual": "EN_PRIMERA_EVALUACION_FORMATO_A",
  "estadoLegible": "En primera evaluación - Formato A",
  "siguientePaso": "Esperar evaluación del coordinador",
  "fase": "FORMATO_A",
  "formatoA": {
    "version": 1,
    "intentoActual": 1,
    "estado": "EN_EVALUACION"
  }
}
```

---

## ⚠️ Importante: Orden de Publicación

**SIEMPRE publicar eventos DESPUÉS de confirmar la transacción de BD:**

```java
// ❌ MAL: Publicar antes de guardar
rabbitTemplate.convertAndSend(...);
formatoARepository.save(formatoA);

// ✅ BIEN: Publicar después de guardar
FormatoA guardado = formatoARepository.save(formatoA);
rabbitTemplate.convertAndSend(...);
```

**Razón:** Si la BD falla, no queremos eventos huérfanos en RabbitMQ.

---

## 🧪 Prueba de Integración End-to-End

### **Paso 1: Levantar servicios**
```bash
# Terminal 1: RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Terminal 2: Progress Service
cd progress-tracking-service
mvn spring-boot:run

# Terminal 3: Submission Service
cd submission-service
mvn spring-boot:run
```

### **Paso 2: Enviar Formato A**
```bash
curl -X POST http://localhost:8082/api/submissions/formatoA \
  -H "Content-Type: multipart/form-data" \
  -H "X-User-Id: 100" \
  -H "X-User-Role: DOCENTE" \
  -F 'data={"titulo":"Proyecto Test","objetivoGeneral":"..."}' \
  -F 'pdf=@formato_a.pdf'
```

### **Paso 3: Verificar en RabbitMQ**
- Ir a http://localhost:15672
- Exchanges → `formato-a-exchange` → Message rates (debe mostrar actividad)
- Queues → `progress.formato-a.queue` → Messages (debe consumirse)

### **Paso 4: Consultar estado**
```bash
curl http://localhost:8085/api/progress/proyectos/1/estado
```

### **Paso 5: Verificar logs**

**Logs de submission-service:**
```
✉️ Evento publicado: formato-a.enviado - Proyecto: 1
```

**Logs de progress-service:**
```
📥 [FORMATO A] Evento recibido: formato-a.enviado - Payload: {...}
✅ Evento guardado en historial: ID=1
✅ [FORMATO A] Proyecto 1 actualizado a: EN_PRIMERA_EVALUACION_FORMATO_A
```

---

## 📝 Checklist de Implementación

Para implementar en **submission-service**:

- [ ] Actualizar `RabbitConfig.java` con exchanges de dominio
- [ ] Inyectar `RabbitTemplate` en `FormatoAController`
- [ ] Publicar `formato-a.enviado` en método `crearFormatoA()`
- [ ] Publicar `formato-a.reenviado` en método `reenviarFormatoA()`
- [ ] Inyectar `RabbitTemplate` en `AnteproyectoController`
- [ ] Publicar `anteproyecto.enviado` en método `crearAnteproyecto()`
- [ ] (Opcional) Publicar `proyecto.rechazado-definitivamente` al detectar 3 rechazos
- [ ] Agregar logs informativos con `log.info("✉️ Evento publicado: ...")`
- [ ] Probar end-to-end con progress-service

---

## 🎉 Conclusión

Una vez que **submission-service** implemente estas publicaciones de eventos:

✅ **Progress-service** (ya implementado) consumirá automáticamente  
✅ **Historial de eventos** se guardará en `historial_eventos`  
✅ **Estado actualizado** se proyectará en `proyecto_estado`  
✅ **RF5** ("ver estado del proyecto") funcionará completamente  

**La arquitectura Event-Driven CQRS estará completa! 🚀**

