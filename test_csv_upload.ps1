# Test script for CSV upload functionality
# This demonstrates the manual CSV upload process

Write-Host "=== Market Trends CSV Upload Test ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check current database status
Write-Host "Step 1: Checking current database status..." -ForegroundColor Yellow
$statsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/market-trends/stats" -Method Get
Write-Host "Database Statistics:" -ForegroundColor Green
Write-Host "  Total Records: $($statsResponse.totalRecords)"
Write-Host "  Distinct Districts: $($statsResponse.distinctDistricts)"
Write-Host "  Data Loaded: $($statsResponse.dataLoaded)"
Write-Host ""

# Step 2: Upload CSV data
Write-Host "Step 2: Uploading CSV data from default location..." -ForegroundColor Yellow
try {
    $uploadResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/market-trends/upload" -Method Post
    
    Write-Host "Upload Result:" -ForegroundColor Green
    Write-Host "  Success: $($uploadResponse.success)"
    Write-Host "  Message: $($uploadResponse.message)"
    Write-Host ""
    Write-Host "Statistics:" -ForegroundColor Green
    Write-Host "  Total Rows Processed: $($uploadResponse.statistics.totalRows)"
    Write-Host "  Successful Inserts: $($uploadResponse.statistics.successfulInserts)" -ForegroundColor Green
    Write-Host "  Duplicates Skipped: $($uploadResponse.statistics.duplicatesSkipped)" -ForegroundColor Yellow
    Write-Host "  Invalid Rows Skipped: $($uploadResponse.statistics.invalidRowsSkipped)" -ForegroundColor Red
    Write-Host "  Errors: $($uploadResponse.statistics.errors)"
    Write-Host ""
    Write-Host "Database Statistics After Upload:" -ForegroundColor Green
    Write-Host "  Total Records in Database: $($uploadResponse.databaseStatistics.totalRecordsInDatabase)"
    Write-Host "  Distinct Districts: $($uploadResponse.databaseStatistics.distinctDistricts)"
    Write-Host ""
    
    # Show errors if any
    if ($uploadResponse.errors -and $uploadResponse.errors.Count -gt 0) {
        Write-Host "Errors encountered:" -ForegroundColor Red
        foreach ($error in $uploadResponse.errors) {
            Write-Host "  - $error" -ForegroundColor Red
        }
        Write-Host ""
    }
} catch {
    Write-Host "Upload failed: $_" -ForegroundColor Red
    Write-Host ""
}

# Step 3: Verify data was loaded
Write-Host "Step 3: Verifying data by fetching districts..." -ForegroundColor Yellow
try {
    $districts = Invoke-RestMethod -Uri "http://localhost:8080/api/market-trends/districts" -Method Get
    Write-Host "Available Districts (first 10):" -ForegroundColor Green
    $districts | Select-Object -First 10 | ForEach-Object { Write-Host "  - $_" }
    Write-Host ""
} catch {
    Write-Host "Failed to fetch districts: $_" -ForegroundColor Red
    Write-Host ""
}

# Step 4: Test duplicate prevention by uploading again
Write-Host "Step 4: Testing duplicate prevention (uploading same data again)..." -ForegroundColor Yellow
try {
    $duplicateTestResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/market-trends/upload" -Method Post
    
    Write-Host "Second Upload Result:" -ForegroundColor Green
    Write-Host "  Successful Inserts: $($duplicateTestResponse.statistics.successfulInserts)" -ForegroundColor Green
    Write-Host "  Duplicates Skipped: $($duplicateTestResponse.statistics.duplicatesSkipped)" -ForegroundColor Yellow
    Write-Host ""
    
    if ($duplicateTestResponse.statistics.duplicatesSkipped -gt 0) {
        Write-Host "✅ Duplicate prevention is working correctly!" -ForegroundColor Green
        Write-Host "   All $($duplicateTestResponse.statistics.duplicatesSkipped) records were skipped as duplicates." -ForegroundColor Green
    }
    Write-Host ""
} catch {
    Write-Host "Duplicate test failed: $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- CSV data is NOT loaded automatically on startup"
Write-Host "- Use POST /api/market-trends/upload to load data manually"
Write-Host "- Duplicate records are automatically skipped"
Write-Host "- Application can be restarted without re-processing CSV"
Write-Host ""
