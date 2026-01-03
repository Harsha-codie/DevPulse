# Change to the backend directory
Set-Location -Path $PSScriptRoot

# Load environment variables from .env file
$envFile = Join-Path (Split-Path $PSScriptRoot -Parent) ".env"

if (Test-Path $envFile) {
    Write-Host "Loading environment variables from .env file..." -ForegroundColor Green
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]*)\s*=\s*(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "  Loaded: $name" -ForegroundColor Cyan
        }
    }
} else {
    Write-Host "Warning: .env file not found at $envFile" -ForegroundColor Yellow
}

Write-Host "`nStarting Spring Boot backend..." -ForegroundColor Green
mvn spring-boot:run -DskipTests
