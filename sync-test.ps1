$KEYCLOAK_URL = "http://localhost:8080"
$REALM = "meetlines"
$CLIENT_ID = "meetlines-backend"
$CLIENT_SECRET = "VG88v8kzshAte09I3zUtJUxgX9jkY5eN"
$USERNAME = "tomas123"
$PASSWORD = "tom123"
$APP_URL = "http://localhost:8081"

Write-Host "Getting JWT token..." -ForegroundColor Cyan

$tokenEndpoint = "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token"
$tokenBody = @{
    client_id     = $CLIENT_ID
    client_secret = $CLIENT_SECRET
    username      = $USERNAME
    password      = $PASSWORD
    grant_type    = "password"
}

$tokenResponse = Invoke-RestMethod -Uri $tokenEndpoint -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
$accessToken = $tokenResponse.access_token

Write-Host "Token obtained!" -ForegroundColor Green
Write-Host ""

Write-Host "Calling /api/auth/me..." -ForegroundColor Cyan

$headers = @{
    Authorization = "Bearer $accessToken"
}

$userResponse = Invoke-RestMethod -Uri "$APP_URL/api/auth/me" -Method Get -Headers $headers

Write-Host "User data from database:" -ForegroundColor Green
$userResponse | ConvertTo-Json
