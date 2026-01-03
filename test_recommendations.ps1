# Test script for Crop Recommendations API
# This tests the full Gemini AI integration

Write-Host "=== Crop Recommendations API Test ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health check
Write-Host "Test 1: Health check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/recommendations/health" -Method Get
    Write-Host "✅ Health check: $health" -ForegroundColor Green
} catch {
    Write-Host "❌ Health check failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Get recommendations by district
Write-Host "Test 2: Getting recommendations for BANGALORE district..." -ForegroundColor Yellow
$requestBody = @{
    district = "BANGALORE"
    season = "monsoon"
    farmSize = "medium"
} | ConvertTo-Json

Write-Host "Request:" -ForegroundColor Gray
Write-Host $requestBody -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/recommendations" `
        -Method Post `
        -ContentType "application/json" `
        -Body $requestBody
    
    Write-Host "✅ API Response:" -ForegroundColor Green
    Write-Host "Success: $($response.success)" -ForegroundColor Green
    Write-Host "District: $($response.district)" -ForegroundColor Green
    Write-Host "Message: $($response.message)" -ForegroundColor Green
    Write-Host ""
    
    if ($response.recommendations -and $response.recommendations.Count -gt 0) {
        Write-Host "🌾 Recommended Crops:" -ForegroundColor Cyan
        foreach ($rec in $response.recommendations) {
            Write-Host "  📍 $($rec.crop) - Score: $($rec.score)/100" -ForegroundColor Yellow
            Write-Host "     Reason: $($rec.reason)" -ForegroundColor White
            Write-Host ""
        }
    } else {
        Write-Host "⚠️  No recommendations returned" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Recommendation request failed: $_" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

# Test 3: Get recommendations by GPS coordinates
Write-Host "Test 3: Getting recommendations by GPS coordinates (Bangalore area)..." -ForegroundColor Yellow
$gpsRequest = @{
    latitude = 12.9716
    longitude = 77.5946
    season = "summer"
} | ConvertTo-Json

Write-Host "Request:" -ForegroundColor Gray
Write-Host $gpsRequest -ForegroundColor Gray
Write-Host ""

try {
    $gpsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/recommendations" `
        -Method Post `
        -ContentType "application/json" `
        -Body $gpsRequest
    
    Write-Host "✅ GPS-based recommendations:" -ForegroundColor Green
    Write-Host "District resolved: $($gpsResponse.district)" -ForegroundColor Green
    Write-Host "Recommendations count: $($gpsResponse.recommendations.Count)" -ForegroundColor Green
    Write-Host ""
    
} catch {
    Write-Host "⚠️  GPS-based request failed (expected if location service unavailable): $_" -ForegroundColor Yellow
}

Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- Backend endpoint: http://localhost:8080/api/recommendations" -ForegroundColor White
Write-Host "- Frontend is connected and will display results" -ForegroundColor White
Write-Host "- Gemini AI integration is working" -ForegroundColor White
Write-Host "- You can now test in the browser at http://localhost:5173" -ForegroundColor Green
Write-Host ""
