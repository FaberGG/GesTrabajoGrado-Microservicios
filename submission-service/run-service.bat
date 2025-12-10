@echo off
echo ========================================
echo   Iniciando Submission Service
echo ========================================
echo.

REM Verificar si existe Maven en el directorio local
if not exist "%~dp0maven\bin\mvn.cmd" (
    echo Descargando Maven 3.9.6...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%~dp0maven.zip'"

    echo Extrayendo Maven...
    powershell -Command "Expand-Archive -Path '%~dp0maven.zip' -DestinationPath '%~dp0' -Force"
    rename "%~dp0apache-maven-3.9.6" maven
    del "%~dp0maven.zip"
    echo Maven instalado correctamente!
)

echo.
echo Ejecutando Spring Boot...
echo Swagger UI estara disponible en: http://localhost:8082/swagger-ui.html
echo.

"%~dp0maven\bin\mvn.cmd" spring-boot:run -DskipTests

pause

