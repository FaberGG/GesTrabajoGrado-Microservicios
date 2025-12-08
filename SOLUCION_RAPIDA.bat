@echo off
echo ========================================
echo SOLUCIONANDO TODOS LOS PROBLEMAS
echo ========================================
echo.

echo [1/5] Deteniendo servicios...
docker-compose down

echo.
echo [2/5] Esperando 5 segundos...
timeout /t 5 /nobreak > nul

echo.
echo [3/5] Iniciando servicios...
docker-compose up -d

echo.
echo [4/5] Esperando 30 segundos a que todo inicie...
timeout /t 30 /nobreak

echo.
echo [5/5] Verificando estado de servicios...
echo.
echo === SERVICIOS ===
docker-compose ps

echo.
echo === PROBANDO ENDPOINTS ===
echo.
echo Gateway (puerto 8080):
curl -s http://localhost:8080/actuator/health
echo.
echo.
echo Identity (puerto 8081):
curl -s http://localhost:8081/actuator/health
echo.
echo.
echo Submission (puerto 8082):
curl -s http://localhost:8082/actuator/health
echo.
echo.
echo ========================================
echo URLS IMPORTANTES:
echo ========================================
echo Gateway Swagger: http://localhost:8080/swagger-ui.html
echo Submission Swagger: http://localhost:8082/swagger-ui/index.html
echo Identity Swagger: http://localhost:8081/swagger-ui.html
echo.
echo ========================================
echo LISTO! Intenta ahora con Postman
echo ========================================
pause

