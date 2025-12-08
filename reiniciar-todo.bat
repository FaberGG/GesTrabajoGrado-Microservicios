@echo off
echo ========================================
echo REINICIANDO TODOS LOS MICROSERVICIOS
echo ========================================
echo.

cd /d C:\Users\USUARIO\Downloads\GesTrabajoGrado-Microservicios

echo [1/5] Deteniendo todos los contenedores...
docker-compose down -v
timeout /t 3 /nobreak >nul

echo.
echo [2/5] Limpiando volumenes antiguos...
docker volume prune -f
timeout /t 2 /nobreak >nul

echo.
echo [3/5] Reconstruyendo submission-service...
docker-compose build --no-cache submission
timeout /t 2 /nobreak >nul

echo.
echo [4/5] Levantando todos los servicios...
docker-compose up -d

echo.
echo [5/5] Esperando 60 segundos para que los servicios se inicien...
timeout /t 60 /nobreak

echo.
echo ========================================
echo VERIFICANDO ESTADO DE LOS SERVICIOS
echo ========================================
docker-compose ps

echo.
echo ========================================
echo VERIFICANDO LOGS DEL SUBMISSION-SERVICE
echo ========================================
docker logs submission-service --tail 30

echo.
echo ========================================
echo VERIFICANDO GATEWAY (Puerto 8080)
echo ========================================
curl -s http://localhost:8080/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ✅ GATEWAY DISPONIBLE EN http://localhost:8080
) else (
    echo.
    echo ❌ GATEWAY NO DISPONIBLE - Revisando logs...
    docker logs gateway-service --tail 20
)

echo.
echo ========================================
echo COMPLETADO!
echo ========================================
pause

