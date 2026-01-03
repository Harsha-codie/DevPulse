@echo off
echo Starting Backend...
start "Backend" cmd /k "cd /d D:\CropPlannerApp\backend && set GEMINI_API_KEY=AIzaSyCOykb91-NpdnE4rNEHEKdORepw9GAdIuQ && set OPENWEATHER_API_KEY=4cd4b8b057cbb40195f157c3a07e3620 && mvn spring-boot:run -DskipTests"

timeout /t 5 /nobreak >nul

echo Starting Frontend...
start "Frontend" cmd /k "cd /d D:\CropPlannerApp\frontend && npm run dev"

echo.
echo Both servers starting...
echo Backend will be on: http://localhost:8080
echo Frontend will be on: http://localhost:5173
echo.
echo Wait 60 seconds then open http://localhost:5173
pause
