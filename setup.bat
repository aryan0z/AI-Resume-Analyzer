@echo off
REM AI Resume Analyzer - Setup Script for Windows

echo ================================
echo AI Resume Analyzer Setup
echo ================================
echo.

REM Check Java version
echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| find "version"') do (
    set JAVA_VERSION=%%i
    echo Java version found: %JAVA_VERSION%
)

REM Check Maven
echo Checking Maven installation...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed
    exit /b 1
)
echo Maven found
echo.

REM Build project
echo Building project with Maven...
call mvn clean install

if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo.
echo ================================================
echo Build successful!
echo ================================================
echo.
echo Next steps:
echo 1. Edit src\main\resources\application.properties
echo 2. Set your Gemini API key
echo 3. Run: mvn spring-boot:run
echo 4. Access: http://localhost:8080
echo.
