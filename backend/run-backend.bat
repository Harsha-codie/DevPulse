@echo off
set GEMINI_API_KEY=AIzaSyCOykb91-NpdnE4rNEHEKdORepw9GAdIuQ
set OPENWEATHER_API_KEY=4cd4b8b057cbb40195f157c3a07e3620
set IPAPI_BASE_URL=https://ipapi.co

echo Starting CropPlanner Backend...
echo.
mvn clean spring-boot:run -DskipTests
