# Query the database to check if user exists
$pghost = "46.224.13.157"
$pgport = "5432"
$pgdatabase = "postgres"
$pguser = "postgres"
$pgpassword = "emYqpMHL+p2nv1i0QOI8uTlt3m7CaQaz"

# Connection string for PostgreSQL
$connectionString = "Host=$pghost;Port=$pgport;Database=$pgdatabase;Username=$pguser;Password=$pgpassword"

# Try to query using PowerShell (requires Npgsql if not using psql)
# Alternative: Use SQL via a temp file

# Create a simple PowerShell script using System.Data.Odbc or invoke SQL through other means
# For now, let's try using the Npgsql .NET provider if available

try {
    # Load the Npgsql assembly if available
    [void][System.Reflection.Assembly]::LoadWithPartialName("Npgsql")
    
    $connection = New-Object Npgsql.NpgsqlConnection($connectionString)
    $connection.Open()
    
    $command = $connection.CreateCommand()
    $command.CommandText = "SELECT id, email, full_name, external_provider_id, created_at FROM app_users ORDER BY created_at DESC LIMIT 10;"
    
    $reader = $command.ExecuteReader()
    
    Write-Host "Users in app_users table:" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    
    while ($reader.Read()) {
        Write-Host "ID: $($reader['id'])"
        Write-Host "Email: $($reader['email'])"
        Write-Host "Full Name: $($reader['full_name'])"
        Write-Host "External Provider ID: $($reader['external_provider_id'])"
        Write-Host "Created At: $($reader['created_at'])"
        Write-Host "--------------------------------"
    }
    
    $reader.Close()
    $connection.Close()
}
catch {
    Write-Host "Error querying database: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "This likely means Npgsql is not installed." -ForegroundColor Yellow
}
