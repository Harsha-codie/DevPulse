@echo off
echo ========================================
echo   Starting CropPlanner Application
echo ========================================
echo.
echo [1/2] Starting Backend Server...
start "CropPlanner Backend" cmd /k "cd /d D:\CropPlannerApp\backend && mvn org.springframework.boot:spring-boot-maven-plugin:run "-Dspring-boot.run.jvmArguments=-Dgemini.api.key=AIzaSyCOykb91-NpdnE4rNEHEKdORepw9GAdIuQ -Dopenweather.api-key=4cd4b8b057cbb40195f157c3a07e3620 -Dipapi.base-url=https://ipapi.co""

echo Waiting 30 seconds for backend to initialize...
timeout /t 30 /nobreak > nul

echo.
echo [2/2] Starting Frontend Server...
start "CropPlanner Frontend" cmd /k "cd /d D:\CropPlannerApp\frontend && npm run dev"

echo.
echo ========================================
echo   Servers Starting Successfully!
echo ========================================
echo.
echo Backend API:  http://localhost:8080
echo Frontend UI:  http://localhost:5173
echo.
echo IMPORTANT: 
echo - Wait 1-2 minutes for both servers to fully start
echo - Backend loads 97,000+ market trend records
echo - Do NOT close the command windows that open
echo.
echo Open http://localhost:5173 in your browser
echo ========================================
echo.
pause
