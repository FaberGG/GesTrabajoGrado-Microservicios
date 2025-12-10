@echo off
echo ========================================
echo LIMPIANDO BASE DE DATOS SUBMISSION
echo ========================================
echo.
echo ADVERTENCIA: Esto eliminara TODOS los datos del submission-service
echo Presiona Ctrl+C para cancelar, o
pause
echo.

echo Deteniendo submission-service...
docker-compose stop submission

echo Eliminando contenedor submission-service...
docker-compose rm -f submission

echo Eliminando volumen de base de datos submission...
docker volume rm gestrabajogrado-microservicios_postgres-submission-data 2>nul

echo Recreando base de datos...
docker-compose up -d postgres-submission

echo Esperando que PostgreSQL este listo...
timeout /t 10 /nobreak >nul

echo Reconstruyendo submission-service...
docker-compose build --no-cache submission

echo Iniciando submission-service...
docker-compose up -d submission

echo.
echo ========================================
echo COMPLETADO!
echo ========================================
echo.
echo Revisa los logs con: docker-compose logs -f submission

