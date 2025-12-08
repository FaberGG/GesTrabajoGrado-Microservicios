@echo off
echo ========================================
echo RECONSTRUYENDO SUBMISSION-SERVICE
echo Y APLICANDO CORRECCION DE SWAGGER
echo ========================================
echo.

echo [1/5] Deteniendo contenedores...
docker-compose down

echo.
echo [2/5] Reconstruyendo imagen Docker de submission-service...
echo (Docker hara la compilacion internamente)
docker-compose build --no-cache submission-service
if errorlevel 1 (
    echo ERROR: Fallo la construccion de la imagen Docker
    pause
    exit /b 1
)

echo.
echo [4/5] Iniciando todos los servicios...
docker-compose up -d

echo.
echo [5/5] Esperando 30 segundos a que los servicios inicien...
timeout /t 30 /nobreak

echo.
echo ========================================
echo VERIFICACION DE SERVICIOS
echo ========================================
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo ========================================
echo PROBANDO SWAGGER
echo ========================================
echo.
echo Probando OpenAPI JSON...
curl -s http://localhost:8082/v3/api-docs | findstr "openapi"
echo.

echo.
echo ========================================
echo URLS CORREGIDAS DE SWAGGER:
echo ========================================
echo.
echo ** SWAGGER UI **
echo http://localhost:8082/swagger-ui/index.html
echo.
echo ** OpenAPI JSON **
echo http://localhost:8082/v3/api-docs
echo.
echo ** NOTA IMPORTANTE **
echo El Gateway (8080) NO tiene Swagger UI porque
echo es solo un proxy/router. La documentacion esta
echo en cada microservicio individual.
echo.
echo ========================================
echo LISTO! Abre el navegador y prueba las URLs
echo ========================================
pause

