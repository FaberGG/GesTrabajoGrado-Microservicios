# 📮 Guía Completa de Pruebas en Postman

## 🎯 Sistema de Gestión de Trabajos de Grado - Microservicios

Esta guía te llevará paso a paso para probar **TODOS** los endpoints del sistema, incluyendo la autenticación JWT, comunicación síncrona y asíncrona entre servicios.

---

## 📋 Tabla de Contenidos

1. [Configuración Inicial](#1-configuración-inicial)
2. [Autenticación y Autorización](#2-autenticación-y-autorización)
3. [Gestión de Usuarios (Identity Service)](#3-gestión-de-usuarios-identity-service)
4. [Gestión de Propuestas (Submission Service)](#4-gestión-de-propuestas-submission-service)
5. [Notificaciones (Notification Service)](#5-notificaciones-notification-service)
6. [Comunicación Síncrona vs Asíncrona](#6-comunicación-síncrona-vs-asíncrona)
7. [Verificación de Mensajería RabbitMQ](#7-verificación-de-mensajería-rabbitmq)
8. [Solución de Problemas](#8-solución-de-problemas)

---

## 1. Configuración Inicial

### 1.1. Verificar que Todos los Servicios Están Activos

Antes de empezar, verifica que todos los contenedores Docker estén corriendo:

```bash
docker-compose ps
```

**Debes ver todos los servicios como "healthy":**
- ✅ gateway-service (puerto 8080)
- ✅ identity-service (puerto 8081)
- ✅ submission-service (puerto 8082)
- ✅ notification-service (puerto 8083)
- ✅ rabbitmq (puertos 5672, 15672)
- ✅ postgres-identity, postgres-submission, postgres-notification

### 1.2. Crear una Colección en Postman

1. Abre **Postman**
2. Click en **"New Collection"**
3. Nombre: `GesTrabajoGrado - Microservicios`
4. Descripción: `Sistema de gestión de trabajos de grado con arquitectura de microservicios`

### 1.3. Configurar Variables de Entorno

Crea un **Environment** en Postman llamado `GesTrabajoGrado - Local`:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost:8080` | `http://localhost:8080` |
| `jwt_token` | *(vacío)* | *(se llenará automáticamente)* |
| `docente_token` | *(vacío)* | *(se llenará automáticamente)* |
| `estudiante_token` | *(vacío)* | *(se llenará automáticamente)* |
| `user_id` | *(vacío)* | *(se llenará automáticamente)* |

**Activa este environment** antes de hacer las pruebas.

---

## 2. Autenticación y Autorización

### ⚠️ Importante: El Gateway Requiere JWT

El **gateway-service** tiene un filtro de seguridad JWT (`JwtGatewayFilter`) que:
- ✅ **Permite** acceso sin token a rutas públicas: `/api/auth/**`, `/actuator/health`
- ❌ **Bloquea (401/403)** acceso sin token a rutas protegidas: `/api/submissions/**`, `/notifications/**`, `/api/auth/profile`, etc.

**Por lo tanto, DEBES autenticarte primero antes de acceder a recursos protegidos.**

---

## 3. Gestión de Usuarios (Identity Service)

### 3.1. Health Check (Público - No Requiere Token)

**Propósito:** Verificar que el gateway y el identity service están funcionando.

```http
GET {{base_url}}/actuator/health
```

**Headers:** (ninguno)

**Respuesta Esperada (200 OK):**
```json
{
  "status": "UP"
}
```

---

### 3.2. Registrar Usuario DOCENTE (Público - No Requiere Token)

**Propósito:** Crear un usuario con rol DOCENTE que podrá crear propuestas.

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "nombres": "Dr Carlos",
  "apellidos": "Méndez",
  "email": "carlos.mendez@unicauca.edu.co",
  "password": "Docente123!",
  "rol": "DOCENTE",
  "programa": "INGENIERIA_SISTEMAS"
}
```

**Respuesta Esperada (201 Created):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 1,
    "nombres": "Dr Carlos",
    "apellidos": "Méndez",
    "email": "carlos.mendez@unicauca.edu.co",
    "rol": "DOCENTE",
    "programa": "INGENIERIA_SISTEMAS"
  },
  "timestamp": "2025-10-16T22:40:00"
}
```

**📝 Nota:** Guarda el `id` del usuario, lo necesitarás después.

---

### 3.3. Registrar Usuario ESTUDIANTE (Público - No Requiere Token)

**Propósito:** Crear un estudiante que podrá visualizar propuestas (pero no crearlas).

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "nombres": "María",
  "apellidos": "González",
  "email": "maria.gonzalez@unicauca.edu.co",
  "password": "Estudiante123!",
  "rol": "ESTUDIANTE",
  "programa": "INGENIERIA_SISTEMAS"
}
```

**Respuesta Esperada (201 Created):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 2,
    "nombres": "María",
    "apellidos": "González",
    "email": "maria.gonzalez@unicauca.edu.co",
    "rol": "ESTUDIANTE",
    "programa": "INGENIERIA_SISTEMAS"
  },
  "timestamp": "2025-10-16T22:41:00"
}
```

---

### 3.4. Login como DOCENTE (Público - No Requiere Token)

**Propósito:** Obtener un token JWT para el docente.

```http
POST {{base_url}}/api/auth/login
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "carlos.mendez@unicauca.edu.co",
  "password": "Docente123!"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxIiwicm9sZSI6IkRPQ0VOVEUiLCJlbWFpbCI6ImNhcmxvcy5tZW5kZXpAdW5pY2F1Y2EuZWR1LmNvIn0...",
    "type": "Bearer",
    "usuario": {
      "id": 1,
      "nombres": "Dr Carlos",
      "apellidos": "Méndez",
      "email": "carlos.mendez@unicauca.edu.co",
      "rol": "DOCENTE"
    }
  },
  "timestamp": "2025-10-16T22:42:00"
}
```

**🔑 IMPORTANTE: Guardar el Token**

En la pestaña **Tests** de esta petición en Postman, añade este script para guardar automáticamente el token:

```javascript
// Guardar token JWT automáticamente
if (pm.response.code === 200) {
    const jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.token) {
        pm.environment.set("docente_token", jsonData.data.token);
        pm.environment.set("jwt_token", jsonData.data.token);
        pm.environment.set("user_id", jsonData.data.usuario.id);
        console.log("✅ Token de docente guardado en variables de entorno");
    }
}
```

**Ahora `{{jwt_token}}` contendrá tu token de autenticación.**

---

### 3.5. Login como ESTUDIANTE (Público - No Requiere Token)

**Propósito:** Obtener un token JWT para el estudiante.

```http
POST {{base_url}}/api/auth/login
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "maria.gonzalez@unicauca.edu.co",
  "password": "Estudiante123!"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "usuario": {
      "id": 2,
      "nombres": "María",
      "apellidos": "González",
      "email": "maria.gonzalez@unicauca.edu.co",
      "rol": "ESTUDIANTE"
    }
  },
  "timestamp": "2025-10-16T22:43:00"
}
```

**Script de Tests para guardar el token:**
```javascript
if (pm.response.code === 200) {
    const jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.token) {
        pm.environment.set("estudiante_token", jsonData.data.token);
        console.log("✅ Token de estudiante guardado");
    }
}
```

---

### 3.6. Obtener Perfil del Usuario (🔒 Requiere Token)

**Propósito:** Ver información del usuario autenticado.

```http
GET {{base_url}}/api/auth/profile
Authorization: Bearer {{jwt_token}}
```

**Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Respuesta Esperada (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "nombres": "Dr Carlos",
    "apellidos": "Méndez",
    "email": "carlos.mendez@unicauca.edu.co",
    "rol": "DOCENTE",
    "programa": "INGENIERIA_SISTEMAS"
  },
  "timestamp": "2025-10-16T22:44:00"
}
```

**⚠️ Si NO incluyes el token, obtendrás:**
```json
{
  "error": "Unauthorized",
  "message": "Token missing"
}
```

---

### 3.7. Obtener Roles y Programas Disponibles (🔒 Requiere Token)

**Propósito:** Ver los roles y programas académicos disponibles en el sistema.

```http
GET {{base_url}}/api/auth/roles
Authorization: Bearer {{jwt_token}}
```

**Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Respuesta Esperada (200 OK):**
```json
{
  "success": true,
  "data": {
    "roles": [
      "ESTUDIANTE",
      "DOCENTE",
      "COORDINADOR"
    ],
    "programas": [
      "INGENIERIA_SISTEMAS",
      "INGENIERIA_ELECTRONICA",
      "INGENIERIA_CIVIL"
    ]
  },
  "timestamp": "2025-10-16T22:45:00"
}
```

---

### 3.8. Buscar Usuarios (🔒 Requiere Token)

**Propósito:** Buscar usuarios por nombre, rol o programa.

```http
GET {{base_url}}/api/auth/users/search?query=Carlos&rol=DOCENTE&page=0&size=10
Authorization: Bearer {{jwt_token}}
```

**Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Query Parameters:**
- `query` (opcional): Texto a buscar en nombre o email
- `rol` (opcional): Filtrar por rol (ESTUDIANTE, DOCENTE, COORDINADOR)
- `programa` (opcional): Filtrar por programa académico
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 10)

**Respuesta Esperada (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "nombres": "Dr Carlos",
        "apellidos": "Méndez",
        "email": "carlos.mendez@unicauca.edu.co",
        "rol": "DOCENTE",
        "programa": "INGENIERIA_SISTEMAS"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2025-10-16T22:46:00"
}
```

---

## 4. Gestión de Propuestas (Submission Service)

### ⚠️ IMPORTANTE: Solo Docentes Pueden Crear Propuestas

El **Submission Service** valida que el usuario tenga rol **DOCENTE** antes de permitir la creación de propuestas. El gateway extrae el rol del JWT y lo envía en el header `X-User-Role`.

**Asegúrate de usar el token del DOCENTE (`{{docente_token}}`) para estas pruebas.**

---

### 4.1. Crear Propuesta de Trabajo de Grado - Formato A (🔒 Solo DOCENTE)

**Propósito:** Un docente crea una propuesta de proyecto de grado con el formato A requerido.

```http
POST {{base_url}}/api/submissions
Authorization: Bearer {{docente_token}}
Content-Type: application/json
```

**Headers:**
```
Authorization: Bearer {{docente_token}}
Content-Type: application/json
```

**Body (JSON) - Formato A:**
```json
{
  "titulo": "Sistema de Gestión de Inventarios con IoT",
  "descripcion": "Desarrollo de un sistema web integrado con sensores IoT para gestión de inventarios en tiempo real. El sistema permitirá monitoreo automatizado de stock, alertas de reabastecimiento y generación de reportes analíticos.",
  "objetivoGeneral": "Desarrollar un sistema de gestión de inventarios inteligente que utilice tecnología IoT para automatizar el control de stock y mejorar la eficiencia operativa de las empresas.",
  "objetivosEspecificos": [
    "Diseñar e implementar una arquitectura de microservicios escalable para el backend",
    "Integrar sensores IoT (RFID, peso, temperatura) para monitoreo automatizado",
    "Desarrollar dashboards interactivos con visualización de datos en tiempo real",
    "Implementar algoritmos de machine learning para predicción de demanda"
  ],
  "alcance": "El proyecto cubrirá la implementación de un MVP funcional para una bodega de tamaño mediano (hasta 1000 productos). Incluye módulos de: gestión de productos, monitoreo IoT, reportes y alertas. No incluye integración con sistemas ERP externos.",
  "metodologia": "Se utilizará Scrum como metodología ágil, con sprints de 2 semanas. Desarrollo iterativo e incremental con pruebas continuas. Stack tecnológico: Spring Boot, Angular, MQTT, PostgreSQL, Redis.",
  "recursosTecnicos": {
    "hardware": ["Raspberry Pi 4", "Sensores RFID RC522", "Celdas de carga HX711", "Sensores DHT22"],
    "software": ["Java 21", "Spring Boot 3.x", "Angular 17", "PostgreSQL 15", "RabbitMQ", "Docker"],
    "herramientas": ["IntelliJ IDEA", "VS Code", "Git", "Jenkins", "SonarQube", "Postman"]
  },
  "resultadosEsperados": "Sistema funcional desplegado en ambiente productivo, con documentación técnica completa, casos de prueba automatizados (cobertura >80%), y manual de usuario. Se espera reducir en un 30% el tiempo de gestión de inventarios manual.",
  "cronograma": {
    "mes1": "Análisis de requerimientos y diseño de arquitectura",
    "mes2": "Desarrollo del backend y APIs REST",
    "mes3": "Desarrollo del frontend y integración IoT",
    "mes4": "Pruebas, documentación y despliegue"
  },
  "docenteDirectorId": 1,
  "programa": "INGENIERIA_SISTEMAS",
  "modalidad": "PROYECTO_APLICADO",
  "numeroEstudiantesRequeridos": 2
}
```

**Respuesta Esperada (201 Created):**
```json
{
  "success": true,
  "message": "Propuesta creada exitosamente",
  "data": {
    "id": 1,
    "titulo": "Sistema de Gestión de Inventarios con IoT",
    "descripcion": "Desarrollo de un sistema web integrado con sensores IoT...",
    "estado": "EN_REVISION",
    "docenteDirector": {
      "id": 1,
      "nombre": "Dr. Carlos Méndez",
      "correo": "carlos.mendez@unicauca.edu.co"
    },
    "programa": "INGENIERIA_SISTEMAS",
    "modalidad": "PROYECTO_APLICADO",
    "fechaCreacion": "2025-10-16T22:50:00",
    "notificacionEnviada": true
  },
  "timestamp": "2025-10-16T22:50:00"
}
```

**🔔 Comunicación Asíncrona Activada:**
Al crear una propuesta, automáticamente:
1. ✅ El Submission Service **publica un mensaje** en RabbitMQ (cola: `submission.queue`)
2. ✅ El Notification Service **escucha** la cola y **consume el mensaje**
3. ✅ Se **envía una notificación** (email mock) al docente director

**Verifica los logs:**
```bash
docker-compose logs -f submission
docker-compose logs -f notification
```

---

### 4.2. Intentar Crear Propuesta como ESTUDIANTE (Debe Fallar)

**Propósito:** Verificar que el sistema rechaza la creación de propuestas por usuarios no autorizados.

```http
POST {{base_url}}/api/submissions
Authorization: Bearer {{estudiante_token}}
Content-Type: application/json
```

**Headers:**
```
Authorization: Bearer {{estudiante_token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "titulo": "Intento de Propuesta por Estudiante",
  "descripcion": "Esta propuesta debería ser rechazada",
  "docenteDirectorId": 1
}
```

**Respuesta Esperada (403 Forbidden):**
```json
{
  "error": "Forbidden",
  "message": "Solo usuarios con rol DOCENTE pueden crear propuestas",
  "timestamp": "2025-10-16T22:52:00"
}
```

**✅ Esto confirma que la validación de roles está funcionando correctamente.**

---

### 4.3. Health Check del Submission Service (🔒 Requiere Token)

```http
GET {{base_url}}/api/submissions/health
Authorization: Bearer {{jwt_token}}
```

**Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Respuesta Esperada (200 OK):**
```
ok
```

---

## 5. Notificaciones (Notification Service)

El **Notification Service** maneja notificaciones de dos formas:
1. **Síncrona (HTTP):** Envío directo con respuesta inmediata
2. **Asíncrona (RabbitMQ):** Publicación en cola para procesamiento posterior

---

### 5.1. Enviar Notificación Síncrona (🔒 Requiere Token)

**Propósito:** Enviar una notificación directamente y recibir confirmación inmediata.

```http
POST {{base_url}}/api/notifications
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Headers:**
```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "destinatario": "carlos.mendez@unicauca.edu.co",
  "asunto": "Prueba de Notificación Síncrona",
  "cuerpo": "Este es un mensaje de prueba enviado síncronamente a través del servicio de notificaciones.",
  "tipo": "EMAIL"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "success": true,
  "message": "Notificación enviada correctamente (MOCK MODE)",
  "data": {
    "id": "notif-123456",
    "destinatario": "carlos.mendez@unicauca.edu.co",
    "asunto": "Prueba de Notificación Síncrona",
    "estado": "ENVIADA",
    "fechaEnvio": "2025-10-16T22:55:00"
  }
}
```

**📧 Modo MOCK:** Por defecto, el sistema está en modo MOCK (no envía emails reales), solo registra en logs.

**Ver el log:**
```bash
docker-compose logs notification | grep "Notificación"
```

---

### 5.2. Enviar Notificación Asíncrona (🔒 Requiere Token)

**Propósito:** Publicar una notificación en RabbitMQ para procesamiento asíncrono.

```http
POST {{base_url}}/notifications/async
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Headers:**
```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "destinatario": "maria.gonzalez@unicauca.edu.co",
  "asunto": "Notificación Asíncrona - Nueva Propuesta Disponible",
  "cuerpo": "Hola María, hay una nueva propuesta de trabajo de grado disponible en el sistema. Revisa el módulo de propuestas para más información.",
  "tipo": "EMAIL"
}
```

**Respuesta Esperada (202 Accepted):**
```
(Respuesta vacía - HTTP 202)
```

**✅ La respuesta 202 Accepted significa:**
- La petición fue aceptada
- El mensaje se publicó en RabbitMQ
- El procesamiento se realizará de forma asíncrona

**Verificar en RabbitMQ:**
1. Abre: `http://localhost:15672`
2. Login: `guest` / `guest`
3. Ve a **Queues** → `notification.email.queue`
4. Deberías ver el mensaje en la cola o ya procesado

---

## 6. Comunicación Síncrona vs Asíncrona

### 🔄 Comparativa de Ambos Métodos

#### **Comunicación SÍNCRONA (HTTP REST)**

**Características:**
- ✅ Respuesta inmediata
- ✅ Cliente espera hasta recibir confirmación
- ❌ Bloquea el hilo hasta completar operación
- ❌ No escala bien con alta carga

**Ejemplo: Envío de notificación síncrona**
```
Cliente → POST /notifications → Notification Service
                                         ↓
                                    Envía email
                                         ↓
Cliente ← HTTP 200 ← Notification Service
```

**Tiempo de respuesta:** ~200-500ms

---

#### **Comunicación ASÍNCRONA (RabbitMQ)**

**Características:**
- ✅ Respuesta inmediata (202 Accepted)
- ✅ No bloquea el cliente
- ✅ Escalable y tolerante a fallos
- ✅ Permite procesamiento en batch
- ❌ No hay confirmación inmediata de éxito/fallo

**Ejemplo: Creación de propuesta**
```
Cliente → POST /api/submissions → Submission Service
                                         ↓
                                  Guarda en BD
                                         ↓
                                  Publica mensaje en RabbitMQ
                                         ↓
Cliente ← HTTP 201 Created ←    (respuesta inmediata)

(En paralelo, de forma asíncrona)
RabbitMQ Queue → Notification Service escucha
                         ↓
                    Envía notificación
```

**Tiempo de respuesta:** ~50-100ms (no espera envío de email)

---

### 6.1. Prueba Completa de Comunicación Asíncrona

**Paso 1:** Crear una nueva propuesta (como DOCENTE)

```http
POST {{base_url}}/api/submissions
Authorization: Bearer {{docente_token}}
Content-Type: application/json

{
  "titulo": "Aplicación Móvil para Transporte Urbano",
  "descripcion": "App móvil con geolocalización para optimizar rutas de transporte público",
  "docenteDirectorId": 1,
  "programa": "INGENIERIA_SISTEMAS"
}
```

**Paso 2:** Ver logs en tiempo real

Abre **2 terminales** y ejecuta:

**Terminal 1 (Submission Service):**
```bash
docker-compose logs -f submission
```

**Terminal 2 (Notification Service):**
```bash
docker-compose logs -f notification
```

**Paso 3:** Analizar la secuencia de eventos

En los logs deberías ver:

**Submission Service:**
```
INFO  - Creando nueva propuesta: Aplicación Móvil para Transporte Urbano
INFO  - Propuesta guardada en BD con ID: 2
INFO  - Publicando mensaje en RabbitMQ (exchange: submission.exchange)
INFO  - Mensaje publicado exitosamente
```

**Notification Service:**
```
INFO  - Mensaje recibido de RabbitMQ
INFO  - Procesando notificación para: carlos.mendez@unicauca.edu.co
INFO  - Asunto: Nueva propuesta creada - Aplicación Móvil para Transporte Urbano
INFO  - [MOCK] Email enviado exitosamente
INFO  - Notificación procesada correctamente
```

**✅ Esto demuestra:**
- El Submission Service no espera a que se envíe la notificación
- El cliente recibe respuesta rápida (201 Created)
- El Notification Service procesa el mensaje de forma independiente
- Si el Notification Service falla, no afecta la creación de la propuesta

---

### 6.2. Ventajas del Patrón Asíncrono en Este Sistema

1. **Resiliencia:** Si el Notification Service está caído, los mensajes quedan en RabbitMQ esperando
2. **Escalabilidad:** Se pueden tener múltiples instancias del Notification Service procesando en paralelo
3. **Desacoplamiento:** Submission Service no depende de Notification Service
4. **Rendimiento:** El usuario no espera a que se envíe el email
5. **Reintento automático:** RabbitMQ puede reintentar mensajes fallidos

---

## 7. Verificación de Mensajería RabbitMQ

### 7.1. Acceder a RabbitMQ Management Console

```
URL: http://localhost:15672
Usuario: guest
Contraseña: guest
```

### 7.2. Ver Colas Activas

1. Click en **Queues**
2. Busca estas colas:
   - `submission.queue` - Mensajes de nuevas propuestas
   - `notification.email.queue` - Cola de emails pendientes
   - `notification.queue` - Cola general de notificaciones

### 7.3. Ver Mensajes en Cola

1. Click en el nombre de una cola
2. Ve a la sección **Get messages**
3. Click en **Get Message(s)**
4. Verás el contenido JSON del mensaje

**Ejemplo de mensaje en `submission.queue`:**
```json
{
  "submissionId": 2,
  "titulo": "Aplicación Móvil para Transporte Urbano",
  "docenteEmail": "carlos.mendez@unicauca.edu.co",
  "docenteNombre": "Dr. Carlos Méndez",
  "fechaCreacion": "2025-10-16T22:55:00",
  "eventType": "SUBMISSION_CREATED"
}
```

### 7.4. Ver Estadísticas de Mensajes

En la pestaña **Overview** puedes ver:
- **Total de mensajes publicados**
- **Tasa de publicación** (mensajes/segundo)
- **Total de mensajes consumidos**
- **Mensajes pendientes** en cada cola

---

## 8. Solución de Problemas

### 8.1. Error 401 Unauthorized

**Problema:** Recibes `{"error":"Unauthorized","message":"Token missing"}`

**Solución:**
1. Verifica que estás usando el header: `Authorization: Bearer {{jwt_token}}`
2. Asegúrate de haber hecho login primero (paso 3.4)
3. Verifica que la variable `{{jwt_token}}` tenga valor en el environment
4. El token expira en 24 horas, haz login nuevamente si es necesario

---

### 8.2. Error 403 Forbidden

**Problema:** Recibes `{"error":"Forbidden","message":"Solo usuarios con rol DOCENTE pueden crear propuestas"}`

**Solución:**
- Estás usando un token de ESTUDIANTE para crear propuestas
- Usa `{{docente_token}}` en lugar de `{{jwt_token}}`
- Asegúrate de haber hecho login como DOCENTE (paso 3.4)

---

### 8.3. Gateway No Responde / Timeout

**Problema:** Las peticiones tardan mucho o no responden

**Solución:**
```bash
# Verificar estado de servicios
docker-compose ps

# Ver logs del gateway
docker-compose logs gateway

# Reiniciar el gateway
docker-compose restart gateway
```

---

### 8.4. Notificaciones No Se Envían

**Problema:** No ves logs de notificaciones después de crear propuesta

**Solución:**
```bash
# Verificar RabbitMQ
docker-compose logs rabbitmq

# Verificar Notification Service
docker-compose logs notification

# Verificar conectividad
curl http://localhost:15672/api/overview
```

---

### 8.5. Base de Datos Vacía

**Problema:** No hay usuarios o propuestas en el sistema

**Solución:**
- Ejecuta los pasos de registro (3.2 y 3.3) para crear usuarios
- Asegúrate de que los servicios estén conectados a PostgreSQL
- Verifica logs de identity/submission services

---

## 9. Colección Completa de Postman

### 9.1. Importar Colección JSON

Crea un archivo `GesTrabajoGrado.postman_collection.json` con la colección completa.

### 9.2. Orden Recomendado de Ejecución

**Fase 1: Setup (Sin token)**
1. Health Check
2. Registrar Docente
3. Registrar Estudiante
4. Login Docente
5. Login Estudiante

**Fase 2: Gestión de Usuarios (Con token docente)**
6. Obtener Perfil
7. Obtener Roles
8. Buscar Usuarios

**Fase 3: Gestión de Propuestas (Con token docente)**
9. Crear Propuesta - Formato A
10. (Opcional) Intentar crear propuesta como estudiante (debe fallar)

**Fase 4: Notificaciones**
11. Enviar Notificación Síncrona
12. Enviar Notificación Asíncrona

**Fase 5: Verificación**
13. Ver logs de servicios
14. Revisar RabbitMQ Management Console
15. Verificar mensajes procesados

---

## 10. Casos de Prueba Adicionales

### 10.1. Registro con Datos Inválidos

**Propósito:** Verificar validaciones del sistema

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "nombres": "A",
  "apellidos": "B",
  "email": "correo-invalido",
  "password": "123",
  "rol": "ROL_INEXISTENTE",
  "programa": "PROGRAMA_INVALIDO"
}
```

**Respuesta Esperada (400 Bad Request):**
```json
{
  "error": "Validation Error",
  "message": "Datos inválidos",
  "details": [
    "nombres: debe tener al menos 2 caracteres",
    "apellidos: debe tener al menos 2 caracteres",
    "email: formato de email inválido",
    "password: debe tener al menos 8 caracteres y contener mayúsculas, minúsculas y números",
    "rol: debe ser uno de [ESTUDIANTE, DOCENTE, COORDINADOR]",
    "programa: debe ser uno de [INGENIERIA_SISTEMAS, INGENIERIA_ELECTRONICA, INGENIERIA_CIVIL]"
  ]
}
```

---

### 10.2. Login con Credenciales Incorrectas

```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "carlos.mendez@unicauca.edu.co",
  "password": "PasswordIncorrecto123"
}
```

**Respuesta Esperada (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "Credenciales inválidas"
}
```

---

### 10.3. Acceso a Ruta Protegida Sin Token

```http
GET {{base_url}}/api/auth/profile
```

**Headers:** (sin Authorization)

**Respuesta Esperada (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "Token missing"
}
```

---

## 11. Checklist Final de Pruebas

Marca cada item cuando lo completes:

- [ ] Todos los servicios están corriendo (docker-compose ps)
- [ ] Health check del gateway responde OK
- [ ] Usuario DOCENTE registrado exitosamente
- [ ] Usuario ESTUDIANTE registrado exitosamente
- [ ] Login como DOCENTE obtiene token JWT válido
- [ ] Token guardado en variable de entorno `{{docente_token}}`
- [ ] Perfil de usuario se obtiene correctamente con token
- [ ] Búsqueda de usuarios funciona
- [ ] Propuesta creada exitosamente (con token de DOCENTE)
- [ ] Creación de propuesta como ESTUDIANTE es rechazada (403)
- [ ] Notificación síncrona enviada y confirmada
- [ ] Notificación asíncrona aceptada (202)
- [ ] Logs de Submission Service muestran publicación en RabbitMQ
- [ ] Logs de Notification Service muestran consumo de mensaje
- [ ] RabbitMQ Management Console muestra mensajes procesados
- [ ] Verificación de cola `submission.queue` en RabbitMQ
- [ ] Verificación de cola `notification.email.queue` en RabbitMQ

---

## 12. Contacto y Soporte

Si encuentras problemas:

1. **Revisa los logs:**
   ```bash
   docker-compose logs -f
   ```

2. **Verifica el estado de servicios:**
   ```bash
   docker-compose ps
   ```

3. **Reinicia los servicios si es necesario:**
   ```bash
   docker-compose restart
   ```

4. **Limpia y reconstruye (último recurso):**
   ```bash
   docker-compose down -v
   docker-compose build --no-cache
   docker-compose up -d
   ```

---

## 📚 Documentación Adicional

- **Arquitectura del Sistema:** Ver `ANALISIS_TECNICO.md`
- **Inicio Rápido:** Ver `INICIO_RAPIDO.md`
- **Solución de Problemas:** Ver `TROUBLESHOOTING.md`
- **Swagger UI (cuando esté habilitado):**
  - Identity: `http://localhost:8081/swagger-ui.html`
  - Submission: `http://localhost:8082/swagger-ui.html`
  - Notification: `http://localhost:8083/swagger-ui.html`

---

## ✅ ¡Listo para Probar!

Ahora tienes una guía completa para probar **TODOS** los aspectos del sistema:
- ✅ Autenticación y autorización con JWT
- ✅ Gestión de usuarios y roles
- ✅ Creación de propuestas (solo docentes)
- ✅ Comunicación síncrona (HTTP)
- ✅ Comunicación asíncrona (RabbitMQ)
- ✅ Verificación de mensajería
- ✅ Manejo de errores y validaciones

**¡Éxito con las pruebas! 🚀**
