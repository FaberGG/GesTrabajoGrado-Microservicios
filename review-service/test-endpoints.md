# 🧪 Script de Pruebas - Review Service

## ✅ Verificar que el servicio esté funcionando

### 1. Verificar estado del servicio
```powershell
# Ver logs del servicio
docker logs review-service --tail 50

# Ver estado del contenedor
docker ps | findstr review

# Verificar health check (debe mostrar "healthy")
docker inspect review-service --format='{{.State.Health.Status}}'

# Probar endpoint de salud
curl http://localhost:8084/actuator/health
```

## 🎯 Pruebas de Endpoints

### Prerequisitos
1. **Submission Service** debe estar corriendo en puerto 8082
2. **RabbitMQ** debe estar corriendo
3. **PostgreSQL** debe estar corriendo (puerto 5435)

---

## 📝 Test 1: Listar Formato A Pendientes (Coordinador)

```powershell
# PowerShell
curl http://localhost:8084/api/review/formatoA/pendientes?page=0&size=10 `
  -H "X-User-Role: COORDINADOR" `
  -H "Content-Type: application/json" | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": X,
    "pageNumber": 0,
    "pageSize": 10
  }
}
```

---

## 📝 Test 2: Evaluar Formato A (Coordinador)

**IMPORTANTE:** Reemplaza `{id}` con un ID real de Formato A pendiente

```powershell
# PowerShell
$body = @{
    decision = "APROBADO"
    observaciones = "El formato cumple con todos los requisitos establecidos"
} | ConvertTo-Json

curl http://localhost:8084/api/review/formatoA/1/evaluar `
  -Method POST `
  -H "X-User-Id: 5" `
  -H "X-User-Role: COORDINADOR" `
  -H "Content-Type: application/json" `
  -Body $body | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Formato A evaluado exitosamente",
  "data": {
    "evaluationId": 1,
    "documentId": 1,
    "documentType": "FORMATO_A",
    "decision": "APROBADO",
    "observaciones": "El formato cumple con todos los requisitos establecidos",
    "fechaEvaluacion": "2025-12-03T03:40:00",
    "notificacionEnviada": true
  }
}
```

---

## 📝 Test 3: Asignar Evaluadores a Anteproyecto (Jefe Departamento)

**IMPORTANTE:** Reemplaza los IDs con valores reales

```powershell
# PowerShell
$body = @{
    anteproyectoId = 1
    evaluador1Id = 15
    evaluador2Id = 20
} | ConvertTo-Json

curl http://localhost:8084/api/review/anteproyectos/asignar `
  -Method POST `
  -H "X-User-Id: 10" `
  -H "X-User-Role: JEFE_DEPARTAMENTO" `
  -H "Content-Type: application/json" `
  -Body $body | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Evaluadores asignados exitosamente",
  "data": {
    "asignacionId": 1,
    "anteproyectoId": 1,
    "tituloAnteproyecto": "...",
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
    "fechaAsignacion": "2025-12-03T03:45:00",
    "fechaCompletado": null,
    "finalDecision": null
  }
}
```

---

## 📝 Test 4: Listar Asignaciones (Jefe Departamento)

```powershell
# PowerShell - Ver todas las asignaciones
curl http://localhost:8084/api/review/anteproyectos/asignaciones?page=0&size=10 `
  -H "X-User-Id: 10" `
  -H "X-User-Role: JEFE_DEPARTAMENTO" `
  -H "Content-Type: application/json" | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "asignacionId": 1,
        "anteproyectoId": 1,
        "tituloAnteproyecto": "...",
        "evaluador1": {...},
        "evaluador2": {...},
        "estado": "PENDIENTE",
        "fechaAsignacion": "...",
        "fechaCompletado": null,
        "finalDecision": null
      }
    ],
    "totalElements": 1,
    "pageNumber": 0,
    "pageSize": 10
  }
}
```

---

## 📝 Test 5: Listar Asignaciones (Evaluador - Solo las suyas)

```powershell
# PowerShell - Ver solo asignaciones del evaluador
curl http://localhost:8084/api/review/anteproyectos/asignaciones?page=0&size=10 `
  -H "X-User-Id: 15" `
  -H "X-User-Role: EVALUADOR" `
  -H "Content-Type: application/json" | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

---

## 📝 Test 6: Primera Evaluación de Anteproyecto (Evaluador 1)

**IMPORTANTE:** Este es el test más crítico - PRIMERA evaluación

```powershell
# PowerShell
$body = @{
    decision = "APROBADO"
    observaciones = "El anteproyecto presenta una metodología sólida y objetivos claros"
} | ConvertTo-Json

curl http://localhost:8084/api/review/anteproyectos/1/evaluar `
  -Method POST `
  -H "X-User-Id: 15" `
  -H "X-User-Role: EVALUADOR" `
  -H "Content-Type: application/json" `
  -Body $body | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Respuesta Esperada (PRIMERA evaluación):**
```json
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 2,
    "documentId": 1,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "El anteproyecto presenta una metodología sólida y objetivos claros",
    "fechaEvaluacion": "2025-12-03T03:50:00",
    "notificacionEnviada": false  // ⚠️ FALSE porque falta el segundo evaluador
  }
}
```

---

## 📝 Test 7: Segunda Evaluación de Anteproyecto (Evaluador 2) ⚠️ CRÍTICO

**IMPORTANTE:** Este test debe ejecutarse DESPUÉS del Test 6

```powershell
# PowerShell
$body = @{
    decision = "APROBADO"
    observaciones = "Coincido con la evaluación previa. El marco teórico está bien fundamentado"
} | ConvertTo-Json

curl http://localhost:8084/api/review/anteproyectos/1/evaluar `
  -Method POST `
  -H "X-User-Id: 20" `
  -H "X-User-Role: EVALUADOR" `
  -H "Content-Type: application/json" `
  -Body $body | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Respuesta Esperada (SEGUNDA evaluación - COMPLETA):**
```json
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 3,
    "documentId": 1,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "Coincido con la evaluación previa. El marco teórico está bien fundamentado",
    "fechaEvaluacion": "2025-12-03T03:55:00",
    "notificacionEnviada": true  // ✅ TRUE porque AMBOS evaluadores completaron
  }
}
```

**✅ VERIFICAR:**
1. `notificacionEnviada: true` en la segunda evaluación
2. Se debe haber actualizado el estado en Submission Service
3. Se debe haber publicado un evento en RabbitMQ
4. La decisión final es APROBADO (ambos aprobaron)

---

## 🔍 Verificaciones Adicionales

### Verificar tablas en PostgreSQL

```powershell
# Conectarse a PostgreSQL
docker exec -it postgres-review psql -U review_user -d review_db

# Ver evaluaciones
SELECT * FROM evaluaciones ORDER BY created_at DESC;

# Ver asignaciones
SELECT * FROM asignaciones_evaluadores ORDER BY created_at DESC;

# Salir
\q
```

### Verificar eventos en RabbitMQ

1. Abrir RabbitMQ Management: http://localhost:15672
2. Usuario: `admin` / Contraseña: `admin123`
3. Ir a **Queues** → `evaluation.notifications.queue`
4. Verificar que haya mensajes publicados
5. Click en la queue → **Get messages** para ver el contenido

---

## ⚠️ Casos de Error a Probar

### 1. Rol no autorizado
```powershell
# Intentar evaluar Formato A con rol ESTUDIANTE (debe fallar)
curl http://localhost:8084/api/review/formatoA/1/evaluar `
  -Method POST `
  -H "X-User-Id: 100" `
  -H "X-User-Role: ESTUDIANTE" `
  -H "Content-Type: application/json" `
  -Body '{"decision":"APROBADO","observaciones":"Test"}'
```
**Respuesta Esperada:** HTTP 403 Forbidden

### 2. Evaluador no asignado
```powershell
# Intentar evaluar con un evaluador que NO fue asignado (debe fallar)
curl http://localhost:8084/api/review/anteproyectos/1/evaluar `
  -Method POST `
  -H "X-User-Id: 999" `
  -H "X-User-Role: EVALUADOR" `
  -H "Content-Type: application/json" `
  -Body '{"decision":"APROBADO","observaciones":"Test"}'
```
**Respuesta Esperada:** HTTP 400 Bad Request - "El evaluador actual no está asignado a este anteproyecto"

### 3. Evaluación duplicada
```powershell
# Intentar que el mismo evaluador evalúe dos veces (debe fallar)
# Ejecutar el Test 6 dos veces con el mismo evaluador
```
**Respuesta Esperada:** HTTP 400 Bad Request - "Este evaluador ya registró su evaluación para este anteproyecto"

### 4. Evaluadores iguales en asignación
```powershell
$body = @{
    anteproyectoId = 2
    evaluador1Id = 15
    evaluador2Id = 15  # ⚠️ Mismo evaluador (debe fallar)
} | ConvertTo-Json

curl http://localhost:8084/api/review/anteproyectos/asignar `
  -Method POST `
  -H "X-User-Id: 10" `
  -H "X-User-Role: JEFE_DEPARTAMENTO" `
  -H "Content-Type: application/json" `
  -Body $body
```
**Respuesta Esperada:** HTTP 400 Bad Request - "Los evaluadores deben ser diferentes"

---

## 📊 Resumen de Validaciones

✅ **Test 1**: Listar Formato A pendientes (Coordinador)
✅ **Test 2**: Evaluar Formato A → `notificacionEnviada: true`
✅ **Test 3**: Asignar 2 evaluadores diferentes
✅ **Test 4**: Listar todas las asignaciones (Jefe)
✅ **Test 5**: Listar solo mis asignaciones (Evaluador)
✅ **Test 6**: Primera evaluación → `notificacionEnviada: false` ⚠️
✅ **Test 7**: Segunda evaluación → `notificacionEnviada: true` ✅
✅ **Errores**: Validar roles, permisos y estados

---

## 🎉 Implementación Completa

Si todos los tests pasan, el **Review Service está 100% funcional** con:

1. ✅ Evaluación de Formato A (1 evaluador - Coordinador)
2. ✅ Asignación de evaluadores a Anteproyectos
3. ✅ Evaluación de Anteproyectos (2 evaluadores con lógica compleja)
4. ✅ Notificaciones RabbitMQ solo cuando corresponde
5. ✅ Actualización de Submission Service en el momento correcto
6. ✅ Validaciones de roles y permisos
7. ✅ Manejo de errores robusto

