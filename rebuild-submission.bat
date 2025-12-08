@echo off
echo ========================================
echo Reconstruyendo Submission Service
echo ========================================

echo.
echo [1/3] Deteniendo el servicio actual...
docker-compose stop submission

echo.
echo [2/3] Reconstruyendo la imagen Docker...
docker-compose build submission

echo.
echo [3/3] Iniciando el servicio actualizado...
docker-compose up -d submission

echo.
echo ========================================
echo Proceso completado!
echo ========================================
echo.
echo Esperando 10 segundos para que el servicio inicie...
timeout /t 10 /nobreak

echo.
echo Verificando logs del servicio...
docker-compose logs --tail=50 submission

echo.
echo ========================================
echo Para acceder a Swagger UI:
echo http://localhost:8082/swagger-ui/index.html
echo.
echo API Docs JSON:
echo http://localhost:8082/v3/api-docs
echo ========================================

