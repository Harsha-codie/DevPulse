@echo off
echo Loading environment variables from .env file...

REM Read .env file and set environment variables
for /f "usebackq tokens=1,2 delims==" %%a in ("..\\.env") do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" (
        set "%%a=%%b"
        echo   Loaded: %%a
    )
)

echo.
echo Starting Spring Boot backend...
mvn spring-boot:run -DskipTests
