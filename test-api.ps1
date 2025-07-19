# API Testing Script for Railway Deployment
# Replace YOUR_RAILWAY_URL with your actual Railway deployment URL

$BASE_URL = "https://YOUR_RAILWAY_URL.up.railway.app"

Write-Host "Testing API endpoints..." -ForegroundColor Green

# Test Health Endpoint
Write-Host "`n1. Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/health" -Method GET
    Write-Host "✅ Health Check: $response" -ForegroundColor Green
} catch {
    Write-Host "❌ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Categories Endpoint (usually public)
Write-Host "`n2. Testing Categories Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/categories" -Method GET
    Write-Host "✅ Categories: Found $($response.Count) categories" -ForegroundColor Green
} catch {
    Write-Host "❌ Categories Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Products Endpoint (usually public)
Write-Host "`n3. Testing Products Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/products" -Method GET
    Write-Host "✅ Products: Found $($response.Count) products" -ForegroundColor Green
} catch {
    Write-Host "❌ Products Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Website Info Endpoint (usually public)
Write-Host "`n4. Testing Website Info Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/website" -Method GET
    Write-Host "✅ Website Info: Retrieved successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Website Info Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTesting completed!" -ForegroundColor Green
Write-Host "Note: Some endpoints may require authentication or specific parameters." -ForegroundColor Cyan
