# 🎓 Sistema de Gestión de Trabajo de Grado - Microservicios

Sistema completo basado en microservicios para la gestión de trabajos de grado, construido con Spring Boot y arquitectura de microservicios.

## 📋 Arquitectura

El sistema está compuesto por los siguientes servicios:

- **Gateway Service** (Puerto 8080): Punto de entrada único, enrutamiento y autenticación
- **Identity Service** (Puerto 8081): Gestión de usuarios y autenticación JWT
- **Submission Service** (Puerto 8082): Gestión de entregas y documentos
- **Notification Service** (Puerto 8083): Envío de notificaciones y emails
- **RabbitMQ** (Puertos 5672, 15672): Message broker para comunicación asíncrona
- **PostgreSQL**: 3 bases de datos independientes (puertos 5432, 5433, 5434)

## 🚀 Inicio Rápido con Docker Compose

### Prerequisitos

- Docker Desktop instalado
- Docker Compose (incluido en Docker Desktop)
- Al menos 4GB de RAM disponible
- Puertos 8080-8083, 5432-5434, 5672, 15672 disponibles

### Pasos para Iniciar

1. **Configurar variables de entorno**
   ```bash
   # Copiar el archivo de ejemplo
   copy .env.example .env
   
   # Editar .env con tus valores reales
   notepad .env
   ```

2. **Iniciar todos los servicios**
   ```bash
   docker-compose up -d
   ```

3. **Verificar el estado de los servicios**
   ```bash
   docker-compose ps
   ```

4. **Ver logs de todos los servicios**
   ```bash
   docker-compose logs -f
   ```

5. **Ver logs de un servicio específico**
   ```bash
   docker-compose logs -f gateway
   docker-compose logs -f identity
   docker-compose logs -f submission
   docker-compose logs -f notification
   ```

### Detener los Servicios

```bash
# Detener sin eliminar volúmenes (datos se mantienen)
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

## 🔧 Distribución de Puertos

### Servicios de Aplicación
| Servicio | Puerto Interno | Puerto Externo | URL |
|----------|---------------|----------------|-----|
| Gateway | 8080 | 8080 | http://localhost:8080 |
| Identity | 8081 | 8081 | http://localhost:8081 |
| Submission | 8082 | 8082 | http://localhost:8082 |
| Notification | 8083 | 8083 | http://localhost:8083 |

### Infraestructura
| Servicio | Puerto Interno | Puerto Externo | Descripción |
|----------|---------------|----------------|-------------|
| RabbitMQ AMQP | 5672 | 5672 | Protocolo de mensajería |
| RabbitMQ Management | 15672 | 15672 | UI de administración |
| PostgreSQL Identity | 5432 | 5432 | Base de datos Identity |
| PostgreSQL Submission | 5432 | 5433 | Base de datos Submission |
| PostgreSQL Notification | 5432 | 5434 | Base de datos Notification |

## 🔐 Variables de Entorno Requeridas

El archivo `.env` debe contener:

```env
# JWT - Secret para firmar tokens (mínimo 32 caracteres)
JWT_SECRET=your-super-secret-jwt-key-change-this

# RabbitMQ - Credenciales del message broker
RABBITMQ_USER=admin
RABBITMQ_PASS=admin_password

# Bases de Datos
IDENTITY_DB_USER=identity_user
IDENTITY_DB_PASS=identity_password

SUBMISSION_DB_USER=submission_user
SUBMISSION_DB_PASS=submission_password

NOTIFICATION_DB_USER=notification_user
NOTIFICATION_DB_PASS=notification_password

# SMTP - Configuración para envío de emails
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
SMTP_FROM=noreply@trabajogrado.com
```

## 🏥 Health Checks

Todos los servicios exponen endpoints de health check:

- Gateway: http://localhost:8080/api/gateway/health
- Identity: http://localhost:8081/actuator/health
- Submission: http://localhost:8082/actuator/health
- Notification: http://localhost:8083/actuator/health
- RabbitMQ: http://localhost:15672 (usuario/contraseña desde .env)

## 📊 Monitoreo

### RabbitMQ Management UI
Accede a http://localhost:15672 con las credenciales configuradas en `.env`

### Logs en Tiempo Real
```bash
# Todos los servicios
docker-compose logs -f

# Servicio específico
docker-compose logs -f gateway
```

## 🔄 Reconstruir Servicios

Cuando hagas cambios en el código:

```bash
# Reconstruir un servicio específico
docker-compose up -d --build gateway

# Reconstruir todos los servicios
docker-compose up -d --build
```

## 🐛 Troubleshooting

### Los contenedores no inician
```bash
# Ver logs detallados
docker-compose logs

# Verificar puertos en uso
netstat -ano | findstr "8080"
netstat -ano | findstr "5432"
```

### Limpiar y reiniciar desde cero
```bash
# Detener y eliminar todo
docker-compose down -v

# Eliminar imágenes
docker-compose down --rmi all -v

# Reconstruir y iniciar
docker-compose up -d --build
```

### Error de conexión a base de datos
- Verificar que las variables de entorno en `.env` coincidan
- Esperar 30-60 segundos para que las bases de datos inicien completamente
- Revisar logs: `docker-compose logs postgres-identity`

### Problemas con RabbitMQ
```bash
# Verificar estado
docker-compose exec rabbitmq rabbitmq-diagnostics ping

# Ver logs
docker-compose logs rabbitmq
```

## 🏗️ Desarrollo Local vs Producción

### Modo Desarrollo (servicios individuales)
Cada microservicio tiene su propio `docker-compose.yml` para desarrollo aislado:
- `identity-service/docker-compose.yml`
- `submission-service/docker-compose.yml`
- `notification-service/docker-compose.yml`

### Modo Producción (docker-compose raíz)
El `docker-compose.yaml` en la raíz inicia **todo el sistema completo** con:
- Todos los microservicios
- Todas las bases de datos
- RabbitMQ
- Red compartida `trabajo-grado-network`
- Health checks configurados
- Límites de recursos

## 📁 Estructura del Proyecto

```
GesTrabajoGrado-Microservicios/
├── docker-compose.yaml          # ⭐ Docker Compose principal (USAR ESTE)
├── .env                          # Variables de entorno (crear desde .env.example)
├── .env.example                  # Plantilla de variables de entorno
├── README.md                     # Esta documentación
├── gateway-service/
│   ├── Dockerfile
│   └── src/
├── identity-service/
│   ├── Dockerfile
│   ├── docker-compose.yml       # Para desarrollo individual
│   └── src/
├── submission-service/
│   ├── Dockerfile
│   ├── docker-compose.yml       # Para desarrollo individual
│   └── src/
└── notification-service/
    ├── Dockerfile
    ├── docker-compose.yml       # Para desarrollo individual
    └── src/
```

## ✅ Checklist de Despliegue

- [ ] Copiar `.env.example` a `.env`
- [ ] Configurar todas las variables en `.env`
- [ ] Cambiar `JWT_SECRET` por un valor seguro (mínimo 32 caracteres)
- [ ] Configurar credenciales SMTP reales para emails
- [ ] Verificar que los puertos estén disponibles
- [ ] Ejecutar `docker-compose up -d`
- [ ] Esperar 1-2 minutos para que todos los servicios inicien
- [ ] Verificar health checks de todos los servicios
- [ ] Probar acceso al gateway: http://localhost:8080

## 📞 Endpoints Principales

### Gateway (Punto de Entrada Único)
- Base URL: http://localhost:8080
- Health: http://localhost:8080/api/gateway/health

### A través del Gateway:
- Auth: `POST http://localhost:8080/api/identity/auth/login`
- Submissions: `http://localhost:8080/api/submissions/*`
- Notifications: `http://localhost:8080/api/notifications/*`

## 🔒 Seguridad

- Todos los servicios corren con usuarios no-root
- JWT para autenticación
- Variables de entorno para secretos
- Health checks configurados
- Límites de recursos por contenedor

## 🎯 Próximos Pasos

1. Configurar CI/CD para builds automatizados
2. Agregar monitoring con Prometheus/Grafana
3. Implementar circuit breakers con Resilience4j
4. Agregar API Gateway rate limiting
5. Configurar logs centralizados con ELK Stack

---

**Autor**: Equipo de Desarrollo - Unicauca  
**Fecha**: Octubre 2025  
**Versión**: 1.0.0

"# MicroservicioSubmission" 
