# Test User Sync Script
# This script authenticates with Keycloak and calls the /api/auth/me endpoint
# which triggers the UserSyncFilter to sync the user to the database

# Configuration
$KEYCLOAK_URL = "http://localhost:8080"
$REALM = "meetlines"
$CLIENT_ID = "meetlines-backend"
$CLIENT_SECRET = "VG88v8kzshAte09I3zUtJUxgX9jkY5eN"
$USERNAME = "tomas123"
$PASSWORD = "tom123"
$APP_URL = "http://localhost:8081"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "User Sync Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Get JWT Token from Keycloak
Write-Host "Step 1: Getting JWT token from Keycloak..." -ForegroundColor Yellow

$tokenEndpoint = "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token"
$tokenBody = @{
    client_id     = $CLIENT_ID
    client_secret = $CLIENT_SECRET
    username      = $USERNAME
    password      = $PASSWORD
    grant_type    = "password"
}

try {
    $tokenResponse = Invoke-RestMethod -Uri $tokenEndpoint -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
    $accessToken = $tokenResponse.access_token
    Write-Host "✓ Token obtained successfully" -ForegroundColor Green
    Write-Host "  Token preview: $($accessToken.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get token: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 2: Call /api/auth/me with the token
Write-Host "Step 2: Calling /api/auth/me endpoint..." -ForegroundColor Yellow

$meEndpoint = "$APP_URL/api/auth/me"
$headers = @{
    Authorization = "Bearer $accessToken"
    "Content-Type" = "application/json"
}

try {
    $userResponse = Invoke-RestMethod -Uri $meEndpoint -Method Get -Headers $headers
    Write-Host "✓ Successfully retrieved user from database" -ForegroundColor Green
    Write-Host ""
    Write-Host "User Data:" -ForegroundColor Cyan
    $userResponse | ConvertTo-Json | Write-Host -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get user: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "User sync test completed!" -ForegroundColor Green
Write-Host "The user should now be visible in the app_users table." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
