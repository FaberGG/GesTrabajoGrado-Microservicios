@echo off
echo ========================================
echo Compilando submission-service
echo ========================================
cd "C:\Users\jhonn\OneDrive\Almacen\Repositorios\PROYECTO SOFTWARE II\GesTrabajoGrado-Microservicios\submission-service"
call mvn clean package -DskipTests
echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo COMPILACION EXITOSA
    echo ========================================
) else (
    echo ========================================
    echo ERROR EN LA COMPILACION
    echo ========================================
)
pause

