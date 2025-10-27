# 🧪 GUÍA DE PRUEBAS - REVIEW SERVICE

Esta guía te permitirá probar completamente el Review Service y verificar que el patrón Template Method funciona correctamente.

---

## 🚀 PASO 1: Compilar y Desplegar

### Opción A: Compilación y Verificación Inicial (SIN Docker)

```bash
# 1. Ir al directorio del Review Service
cd C:\Users\DELTA\Desktop\servicios\GesTrabajoGrado-Microservicios\review-service

# 2. Compilar el proyecto
mvn clean compile

# 3. Ejecutar tests unitarios
mvn test

# 4. Empaquetar (crea el JAR)
mvn clean package

# 5. Verificar que el JAR se creó
dir target\review-service-1.0.0.jar
```

**✅ Si todo compila sin errores, el código está correcto.**

### Opción B: Desplegar con Docker Compose (RECOMENDADO)

```bash
# 1. Ir al directorio raíz
cd C:\Users\DELTA\Desktop\servicios\GesTrabajoGrado-Microservicios

# 2. Construir e iniciar todos los servicios
docker-compose up -d --build

# 3. Ver el estado de los contenedores
docker-compose ps

# Deberías ver:
# - gateway-service (8080) - healthy
# - identity-service (8081) - healthy
# - submission-service (8082) - healthy
# - notification-service (8083) - healthy
# - review-service (8084) - healthy ⭐
# - postgres-review (5435) - healthy ⭐
# - rabbitmq (5672, 15672) - healthy

# 4. Ver logs del Review Service
docker logs -f review-service
```

**Logs esperados al iniciar:**
```
2025-10-26 ... - Starting ReviewServiceApplication
2025-10-26 ... - Started ReviewServiceApplication in X seconds
```

---

## 🔍 PASO 2: Verificar Health Checks

### Test 1: Health del Review Service

```bash
# Verificar que el servicio está vivo
curl http://localhost:8084/actuator/health

# Respuesta esperada:
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "rabbit": { "status": "UP" }
  }
}
```

### Test 2: Verificar Base de Datos PostgreSQL

```bash
# Conectarse a PostgreSQL del Review Service
docker exec -it postgres-review psql -U review_user -d review_db

# Verificar tablas creadas por Hibernate
\dt

# Deberías ver:
# - evaluaciones
# - asignaciones_evaluadores

# Salir
\q
```

### Test 3: Verificar RabbitMQ

```
1. Abrir navegador: http://localhost:15672
2. Login: admin / admin123
3. Ir a "Queues"
4. Buscar: evaluation.notifications.queue
5. Verificar que existe
```

---

## 🔐 PASO 3: Obtener Token JWT

Antes de probar los endpoints protegidos, necesitas autenticarte:

```bash
# 1. Registrar un usuario COORDINADOR (solo si no existe)
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Juan\",\"apellidos\":\"Pérez\",\"rolNombre\":\"COORDINADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"

# 2. Login para obtener JWT
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\"}"

# Respuesta:
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "coordinador@unicauca.edu.co",
    "rol": "COORDINADOR"
  }
}

# ⭐ GUARDAR EL TOKEN - lo usarás en todas las pruebas
```

**Usuarios de prueba para crear:**

```bash
# COORDINADOR
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Juan\",\"apellidos\":\"Pérez\",\"rolNombre\":\"COORDINADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"

# JEFE_DEPARTAMENTO
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"jefe@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"María\",\"apellidos\":\"López\",\"rolNombre\":\"JEFE_DEPARTAMENTO\",\"programa\":\"INGENIERIA_SISTEMAS\"}"

# EVALUADOR 1
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"evaluador1@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Carlos\",\"apellidos\":\"García\",\"rolNombre\":\"EVALUADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"

# EVALUADOR 2
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"evaluador2@unicauca.edu.co\",\"password\":\"123456\",\"nombres\":\"Ana\",\"apellidos\":\"Martínez\",\"rolNombre\":\"EVALUADOR\",\"programa\":\"INGENIERIA_SISTEMAS\"}"
```

---

## 🧪 PASO 4: Probar Evaluación de Formato A (Template Method)

### Test 4.1: Listar Formatos A Pendientes

```bash
# Reemplaza YOUR_TOKEN con el token obtenido en el paso anterior
curl -X GET "http://localhost:8080/api/review/formatoA/pendientes?page=0&size=10" ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json"

# Respuesta esperada:
{
  "success": true,
  "message": null,
  "data": {
    "content": [
      {
        "formatoAId": 1,
        "titulo": "Implementación de IA en agricultura",
        "docenteDirectorNombre": "Dr. Juan Pérez",
        "docenteDirectorEmail": "juan.perez@unicauca.edu.co",
        "estudiantesEmails": ["estudiante1@unicauca.edu.co"],
        "fechaCarga": "2025-10-24T10:30:00",
        "estado": "EN_REVISION"
      }
    ],
    "totalElements": 1
  },
  "errors": null
}
```

### Test 4.2: Evaluar un Formato A (APROBADO)

```bash
# Login como COORDINADOR primero y obtén el token
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\"}"

# Evaluar Formato A con ID 1
curl -X POST http://localhost:8080/api/review/formatoA/1/evaluar ^
  -H "Authorization: Bearer YOUR_COORDINADOR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"El formato cumple con todos los requisitos académicos\"}"

# Respuesta esperada:
{
  "success": true,
  "message": "Formato A evaluado exitosamente",
  "data": {
    "evaluationId": 1,
    "documentId": 1,
    "documentType": "FORMATO_A",
    "decision": "APROBADO",
    "observaciones": "El formato cumple con todos los requisitos académicos",
    "fechaEvaluacion": "2025-10-26T...",
    "notificacionEnviada": true
  },
  "errors": null
}
```

### Test 4.3: Evaluar un Formato A (RECHAZADO)

```bash
curl -X POST http://localhost:8080/api/review/formatoA/2/evaluar ^
  -H "Authorization: Bearer YOUR_COORDINADOR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"RECHAZADO\",\"observaciones\":\"Falta información en la sección de objetivos\"}"
```

### ✅ Verificaciones del Formato A:

1. **Verificar en Base de Datos:**
```bash
docker exec -it postgres-review psql -U review_user -d review_db

SELECT * FROM evaluaciones WHERE document_type = 'FORMATO_A';
```

2. **Verificar Logs del Review Service:**
```bash
docker logs review-service | grep "FORMATO_A"

# Deberías ver:
# - "Iniciando evaluación - Documento: 1, Tipo: FORMATO_A"
# - "Permisos validados correctamente para rol: COORDINADOR"
# - "Estado actualizado en Submission Service"
# - "✓ Evento FORMATO_A_EVALUATED publicado en RabbitMQ"
```

3. **Verificar Notification Service recibió el evento:**
```bash
docker logs notification-service | grep "FORMATO_A"

# Deberías ver:
# - "📧 [EVALUATION NOTIFICATION MOCK]"
# - "Event: FORMATO_A_EVALUATED"
# - "Decision: APROBADO"
```

---

## 🧪 PASO 5: Probar Evaluación de Anteproyecto (2 Evaluadores)

### Test 5.1: Asignar Evaluadores (JEFE_DEPARTAMENTO)

```bash
# Login como JEFE_DEPARTAMENTO
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"jefe@unicauca.edu.co\",\"password\":\"123456\"}"

# Asignar evaluadores al anteproyecto ID 1
curl -X POST http://localhost:8080/api/review/anteproyectos/asignar ^
  -H "Authorization: Bearer YOUR_JEFE_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"anteproyectoId\":1,\"evaluador1Id\":15,\"evaluador2Id\":20}"

# Respuesta esperada:
{
  "success": true,
  "message": "Evaluadores asignados exitosamente",
  "data": {
    "asignacionId": 1,
    "anteproyectoId": 1,
    "tituloAnteproyecto": "Sistema de recomendación basado en ML",
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
    "fechaAsignacion": "2025-10-26T...",
    "finalDecision": null
  }
}
```

### Test 5.2: Primera Evaluación (EVALUADOR 1)

```bash
# Login como EVALUADOR 1
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"evaluador1@unicauca.edu.co\",\"password\":\"123456\"}"

# Listar asignaciones del evaluador
curl -X GET http://localhost:8080/api/review/anteproyectos/asignaciones ^
  -H "Authorization: Bearer YOUR_EVALUADOR1_TOKEN"

# Evaluar como APROBADO
curl -X POST http://localhost:8080/api/review/anteproyectos/1/evaluar ^
  -H "Authorization: Bearer YOUR_EVALUADOR1_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Metodología de investigación sólida y bien fundamentada\"}"

# Respuesta esperada:
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 2,
    "documentId": 1,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "Metodología de investigación sólida...",
    "fechaEvaluacion": "2025-10-26T...",
    "notificacionEnviada": false  ⭐ IMPORTANTE: false porque falta evaluador 2
  }
}
```

### Test 5.3: Segunda Evaluación (EVALUADOR 2) - Escenario APROBADO

```bash
# Login como EVALUADOR 2
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"evaluador2@unicauca.edu.co\",\"password\":\"123456\"}"

# Evaluar como APROBADO (ambos aprueban → decisión final APROBADO)
curl -X POST http://localhost:8080/api/review/anteproyectos/1/evaluar ^
  -H "Authorization: Bearer YOUR_EVALUADOR2_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Excelente propuesta con objetivos claros\"}"

# Respuesta esperada:
{
  "success": true,
  "message": "Evaluación registrada exitosamente",
  "data": {
    "evaluationId": 3,
    "documentId": 1,
    "documentType": "ANTEPROYECTO",
    "decision": "APROBADO",
    "observaciones": "Excelente propuesta...",
    "fechaEvaluacion": "2025-10-26T...",
    "notificacionEnviada": true  ⭐ IMPORTANTE: true porque ambos evaluaron
  }
}
```

### Test 5.4: Escenario RECHAZADO (al menos uno rechaza)

```bash
# Asignar nuevo anteproyecto
curl -X POST http://localhost:8080/api/review/anteproyectos/asignar ^
  -H "Authorization: Bearer YOUR_JEFE_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"anteproyectoId\":2,\"evaluador1Id\":15,\"evaluador2Id\":20}"

# Evaluador 1 APRUEBA
curl -X POST http://localhost:8080/api/review/anteproyectos/2/evaluar ^
  -H "Authorization: Bearer YOUR_EVALUADOR1_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Bien\"}"

# Evaluador 2 RECHAZA → Decisión final = RECHAZADO
curl -X POST http://localhost:8080/api/review/anteproyectos/2/evaluar ^
  -H "Authorization: Bearer YOUR_EVALUADOR2_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"RECHAZADO\",\"observaciones\":\"Faltan referencias bibliográficas\"}"

# La decisión final será RECHAZADO porque al menos uno rechazó
```

### ✅ Verificaciones del Anteproyecto:

1. **Verificar en Base de Datos:**
```bash
docker exec -it postgres-review psql -U review_user -d review_db

-- Ver asignaciones
SELECT * FROM asignaciones_evaluadores;

-- Ver evaluaciones de anteproyectos
SELECT * FROM evaluaciones WHERE document_type = 'ANTEPROYECTO';
```

2. **Verificar Logs (Primera evaluación - NO notifica):**
```bash
docker logs review-service | grep "Evaluador 1"

# Deberías ver:
# - "Evaluación registrada para Evaluador 1"
# - "⏳ Esperando evaluación del segundo evaluador"
# - NO debería publicar evento RabbitMQ aún
```

3. **Verificar Logs (Segunda evaluación - SÍ notifica):**
```bash
docker logs review-service | grep "ambos evaluadores"

# Deberías ver:
# - "Evaluación registrada para Evaluador 2"
# - "✓ Estado final actualizado en Submission Service"
# - "✓ Evento ANTEPROYECTO_EVALUATED publicado en RabbitMQ"
```

4. **Verificar Notification Service:**
```bash
docker logs notification-service | grep "ANTEPROYECTO"

# Deberías ver:
# - "📧 [EVALUATION NOTIFICATION MOCK]"
# - "Event: ANTEPROYECTO_EVALUATED"
# - "Decision: APROBADO" (o RECHAZADO según el caso)
```

---

## 🧪 PASO 6: Probar Validaciones y Errores

### Test 6.1: Rol Incorrecto (debe fallar)

```bash
# Intentar evaluar Formato A con rol ESTUDIANTE (debería fallar)
curl -X POST http://localhost:8080/api/review/formatoA/1/evaluar ^
  -H "Authorization: Bearer TOKEN_DE_ESTUDIANTE" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Test\"}"

# Respuesta esperada (403 Forbidden):
{
  "success": false,
  "message": "Solo coordinadores pueden evaluar Formato A",
  "data": null,
  "errors": null
}
```

### Test 6.2: Evaluador Ya Evaluó (debe fallar)

```bash
# Evaluador 1 intenta evaluar dos veces el mismo anteproyecto
curl -X POST http://localhost:8080/api/review/anteproyectos/1/evaluar ^
  -H "Authorization: Bearer YOUR_EVALUADOR1_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Test\"}"

# Respuesta esperada (400 Bad Request):
{
  "success": false,
  "message": "Este evaluador ya registró su evaluación para este anteproyecto",
  "data": null,
  "errors": null
}
```

### Test 6.3: Anteproyecto Sin Asignación (debe fallar)

```bash
# Intentar evaluar anteproyecto que no tiene evaluadores asignados
curl -X POST http://localhost:8080/api/review/anteproyectos/999/evaluar ^
  -H "Authorization: Bearer YOUR_EVALUADOR1_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APROBADO\",\"observaciones\":\"Test\"}"

# Respuesta esperada (400 Bad Request):
{
  "success": false,
  "message": "Anteproyecto 999 no tiene evaluadores asignados",
  "data": null
}
```

---

## 📊 PASO 7: Verificar Template Method en Acción

### Verificar logs completos del flujo Template Method:

```bash
# Ver logs detallados del Review Service
docker logs review-service --tail=100

# Deberías ver el flujo completo:
# 1. "Iniciando evaluación - Documento: X, Tipo: FORMATO_A"
# 2. "Permisos validados correctamente para rol: COORDINADOR"
# 3. "Obteniendo información de Formato A con id: X"
# 4. "Estado del documento validado correctamente: EN_REVISION"
# 5. "Evaluación guardada - ID: Y, Documento: X, Decisión: APROBADO"
# 6. "Actualizando estado de Formato A X en Submission Service"
# 7. "✓ Estado actualizado exitosamente en Submission Service"
# 8. "Publicando evento de notificación para Formato A X"
# 9. "✓ Evento FORMATO_A_EVALUATED publicado en RabbitMQ"
# 10. "Evaluación completada exitosamente - ID: Y"
```

---

## 🎯 PASO 8: Tests Unitarios (Validación de Código)

```bash
# Ejecutar todos los tests
cd C:\Users\DELTA\Desktop\servicios\GesTrabajoGrado-Microservicios\review-service
mvn test

# Deberías ver:
# [INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS

# Tests específicos:
mvn test -Dtest=EvaluationTemplateTest
mvn test -Dtest=FormatoAEvaluationServiceTest
mvn test -Dtest=AnteproyectoEvaluationServiceTest
```

---

## 📋 CHECKLIST FINAL DE VALIDACIÓN

### ✅ Compilación y Despliegue
- [ ] `mvn clean compile` - Sin errores
- [ ] `mvn test` - Todos los tests pasan
- [ ] `docker-compose up -d` - Todos los contenedores healthy
- [ ] `curl http://localhost:8084/actuator/health` - UP

### ✅ Base de Datos
- [ ] PostgreSQL review_db en puerto 5435 accesible
- [ ] Tablas `evaluaciones` y `asignaciones_evaluadores` creadas

### ✅ RabbitMQ
- [ ] Queue `evaluation.notifications.queue` existe
- [ ] Exchange `evaluation.exchange` existe

### ✅ Formato A (Template Method)
- [ ] GET /formatoA/pendientes funciona (COORDINADOR)
- [ ] POST /formatoA/{id}/evaluar funciona (COORDINADOR)
- [ ] Evaluación guarda en BD
- [ ] Actualiza Submission Service
- [ ] Publica evento en RabbitMQ inmediatamente
- [ ] Notification Service recibe evento

### ✅ Anteproyecto (Template Method con 2 Evaluadores)
- [ ] POST /anteproyectos/asignar funciona (JEFE_DEPARTAMENTO)
- [ ] GET /anteproyectos/asignaciones funciona (EVALUADOR)
- [ ] Primera evaluación NO publica evento
- [ ] Segunda evaluación SÍ publica evento
- [ ] Decisión final correcta (ambos APROBADO → APROBADO, al menos uno RECHAZADO → RECHAZADO)
- [ ] Estado cambia a COMPLETADA

### ✅ Validaciones
- [ ] Rol incorrecto retorna 403
- [ ] Evaluador duplicado retorna 400
- [ ] Sin asignación retorna 400

### ✅ Logs y Monitoreo
- [ ] Logs muestran flujo completo del Template Method
- [ ] Logs muestran "✓" para eventos exitosos
- [ ] Logs muestran "⏳" cuando falta evaluador

---

## 🐛 Troubleshooting

### Problema: "Connection refused" al hacer curl

```bash
# Verificar que el servicio está corriendo
docker ps | grep review

# Verificar logs
docker logs review-service

# Reiniciar el servicio
docker-compose restart review
```

### Problema: "Unauthorized" en endpoints protegidos

```bash
# Verificar que el token es válido
# El token debe ser reciente (expira en 24 horas)
# Hacer login nuevamente

curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"coordinador@unicauca.edu.co\",\"password\":\"123456\"}"
```

### Problema: RabbitMQ no recibe eventos

```bash
# Verificar conexión RabbitMQ
docker logs review-service | grep "rabbit"

# Verificar que la queue existe
# Ir a http://localhost:15672 → Queues
# Buscar: evaluation.notifications.queue
```

### Problema: Base de datos no accesible

```bash
# Verificar PostgreSQL
docker logs postgres-review

# Reiniciar BD
docker-compose restart postgres-review

# Verificar conexión
docker exec -it postgres-review psql -U review_user -d review_db -c "SELECT 1;"
```

---

## 🎉 CONCLUSIÓN

Si todos los tests anteriores pasan, tu Review Service está **100% funcional** y el patrón Template Method está correctamente implementado.

**Flujos validados:**
✅ Formato A con notificación inmediata
✅ Anteproyecto con 2 evaluadores y notificación diferida
✅ Validaciones de roles y permisos
✅ Comunicación HTTP con Submission Service
✅ Mensajería asíncrona con RabbitMQ
✅ Persistencia en PostgreSQL

---

**Happy Testing! 🚀**

