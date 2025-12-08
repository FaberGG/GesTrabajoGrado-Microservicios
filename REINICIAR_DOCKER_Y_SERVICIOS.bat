@echo off
echo ========================================
echo REINICIANDO DOCKER Y TODOS LOS SERVICIOS
echo ========================================
echo.

echo [1/6] Cerrando Docker Desktop...
taskkill /IM "Docker Desktop.exe" /F 2>nul
timeout /t 3 /nobreak > nul

echo [2/6] Deteniendo WSL...
wsl --shutdown
timeout /t 5 /nobreak docker> nul

echo [3/6] Iniciando Docker Desktop...
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
echo Esperando 45 segundos a que Docker Desktop inicie completamente...
timeout /t 45 /nobreak

echo.
echo [4/6] Verificando que Docker responda...
docker ps
if errorlevel 1 (
    echo ERROR: Docker no responde aun. Espera 30 segundos mas y ejecuta este script de nuevo.
    pause
    exit /b 1
)

echo.
echo [5/6] Iniciando todos los servicios...
docker-compose up -d

echo.
echo [6/6] Esperando 30 segundos a que los servicios inicien...
timeout /t 30 /nobreak

echo.
echo ========================================
echo VERIFICANDO SERVICIOS
echo ========================================
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo ========================================
echo PROBANDO ENDPOINTS
echo ========================================
echo.
echo Gateway (puerto 8080):
curl -s http://localhost:8080/actuator/health
echo.
echo.
echo Submission (puerto 8082):
curl -s http://localhost:8082/actuator/health
echo.
echo.
echo ========================================
echo URLS DISPONIBLES:
echo ========================================
echo.
echo ** SWAGGER UI / DOCUMENTACION **
echo Submission Service: http://localhost:8082/swagger-ui/index.html
echo.
echo ** API DIRECTAS **
echo Gateway Health: http://localhost:8080/actuator/health
echo Submission API: http://localhost:8082/api/submissions
echo.
echo ** OPENAPI JSON **
echo Submission OpenAPI: http://localhost:8082/v3/api-docs
echo.
echo ========================================
echo LISTO! Prueba ahora en Postman
echo ========================================
pause

