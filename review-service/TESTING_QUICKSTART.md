# 🚀 GUÍA RÁPIDA DE PRUEBAS - REVIEW SERVICE

Esta guía te permite probar el Review Service en **10 minutos**.

---

## ✅ PREREQUISITOS

Antes de comenzar, asegúrate de tener:
- ✅ Docker Desktop en ejecución
- ✅ Postman instalado (o usa cURL)
- ✅ Puerto 8080 libre (Gateway)
- ✅ Las pruebas unitarias pasando (`mvn test`)

---

## 🎯 OPCIÓN 1: PRUEBA RÁPIDA (Solo Tests Unitarios)

Si solo quieres verificar que el código funciona correctamente:

```bash
cd C:\Users\DELTA\Desktop\servicios\GesTrabajoGrado-Microservicios\review-service

# Ejecutar todas las pruebas
mvn clean test

# Resultado esperado:
# Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

**✅ Si ves BUILD SUCCESS, el patrón Template Method funciona correctamente.**

---

## 🚀 OPCIÓN 2: PRUEBA COMPLETA (Con Docker)

### PASO 1: Iniciar todos los servicios

```bash
cd C:\Users\DELTA\Desktop\servicios\GesTrabajoGrado-Microservicios

# Iniciar todo el sistema
docker-compose up -d --build

# Esperar 2 minutos para que todos los servicios inicien

# Verificar que todos están corriendo
docker-compose ps
```

**Debes ver todos los servicios como "healthy":**
- ✅ gateway-service (puerto 8080)
- ✅ identity-service (puerto 8081)
- ✅ submission-service (puerto 8082)
- ✅ notification-service (puerto 8083)
- ✅ **review-service (puerto 8084)** ⭐
- ✅ postgres-review (puerto 5435)
- ✅ rabbitmq (puertos 5672, 15672)

---

### PASO 2: Verificar que Review Service está funcionando

```bash
# Test de salud
curl http://localhost:8084/actuator/health

# Respuesta esperada:
# {"status":"UP","components":{"db":{"status":"UP"},"rabbit":{"status":"UP"}}}
```

---

### PASO 3: Crear usuarios de prueba

Abre Postman y crea estos usuarios (o usa cURL):

#### 3.1 Crear COORDINADOR

```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Juan\",\"apellidos\":\"Perez\",\"rolNombre\":\"COORDINADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"
```

#### 3.2 Crear EVALUADOR 1

```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"evaluador1@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Carlos\",\"apellidos\":\"Garcia\",\"rolNombre\":\"EVALUADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"
```

#### 3.3 Crear EVALUADOR 2

```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"evaluador2@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Ana\",\"apellidos\":\"Martinez\",\"rolNombre\":\"EVALUADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"
```

---

### PASO 4: Obtener Tokens JWT

#### 4.1 Login como COORDINADOR

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\"}"
```

**Guarda el token que recibes en la respuesta:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjb29yZGluYWRvckB1bmljYXVjYS5lZHUuY28iLCJ1c2VySWQiOjUsInJvbCI6IkNPT1JESU5BRE9SIiwiaWF0IjoxNzMwMDA3MjAwLCJleHAiOjE3MzAwMTA4MDB9.abcd1234...",
    "email": "coordinador@unicauca.edu.co",
    "rol": "COORDINADOR"
  }
}
```

#### 4.2 Login como EVALUADOR 1 y EVALUADOR 2

Repite el mismo proceso para obtener sus tokens.

---

### PASO 5: 🧪 PROBAR EVALUACIÓN DE FORMATO A

Este es el primer patrón del Template Method (evaluación simple por el coordinador).

#### 5.1 Listar Formatos A pendientes

```bash
curl -X GET "http://localhost:8080/api/review/formatoA/pendientes?page=0&size=10" ^
  -H "Authorization: Bearer TU_TOKEN_COORDINADOR"
```

#### 5.2 Evaluar un Formato A

```bash
curl -X POST http://localhost:8080/api/review/formatoA/1/evaluar ^
  -H "Authorization: Bearer TU_TOKEN_COORDINADOR" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Cumple con todos los requisitos\"}"
```

**✅ Verificación exitosa:**
```json
{
  "success": true,
  "message": "Formato A evaluado exitosamente",
  "data": {
    "evaluationId": 1,
    "documentId": 1,
    "documentType": "FORMATO_A",
    "decision": "APROBADO",
    "notificacionEnviada": true
  }
}
```

#### 5.3 Verificar en los logs

```bash
docker logs review-service | findstr "FORMATO_A"
```

**Deberías ver:**
- ✅ "Iniciando evaluación - Documento: 1, Tipo: FORMATO_A"
- ✅ "Permisos validados correctamente para rol: COORDINADOR"
- ✅ "Estado actualizado exitosamente en Submission Service"
- ✅ "✓ Evento FORMATO_A_EVALUATED publicado en RabbitMQ"

---

### PASO 6: 🧪 PROBAR EVALUACIÓN DE ANTEPROYECTO (2 EVALUADORES)

Este es el segundo patrón del Template Method (dos evaluadores deben aprobar).

#### 6.1 Verificar que el anteproyecto existe

```bash
curl -X GET "http://localhost:8080/api/submissions/anteproyectos?page=0&size=10" ^
  -H "Authorization: Bearer TU_TOKEN_COORDINADOR"
```

#### 6.2 Primera evaluación (EVALUADOR 1)

```bash
curl -X POST http://localhost:8080/api/review/anteproyectos/1/evaluar ^
  -H "Authorization: Bearer TU_TOKEN_EVALUADOR1" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Metodologia solida\"}"
```

**✅ Respuesta esperada:**
```json
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 2,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "notificacionEnviada": false  ⭐ IMPORTANTE: false porque falta el segundo evaluador
  }
}
```

#### 6.3 Verificar en logs (NO debe notificar aún)

```bash
docker logs review-service | findstr "segundo evaluador"
```

**Deberías ver:**
- ✅ "Evaluación registrada para Evaluador 1: decisión=APROBADO"
- ✅ "⏳ Esperando evaluación del segundo evaluador"
- ❌ NO debe haber publicado evento en RabbitMQ

#### 6.4 Segunda evaluación (EVALUADOR 2)

```bash
curl -X POST http://localhost:8080/api/review/anteproyectos/1/evaluar ^
  -H "Authorization: Bearer TU_TOKEN_EVALUADOR2" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Excelente propuesta\"}"
```

**✅ Respuesta esperada:**
```json
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 3,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "notificacionEnviada": true  ⭐ IMPORTANTE: true porque ambos evaluaron
  }
}
```

#### 6.5 Verificar en logs (AHORA SÍ debe notificar)

```bash
docker logs review-service | findstr "ambos evaluadores"
```

**Deberías ver:**
- ✅ "Evaluación registrada para Evaluador 2: decisión=APROBADO"
- ✅ "✓ Estado final actualizado en Submission Service: anteproyectoId=1, decisión=APROBADO"
- ✅ "Publicando evento de notificación para Anteproyecto 1 (ambos evaluadores completaron)"
- ✅ "✓ Evento ANTEPROYECTO_EVALUATED publicado en RabbitMQ"

---

### PASO 7: 🔍 VERIFICAR NOTIFICACIONES EN RABBITMQ

#### 7.1 Abrir la consola de RabbitMQ

1. Ir a: http://localhost:15672
2. Login: **admin** / **admin123**
3. Click en la pestaña "Queues"
4. Buscar: `evaluation.notifications.queue`

#### 7.2 Verificar mensajes

Deberías ver 2 mensajes en la cola:
- 1 mensaje de FORMATO_A_EVALUATED
- 1 mensaje de ANTEPROYECTO_EVALUATED

#### 7.3 Ver los logs del Notification Service

```bash
docker logs notification-service | findstr "EVALUATION NOTIFICATION"
```

**Deberías ver:**
```
📧 [EVALUATION NOTIFICATION MOCK] Received evaluation event:
Event Type: FORMATO_A_EVALUATED
Decision: APROBADO
---
📧 [EVALUATION NOTIFICATION MOCK] Received evaluation event:
Event Type: ANTEPROYECTO_EVALUATED
Decision: APROBADO
```

---

### PASO 8: 🗄️ VERIFICAR BASE DE DATOS

```bash
# Conectarse a la base de datos del Review Service
docker exec -it postgres-review psql -U review_user -d review_db

# Ver todas las evaluaciones
SELECT id, document_type, document_id, decision, observaciones, evaluator_role 
FROM evaluaciones 
ORDER BY id;

# Ver asignaciones de evaluadores
SELECT * FROM asignaciones_evaluadores;

# Salir
\q
```

**Deberías ver:**
- ✅ 1 evaluación de FORMATO_A con decision=APROBADO
- ✅ 2 evaluaciones de ANTEPROYECTO (una por cada evaluador)
- ✅ 1 asignación con estado=COMPLETADA

---

## 🧪 PASO 9: PROBAR ESCENARIOS DE ERROR

### 9.1 Intentar evaluar sin permisos

```bash
# Evaluador intenta evaluar Formato A (solo COORDINADOR puede)
curl -X POST http://localhost:8080/api/review/formatoA/1/evaluar ^
  -H "Authorization: Bearer TU_TOKEN_EVALUADOR1" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Test\"}"

# Respuesta esperada (403 Forbidden):
# {"success":false,"message":"Solo coordinadores pueden evaluar Formato A"}
```

### 9.2 Intentar evaluar dos veces

```bash
# EVALUADOR1 intenta evaluar de nuevo el mismo anteproyecto
curl -X POST http://localhost:8080/api/review/anteproyectos/1/evaluar ^
  -H "Authorization: Bearer TU_TOKEN_EVALUADOR1" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Test\"}"

# Respuesta esperada (400 Bad Request):
# {"success":false,"message":"Este evaluador ya registró su evaluación"}
```

---

## ✅ CHECKLIST DE VERIFICACIÓN COMPLETA

Marca cada punto cuando lo hayas verificado:

### Tests Unitarios
- [ ] `mvn test` pasa exitosamente (13 tests)
- [ ] No hay errores de compilación

### Infraestructura
- [ ] Docker containers están running y healthy
- [ ] Review Service responde en puerto 8084
- [ ] Base de datos PostgreSQL está accesible
- [ ] RabbitMQ consola web accesible (puerto 15672)

### Evaluación de Formato A (Template Method - Patrón 1)
- [ ] Coordinador puede evaluar Formato A
- [ ] Se guarda la evaluación en la BD
- [ ] Se actualiza el estado en Submission Service
- [ ] Se publica evento en RabbitMQ inmediatamente
- [ ] Notification Service recibe el evento

### Evaluación de Anteproyecto (Template Method - Patrón 2)
- [ ] EVALUADOR1 puede evaluar anteproyecto
- [ ] Primera evaluación NO dispara notificación
- [ ] EVALUADOR2 puede evaluar el mismo anteproyecto
- [ ] Segunda evaluación SÍ dispara notificación
- [ ] Decisión final se calcula correctamente (RECHAZADO si al menos uno rechaza)
- [ ] Asignación cambia a estado COMPLETADA

### Validaciones y Seguridad
- [ ] EVALUADOR no puede evaluar Formato A (403)
- [ ] No se puede evaluar dos veces el mismo documento (400)
- [ ] Solo usuarios autenticados pueden acceder (401 sin token)
- [ ] Los roles se validan correctamente

---

## 🎉 RESULTADO ESPERADO

Si completaste todos los pasos, deberías haber verificado:

✅ **Patrón Template Method funciona correctamente** con dos variantes:
   - Formato A: Evaluación simple por coordinador
   - Anteproyecto: Evaluación con 2 evaluadores

✅ **Integración con otros servicios**:
   - Identity Service para autenticación
   - Submission Service para actualizar estados
   - Notification Service para enviar notificaciones

✅ **Validaciones y seguridad**:
   - Control de roles y permisos
   - Validación de estados
   - Prevención de evaluaciones duplicadas

---

## 🛠️ TROUBLESHOOTING

### Si algo falla:

1. **Ver logs del Review Service:**
   ```bash
   docker logs review-service --tail 100
   ```

2. **Reiniciar solo el Review Service:**
   ```bash
   docker-compose restart review
   ```

3. **Verificar conectividad con otros servicios:**
   ```bash
   docker-compose logs gateway | findstr review
   ```

4. **Limpiar y reiniciar todo:**
   ```bash
   docker-compose down -v
   docker-compose up -d --build
   ```

---

## 📚 DOCUMENTACIÓN ADICIONAL

- **Guía completa de pruebas**: Ver `GUIA_PRUEBAS.md`
- **Implementación completa**: Ver `IMPLEMENTACION_COMPLETA.md`
- **Colección de Postman**: Ver `Review-Service.postman_collection.json`

---

**¡Listo! Tu Review Service está funcionando correctamente con el patrón Template Method.** 🚀

