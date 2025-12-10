@REM Maven Wrapper script for Windows
@REM This script downloads Maven if needed and runs it

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

@REM Find java.exe
if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

@REM Check if java exists
"%JAVA_EXE%" -version >nul 2>&1
if errorlevel 1 (
    echo Error: JAVA_HOME is not set and java.exe is not in PATH
    exit /b 1
)

@REM Run Maven Wrapper
"%JAVA_EXE%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -jar "%WRAPPER_JAR%" %*

endlocal

