# 🚀 Inicio Rápido - Submission Service

Guía rápida para ejecutar el microservicio de entregas localmente.

## ⚡ Opción 1: Docker Compose (Recomendado)

```bash
# 1. Navegar al directorio del servicio
cd submission-service

# 2. Iniciar todos los servicios
docker-compose up -d

# 3. Verificar que todo esté corriendo
docker-compose ps

# 4. Ver logs en tiempo real
docker-compose logs -f submission-service
```

### Servicios Iniciados
- **PostgreSQL**: `localhost:5435`
- **RabbitMQ**: `localhost:5672` (Management: `localhost:15672`)
- **Submission Service**: `localhost:8082`

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

## 🧪 Probar el Servicio

### 1. Crear Formato A (Modalidad Investigación)

```bash
curl -X POST http://localhost:8082/api/submissions/formatoA \
  -H "X-User-Role: DOCENTE" \
  -H "X-User-Id: 101" \
  -F 'data={
    "titulo": "Sistema de Gestión con IoT",
    "modalidad": "INVESTIGACION",
    "objetivoGeneral": "Desarrollar un sistema...",
    "objetivosEspecificos": ["Obj 1", "Obj 2"],
    "directorId": 101,
    "estudiante1Id": 201
  };type=application/json' \
  -F 'pdf=@formato-a.pdf'
```

### 2. Crear Formato A (Modalidad Práctica Profesional)

```bash
curl -X POST http://localhost:8082/api/submissions/formatoA \
  -H "X-User-Role: DOCENTE" \
  -H "X-User-Id: 101" \
  -F 'data={
    "titulo": "Práctica en Empresa X",
    "modalidad": "PRACTICA_PROFESIONAL",
    "objetivoGeneral": "Realizar práctica...",
    "objetivosEspecificos": ["Obj 1"],
    "directorId": 101,
    "estudiante1Id": 201
  };type=application/json' \
  -F 'pdf=@formato-a.pdf' \
  -F 'carta=@carta-empresa.pdf'
```

### 3. Listar Formatos A

```bash
# Todos los formatos
curl http://localhost:8082/api/submissions/formatoA

# Por docente específico
curl "http://localhost:8082/api/submissions/formatoA?docenteId=101&page=0&size=20"
```

### 4. Obtener Formato A por ID

```bash
curl http://localhost:8082/api/submissions/formatoA/1
```

### 5. Reenviar Formato A (tras rechazo)

```bash
curl -X POST http://localhost:8082/api/submissions/formatoA/1/nueva-version \
  -H "X-User-Role: DOCENTE" \
  -H "X-User-Id: 101" \
  -F 'pdf=@formato-a-v2.pdf'
```

### 6. Cambiar Estado (Review Service)

```bash
# Aprobar
curl -X PATCH http://localhost:8082/api/submissions/formatoA/1/estado \
  -H "X-Service: review" \
  -H "Content-Type: application/json" \
  -d '{
    "estado": "APROBADO",
    "observaciones": "Excelente propuesta",
    "evaluadoPor": 50
  }'

# Rechazar
curl -X PATCH http://localhost:8082/api/submissions/formatoA/1/estado \
  -H "X-Service: review" \
  -H "Content-Type: application/json" \
  -d '{
    "estado": "RECHAZADO",
    "observaciones": "Necesita mejoras en la metodología",
    "evaluadoPor": 50
  }'
```

### 7. Subir Anteproyecto

```bash
curl -X POST http://localhost:8082/api/submissions/anteproyecto \
  -H "X-User-Role: DOCENTE" \
  -H "X-User-Id: 101" \
  -F 'data={"proyectoId": 1};type=application/json' \
  -F 'pdf=@anteproyecto.pdf'
```

### 8. Listar Anteproyectos

```bash
curl http://localhost:8082/api/submissions/anteproyecto?page=0&size=20
```

## 📊 Monitoreo

### RabbitMQ Management UI
```
URL: http://localhost:15672
Usuario: guest
Contraseña: guest
```

### Verificar Exchanges
- `formato-a-exchange`
- `anteproyecto-exchange`
- `proyecto-exchange`

### Ver Mensajes Publicados
En RabbitMQ Management UI → Exchanges → Seleccionar exchange → Publish message

## 🛑 Detener Servicios

```bash
# Detener sin eliminar datos
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

## 🐛 Solución de Problemas

### Puerto 8082 ya en uso
```bash
# Windows
netstat -ano | findstr :8082
taskkill /PID <pid> /F

# Cambiar puerto en docker-compose.yml
ports:
  - "8083:8082"  # Host:Container
```

### PostgreSQL no conecta
```bash
# Verificar que el contenedor esté corriendo
docker-compose ps submission-postgres

# Ver logs
docker-compose logs submission-postgres

# Reiniciar
docker-compose restart submission-postgres
```

### RabbitMQ no conecta
```bash
# Verificar estado
docker-compose ps rabbitmq-local

# Reiniciar
docker-compose restart rabbitmq-local

# Verificar conectividad
docker exec rabbitmq-local rabbitmq-diagnostics ping
```

### Archivos no se guardan
```bash
# Verificar volumen
docker volume inspect submission-service_submission-uploads

# Ver contenido del volumen
docker run --rm -v submission-service_submission-uploads:/data alpine ls -la /data
```

## 📝 Notas Importantes

1. **Primera ejecución**: Puede tardar 2-3 minutos mientras descarga las imágenes Docker
2. **Health checks**: El servicio tarda ~60 segundos en estar completamente listo
3. **Logs**: Si hay errores, revisar con `docker-compose logs -f`
4. **Base de datos**: Los datos persisten en volúmenes Docker
5. **Archivos**: Se guardan en `/app/uploads` dentro del contenedor

## 🔄 Reiniciar desde Cero

```bash
# 1. Detener y eliminar todo
docker-compose down -v

# 2. Limpiar imágenes (opcional)
docker rmi submission-service

# 3. Reconstruir e iniciar
docker-compose up -d --build
```

## ✅ Checklist de Verificación

- [ ] PostgreSQL corriendo en puerto 5435
- [ ] RabbitMQ corriendo en puertos 5672 y 15672
- [ ] Submission Service respondiendo en puerto 8082
- [ ] Health check retorna status UP
- [ ] Exchanges creados en RabbitMQ
- [ ] Directorio /app/uploads creado en contenedor

## 📚 Siguientes Pasos

1. Integrar con Gateway Service
2. Integrar con Identity Service
3. Integrar con Notification Service
4. Configurar volúmenes compartidos para archivos
5. Implementar descarga de PDFs
6. Agregar validaciones adicionales

---

**¿Problemas?** Revisa el README.md principal para documentación completa.

