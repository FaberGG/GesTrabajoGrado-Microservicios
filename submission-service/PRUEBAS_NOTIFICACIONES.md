# 🧪 Guía de Pruebas: Sistema de Notificaciones

## Prerequisitos

1. **RabbitMQ corriendo**:
   ```bash
   docker ps | grep rabbitmq
   # Debe mostrar: rabbitmq (healthy)
   ```

2. **Notification-service corriendo**:
   ```bash
   docker ps | grep notification
   # Debe mostrar: notification-service (healthy)
   ```

3. **Submission-service corriendo**:
   ```bash
   docker ps | grep submission
   # Debe mostrar: submission-service (healthy)
   ```

---

## Prueba 1: RF2 - Notificar Coordinador al Enviar Formato A (v1)

### Paso 1: Crear Formato A
```bash
curl -X POST http://localhost:8080/api/submissions/formato-a \
  -H "Content-Type: multipart/form-data" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'data={
    "titulo": "Sistema de Gestión de Inventarios IoT",
    "modalidad": "INVESTIGACION",
    "directorId": "123",
    "codirectorId": null,
    "objetivoGeneral": "Desarrollar un sistema de gestión de inventarios usando IoT",
    "objetivosEspecificos": [
      "Diseñar la arquitectura del sistema",
      "Implementar sensores IoT",
      "Desarrollar dashboard web"
    ],
    "estudiante1Id": "456",
    "estudiante2Id": null
  }' \
  -F "pdf=@/path/to/formato_a.pdf"
```

**Respuesta esperada**:
```json
{
  "id": 1
}
```

### Paso 2: Verificar Logs de Submission-Service
```bash
docker logs submission-service --tail 50
```

**Logs esperados**:
```
INFO  c.u.c.s.SubmissionService - Formato A v1 creado para proyecto 1 - Notificación enviada al coordinador: coordinador@unicauca.edu.co
DEBUG c.u.c.s.NotificationPublisher - Notificación publicada a RabbitMQ - Evento: Formato A enviado (v1), CorrelationId: abc-123-def
```

### Paso 3: Verificar RabbitMQ Management UI
1. Abrir: http://localhost:15672
2. Login: guest / guest
3. Ir a **Queues** → `notifications.q`
4. Si el notification-service está procesando rápido, el mensaje ya estará consumido
5. Para ver detalles, ir a **Message rates** (debe mostrar actividad)

### Paso 4: Verificar Logs de Notification-Service
```bash
docker logs notification-service --tail 50
```

**Logs esperados**:
```
INFO  c.u.m.n.r.NotificationConsumer - Processing notification, attempt 1, correlationId: abc-123-def
INFO  📧 [EMAIL MOCK ASYNC] Enviando correo a: coordinador@unicauca.edu.co (COORDINATOR)
     Asunto: Nuevo Formato A Enviado - Sistema de Gestión de Inventarios IoT
     Mensaje:
     Estimado(a) Coordinador(a),
     
     Se ha recibido un nuevo documento para revisión:
     
     Proyecto: Sistema de Gestión de Inventarios IoT
     Tipo de documento: FORMATO_A
     Versión: 1
     Presentado por: Juan Pérez Docente
     Fecha de envío: 2025-10-31T14:30:00
     
     Por favor, ingrese al sistema para revisar el documento.
     
     Saludos,
     Sistema de Trabajo de Grado
```

### ✅ Criterios de Éxito
- [x] HTTP 201 Created recibido
- [x] Proyecto creado en base de datos
- [x] Log en submission-service muestra envío de notificación
- [x] Log en notification-service muestra email enviado al coordinador
- [x] Email muestra versión = 1

---

## Prueba 2: RF4 - Notificar Coordinador al Reenviar Formato A (v2)

### Prerequisito: Tener un proyecto con Formato A v1 RECHAZADO

**Simular rechazo** (ejecutar como coordinador):
```bash
curl -X PUT http://localhost:8080/api/submissions/formato-a/1/estado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer COORDINATOR_JWT_TOKEN" \
  -d '{
    "estado": "RECHAZADO",
    "evaluadoPor": "Dra. María González",
    "observaciones": "Mejorar la metodología y agregar más referencias bibliográficas"
  }'
```

### Paso 1: Reenviar Formato A (versión 2)
```bash
curl -X PUT http://localhost:8080/api/submissions/formato-a/1 \
  -H "Content-Type: multipart/form-data" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "pdf=@/path/to/formato_a_v2.pdf" \
  -F "carta=@/path/to/carta.pdf"
```

**Respuesta esperada**:
```json
{
  "id": 1
}
```

### Paso 2: Verificar Logs
```bash
docker logs submission-service --tail 30
```

**Logs esperados**:
```
INFO  c.u.c.s.SubmissionService - Formato A v2 reenviado para proyecto 1 - Notificación enviada al coordinador: coordinador@unicauca.edu.co
```

```bash
docker logs notification-service --tail 30
```

**Logs esperados**:
```
INFO  📧 [EMAIL MOCK ASYNC] Enviando correo a: coordinador@unicauca.edu.co (COORDINATOR)
     Asunto: Formato A Reenviado (Versión 2) - Sistema de Gestión de Inventarios IoT
     ...
     Versión: 2
     ...
```

### ✅ Criterios de Éxito
- [x] HTTP 200 OK recibido
- [x] FormatoA v2 creado en base de datos
- [x] Log muestra versión = 2
- [x] Email indica que es un reenvío

---

## Prueba 3: RF6 - Notificar Jefe de Departamento al Enviar Anteproyecto

### Prerequisito: Tener un proyecto con Formato A APROBADO

**Simular aprobación**:
```bash
curl -X PUT http://localhost:8080/api/submissions/formato-a/1/estado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer COORDINATOR_JWT_TOKEN" \
  -d '{
    "estado": "APROBADO",
    "evaluadoPor": "Dra. María González",
    "observaciones": "Excelente propuesta"
  }'
```

### Paso 1: Enviar Anteproyecto
```bash
curl -X POST http://localhost:8080/api/submissions/anteproyecto \
  -H "Content-Type: multipart/form-data" \
  -H "Authorization: Bearer DIRECTOR_JWT_TOKEN" \
  -F 'data={"proyectoId": 1}' \
  -F "pdf=@/path/to/anteproyecto.pdf"
```

**Respuesta esperada**:
```json
{
  "id": 1
}
```

### Paso 2: Verificar Logs
```bash
docker logs submission-service --tail 30
```

**Logs esperados**:
```
INFO  c.u.c.s.SubmissionService - Anteproyecto enviado para proyecto 1 - Notificación enviada al jefe de departamento: jefe.departamento@unicauca.edu.co
```

```bash
docker logs notification-service --tail 30
```

**Logs esperados**:
```
INFO  📧 [EMAIL MOCK ASYNC] Enviando correo a: jefe.departamento@unicauca.edu.co (DEPARTMENT_HEAD)
     Asunto: Nuevo Anteproyecto Enviado - Sistema de Gestión de Inventarios IoT
     ...
     Tipo de documento: ANTEPROYECTO
     ...
```

### ✅ Criterios de Éxito
- [x] HTTP 201 Created recibido
- [x] Anteproyecto creado en base de datos
- [x] Email enviado al jefe de departamento (NO al coordinador)
- [x] businessContext.documentType = "ANTEPROYECTO"

---

## Prueba 4: Rechazo Definitivo (3er Intento)

### Prerequisito: Tener un proyecto con Formato A v3 PENDIENTE

**Crear v2 y v3**:
```bash
# Rechazar v1
curl -X PUT http://localhost:8080/api/submissions/formato-a/1/estado \
  -H "Authorization: Bearer COORDINATOR_JWT" \
  -d '{"estado":"RECHAZADO","evaluadoPor":"Coordinador","observaciones":"Mejorar"}'

# Reenviar v2
curl -X PUT http://localhost:8080/api/submissions/formato-a/1 \
  -H "Authorization: Bearer YOUR_JWT" \
  -F "pdf=@v2.pdf"

# Rechazar v2
curl -X PUT http://localhost:8080/api/submissions/formato-a/{v2_id}/estado \
  -H "Authorization: Bearer COORDINATOR_JWT" \
  -d '{"estado":"RECHAZADO","evaluadoPor":"Coordinador","observaciones":"Aún insuficiente"}'

# Reenviar v3
curl -X PUT http://localhost:8080/api/submissions/formato-a/1 \
  -H "Authorization: Bearer YOUR_JWT" \
  -F "pdf=@v3.pdf"
```

### Paso 1: Rechazar v3 (Rechazo Definitivo)
```bash
curl -X PUT http://localhost:8080/api/submissions/formato-a/{v3_id}/estado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer COORDINATOR_JWT_TOKEN" \
  -d '{
    "estado": "RECHAZADO",
    "evaluadoPor": "Dra. María González",
    "observaciones": "Rechazado definitivamente tras 3 intentos"
  }'
```

### Paso 2: Verificar Logs
```bash
docker logs submission-service --tail 40
```

**Logs esperados**:
```
INFO  c.u.c.s.SubmissionService - Notificaciones de rechazo definitivo enviadas para proyecto 1
```

```bash
docker logs notification-service --tail 40
```

**Logs esperados**:
```
INFO  📧 [EMAIL MOCK ASYNC] Enviando correo a: estudiante1@unicauca.edu.co (STUDENT)
     Asunto: Cambio de Estado del Proyecto - Sistema de Gestión de Inventarios IoT
     ...
     Estado actual: RECHAZADO_DEFINITIVO
     Estado previo: RECHAZADO
     ...

INFO  📧 [EMAIL MOCK ASYNC] Enviando correo a: director@unicauca.edu.co (STUDENT)
     Asunto: Cambio de Estado del Proyecto - Sistema de Gestión de Inventarios IoT
     ...
```

### ✅ Criterios de Éxito
- [x] Proyecto marcado como RECHAZADO_DEFINITIVO
- [x] Emails enviados a estudiantes y director
- [x] notificationType = "STATUS_CHANGED"
- [x] currentStatus = "RECHAZADO_DEFINITIVO"

---

## Pruebas de Integración con RabbitMQ

### Test 1: Verificar Cola Existe
```bash
# Desde dentro del contenedor
docker exec -it rabbitmq rabbitmqctl list_queues
```

**Salida esperada**:
```
Listing queues for vhost / ...
notifications.q    0
notifications.retry.q    0
notifications.dlq    0
```

### Test 2: Verificar Exchanges (Si aplica)
```bash
docker exec -it rabbitmq rabbitmqctl list_exchanges
```

### Test 3: Forzar Error en Notification-Service

**Detener notification-service**:
```bash
docker stop notification-service
```

**Enviar Formato A**:
```bash
curl -X POST http://localhost:8080/api/submissions/formato-a \
  -H "Content-Type: multipart/form-data" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'data={...}' \
  -F "pdf=@test.pdf"
```

**Verificar RabbitMQ UI**:
- Ir a Queues → `notifications.q`
- Debe mostrar **1 mensaje** en "Ready"

**Reiniciar notification-service**:
```bash
docker start notification-service
```

**Verificar**:
- Mensaje debe ser consumido automáticamente
- Log en notification-service debe mostrar el email

### ✅ Criterios de Éxito
- [x] Mensajes persisten en cola si consumer está down
- [x] Mensajes se procesan automáticamente al reiniciar consumer
- [x] Sistema es resiliente a fallos temporales

---

## Pruebas de Manejo de Errores

### Test 1: Email Inválido

**Modificar temporalmente** `application.yml`:
```yaml
notification:
  default:
    coordinador-email: invalid-email  # SIN @
```

**Enviar Formato A y verificar**:
- Debe fallar en notification-service (validación Jakarta)
- Mensaje debe ir a DLQ tras reintentos

### Test 2: Timeout en Identity-Service

**Simular** identityClient retornando error:
```bash
# Detener identity-service
docker stop identity-service
```

**Enviar Formato A**:
- Debe usar email por defecto (fallback)
- Notificación debe enviarse igualmente

---

## Debugging

### Ver todos los logs en tiempo real
```bash
# Terminal 1: submission-service
docker logs -f submission-service

# Terminal 2: notification-service
docker logs -f notification-service

# Terminal 3: rabbitmq
docker logs -f rabbitmq
```

### Verificar mensajes en RabbitMQ UI
1. http://localhost:15672
2. Queues → notifications.q
3. Click "Get messages"
4. Ver payload JSON completo

### Limpiar cola (si es necesario)
```bash
docker exec -it rabbitmq rabbitmqctl purge_queue notifications.q
```

---

## Troubleshooting

### Problema: No se envían notificaciones

**Verificar**:
1. RabbitMQ está corriendo: `docker ps | grep rabbitmq`
2. Submission-service puede conectarse: Ver logs por errores "AMQP"
3. Cola existe: RabbitMQ UI → Queues
4. NotificationPublisher está siendo llamado: Ver logs DEBUG

### Problema: Emails no aparecen en logs

**Verificar**:
1. Notification-service está corriendo: `docker ps | grep notification`
2. Consumer está escuchando: Ver logs "Processing notification"
3. Mensaje tiene formato correcto: Ver en RabbitMQ UI
4. No hay errores de deserialización: Ver logs por "Jackson"

### Problema: Mensajes en Dead Letter Queue

**Investigar**:
1. RabbitMQ UI → Queues → notifications.dlq
2. "Get messages" → Ver payload
3. Verificar errores en logs de notification-service
4. Corregir error y republicar manualmente

---

## Checklist de Pruebas Completo

- [ ] RF2: Crear Formato A v1 → Email al coordinador
- [ ] RF4: Reenviar Formato A v2 → Email al coordinador
- [ ] RF4: Reenviar Formato A v3 → Email al coordinador
- [ ] RF6: Enviar Anteproyecto → Email al jefe de departamento
- [ ] Rechazo definitivo → Emails a estudiantes y director
- [ ] RabbitMQ persiste mensajes si consumer está down
- [ ] Reintentos automáticos funcionan
- [ ] DLQ recibe mensajes tras max reintentos
- [ ] Manejo de errores en IdentityClient (fallback)
- [ ] Correlation IDs en logs para trazabilidad
- [ ] Logs estructurados y legibles

---

**Última actualización**: 31 de octubre de 2025

