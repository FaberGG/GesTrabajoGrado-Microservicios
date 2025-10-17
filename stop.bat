@echo off
REM ========================================
REM Script de Detención - Sistema de Trabajo de Grado
REM ========================================

echo.
echo ================================================
echo  Deteniendo servicios...
echo ================================================
echo.

echo ¿Deseas eliminar tambien los volumenes (datos)? (S/N)
echo   S = Limpieza completa (se pierden datos de BD)
echo   N = Solo detener (datos se mantienen)
set /p ELIMINAR=

if /i "%ELIMINAR%"=="S" (
    echo.
    echo [ADVERTENCIA] Se eliminaran todos los volumenes y datos
    echo ¿Estas seguro? (S/N)
    set /p CONFIRMAR=
    if /i "%CONFIRMAR%"=="S" (
        docker-compose down -v
        echo.
        echo [OK] Servicios detenidos y volumenes eliminados
    ) else (
        docker-compose down
        echo.
        echo [OK] Servicios detenidos (datos preservados)
    )
) else (
    docker-compose down
    echo.
    echo [OK] Servicios detenidos (datos preservados)
)

echo.
pause
