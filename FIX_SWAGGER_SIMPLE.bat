@echo off
chcp 65001 >nul
cls

echo ========================================
echo APLICANDO CORRECCION DE SWAGGER
echo ========================================
echo.

echo [Paso 1/4] Deteniendo contenedores...
cd /d D:\GesTrabajoGrado-Microservicios
docker-compose down
echo.

echo [Paso 2/4] Reconstruyendo submission-service (sin cache)...
docker-compose build --no-cache submission-service
if errorlevel 1 (
    echo.
    echo ERROR: La construccion fallo
    echo.
    pause
    exit /b 1
)
echo.

echo [Paso 3/4] Iniciando todos los servicios...
docker-compose up -d
echo.

echo [Paso 4/4] Esperando 30 segundos...
timeout /t 30 /nobreak
echo.

echo ========================================
echo ESTADO DE LOS CONTENEDORES:
echo ========================================
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

echo ========================================
echo VERIFICANDO SWAGGER...
echo ========================================
timeout /t 5 /nobreak >nul
curl -s http://localhost:8082/v3/api-docs | findstr "openapi" >nul
if errorlevel 1 (
    echo [X] Swagger aun no responde, espera un poco mas
) else (
    echo [OK] Swagger esta funcionando!
)
echo.

echo ========================================
echo LISTO! USA ESTAS URLS:
echo ========================================
echo.
echo Swagger UI:
echo   http://localhost:8082/swagger-ui/index.html
echo.
echo OpenAPI JSON:
echo   http://localhost:8082/v3/api-docs
echo.
echo Gateway:
echo   http://localhost:8080/actuator/health
echo.
echo ========================================
pause

