# 🚀 INICIO RÁPIDO

## ¿Cómo iniciar todos los servicios?

### Opción 1: Usando el script (MÁS FÁCIL)
```bash
# Doble clic en:
start.bat
```

### Opción 2: Comandos manuales
```bash
# 1. Copiar variables de entorno
copy .env.example .env

# 2. Editar .env con tus valores
notepad .env

# 3. Iniciar todos los servicios
docker-compose up -d

# 4. Ver estado
docker-compose ps

# 5. Ver logs
docker-compose logs -f
```

## ¿Cómo detener los servicios?

### Opción 1: Usando el script
```bash
# Doble clic en:
stop.bat
```

### Opción 2: Comando manual
```bash
# Detener (mantener datos)
docker-compose down

# Detener y eliminar datos
docker-compose down -v
```

## URLs de los servicios

- **Gateway**: http://localhost:8080
- **Identity**: http://localhost:8081
- **Submission**: http://localhost:8082
- **Notification**: http://localhost:8083
- **RabbitMQ**: http://localhost:15672

## Comandos útiles

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f gateway

# Reiniciar un servicio
docker-compose restart gateway

# Reconstruir un servicio
docker-compose up -d --build gateway

# Ver estado de servicios
docker-compose ps

# Entrar a un contenedor
docker-compose exec gateway sh
```

## Verificar que todo funciona

1. Espera 1-2 minutos después de iniciar
2. Visita: http://localhost:8080/api/gateway/health
3. Deberías ver: `{"status":"UP"}`

## Problemas comunes

### "Puerto ya en uso"
```bash
# Ver qué proceso usa el puerto
netstat -ano | findstr "8080"

# Detener servicios antiguos
docker-compose down
```

### "Contenedor no inicia"
```bash
# Ver logs del servicio con problemas
docker-compose logs identity

# Reiniciar el servicio
docker-compose restart identity
```

### "Error de base de datos"
- Verifica que las credenciales en `.env` sean correctas
- Espera 30-60 segundos para que PostgreSQL inicie completamente
- Revisa logs: `docker-compose logs postgres-identity`

## 📝 Checklist antes de iniciar

- [ ] Docker Desktop está corriendo
- [ ] Archivo `.env` existe y está configurado
- [ ] Puertos 8080-8083 están disponibles
- [ ] Puertos 5432-5434 están disponibles
- [ ] Tienes al menos 4GB RAM disponible

## 🎯 Siguiente paso

Lee el archivo **README.md** para documentación completa

