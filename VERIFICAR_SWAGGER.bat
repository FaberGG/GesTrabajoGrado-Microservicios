@echo off
echo ========================================
echo VERIFICACION DE SWAGGER Y DOCUMENTACION
echo ========================================
echo.

echo [1] Verificando Submission Service - Health Check...
curl -s http://localhost:8082/actuator/health
echo.
echo.

echo [2] Verificando OpenAPI JSON (debe mostrar JSON valido)...
curl -s http://localhost:8082/v3/api-docs | findstr "openapi"
echo.
echo.

echo ========================================
echo URLS PARA ABRIR EN EL NAVEGADOR:
echo ========================================
echo.
echo 1. Swagger UI Submission Service:
echo    http://localhost:8082/swagger-ui/index.html
echo.
echo 2. OpenAPI JSON:
echo    http://localhost:8082/v3/api-docs
echo.
echo 3. Gateway Health:
echo    http://localhost:8080/actuator/health
echo.
echo ========================================
echo NOTA: El Gateway NO tiene Swagger UI
echo porque es solo un proxy/enrutador.
echo ========================================
echo.
pause

