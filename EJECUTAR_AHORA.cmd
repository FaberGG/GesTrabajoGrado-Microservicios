@echo off
chcp 65001 >nul
cls

echo ========================================
echo CORRIGIENDO SWAGGER - EJECUTANDO...
echo ========================================
echo.

cd /d D:\GesTrabajoGrado-Microservicios

echo [1/4] Deteniendo contenedores...
docker-compose down
echo.

echo [2/4] Reconstruyendo submission-service...
docker-compose build --no-cache submission-service
echo.

echo [3/4] Iniciando servicios...
docker-compose up -d
echo.

echo [4/4] Esperando 35 segundos para que inicien...
timeout /t 35 /nobreak
echo.

echo ========================================
echo ESTADO:
echo ========================================
docker ps --format "table {{.Names}}\t{{.Status}}"
echo.

echo ========================================
echo PROBANDO SWAGGER...
echo ========================================
timeout /t 5 /nobreak >nul
echo.
echo Probando: http://localhost:8082/v3/api-docs
curl -s http://localhost:8082/v3/api-docs | findstr "openapi"
echo.

echo ========================================
echo ABRE EN TU NAVEGADOR:
echo ========================================
echo.
echo http://localhost:8082/swagger-ui/index.html
echo.
echo ========================================
pause

