@echo off
echo Starting CropPlanner Application...
echo.
echo Starting Backend Server (Using JAR)...
start "CropPlanner Backend" cmd /k "cd /d D:\CropPlannerApp\backend && java -jar target\backend-0.1.0.jar --gemini.api.key=AIzaSyCOykb91-NpdnE4rNEHEKdORepw9GAdIuQ --openweather.api-key=4cd4b8b057cbb40195f157c3a07e3620 --ipapi.base-url=https://ipapi.co"

echo Waiting 30 seconds for backend to start...
timeout /t 30 /nobreak

echo.
echo Starting Frontend Server...
start "CropPlanner Frontend" cmd /k "cd /d D:\CropPlannerApp\frontend && npm run dev"

echo.
echo ========================================
echo Both servers are starting!
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo.
echo Open http://localhost:5173 in your browser
echo ========================================
echo.
pause
