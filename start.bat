@echo off
REM ========================================
REM Script de Inicio - Sistema de Trabajo de Grado
REM ========================================

echo.
echo ================================================
echo  Sistema de Gestion de Trabajo de Grado
echo  Microservicios - Docker Compose
echo ================================================
echo.

REM Verificar si Docker esta corriendo
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker no esta corriendo o no esta instalado.
    echo Por favor, inicia Docker Desktop y ejecuta este script nuevamente.
    pause
    exit /b 1
)

echo [OK] Docker esta corriendo
echo.

REM Verificar si existe .env
if not exist .env (
    echo [ADVERTENCIA] Archivo .env no encontrado
    echo.
    echo Copiando .env.example a .env...
    copy .env.example .env
    echo.
    echo [IMPORTANTE] Por favor, edita el archivo .env con tus configuraciones reales:
    echo   - JWT_SECRET (minimo 32 caracteres)
    echo   - Credenciales de bases de datos
    echo   - Configuracion SMTP para emails
    echo.
    echo ¿Deseas editar el archivo .env ahora? (S/N)
    set /p EDITAR=
    if /i "%EDITAR%"=="S" (
        notepad .env
    )
)

echo.
echo ================================================
echo  Iniciando servicios...
echo ================================================
echo.

REM Iniciar servicios
docker-compose up -d

echo.
echo ================================================
echo  Estado de los servicios
echo ================================================
echo.

REM Esperar un poco para que los contenedores inicien
timeout /t 5 /nobreak >nul

docker-compose ps

echo.
echo ================================================
echo  URLs de los servicios
echo ================================================
echo.
echo  Gateway:         http://localhost:8080
echo  Identity:        http://localhost:8081
echo  Submission:      http://localhost:8082
echo  Notification:    http://localhost:8083
echo  RabbitMQ UI:     http://localhost:15672
echo.
echo ================================================
echo  Comandos utiles
echo ================================================
echo.
echo  Ver logs:              docker-compose logs -f
echo  Ver logs (servicio):   docker-compose logs -f gateway
echo  Detener servicios:     docker-compose down
echo  Reiniciar servicios:   docker-compose restart
echo  Estado:                docker-compose ps
echo.
echo ================================================
echo.

pause

