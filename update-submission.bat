@echo off
echo ================================================
echo  Actualizando Submission Service
echo ================================================
echo.

echo [1/3] Deteniendo el servicio actual...
docker-compose stop submission

echo.
echo [2/3] Reconstruyendo la imagen de Docker...
docker-compose build submission

echo.
echo [3/3] Iniciando el servicio actualizado...
docker-compose up -d submission

echo.
echo ================================================
echo  Actualizacion completada!
echo ================================================
echo.
echo Para ver los logs en tiempo real, ejecuta:
echo docker logs submission-service -f
echo.
pause

