# ====================================================================
# DigitalFix - Script para inyectar datos iniciales en PostgreSQL
# ====================================================================

$psqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
if (-not (Test-Path $psqlPath)) {
    $found = Get-ChildItem -Path "C:\Program Files\PostgreSQL" -Recurse -Filter "psql.exe" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
    if ($found) {
        $psqlPath = $found
    } else {
        Write-Error "No se encontró psql.exe en el sistema."
        exit 1
    }
}

$env:PGPASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "abc123" }
$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }
$dbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "postgres" }

Write-Host "Iniciando inyección de datos para DigitalFix..." -ForegroundColor Cyan

# Comprobar si existe la base de datos unificada 'digitalfix' (Docker) o las 3 bases individuales
$existingDbs = & $psqlPath -U $dbUser -h $dbHost -p $dbPort -d postgres -t -c "SELECT datname FROM pg_database WHERE datname IN ('digitalfix', 'digitalfix_catalog_db', 'digitalfix_technicians_db', 'digitalfix_workorders_db');"

if ($existingDbs -match "\bdigitalfix\b") {
    Write-Host "-> Inyectando seeds en base de datos unificada 'digitalfix'..." -ForegroundColor Yellow
    & $psqlPath -U $dbUser -h $dbHost -p $dbPort -d digitalfix -f "$PSScriptRoot\init-seeds.sql"
}

if ($existingDbs -match "\bdigitalfix_technicians_db\b") {
    Write-Host "-> Inyectando técnicos en 'digitalfix_technicians_db'..." -ForegroundColor Yellow
    & $psqlPath -U $dbUser -h $dbHost -p $dbPort -d digitalfix_technicians_db -f "$PSScriptRoot\ms-digitalfix-technicians\seed-technicians.sql"
}

if ($existingDbs -match "\bdigitalfix_catalog_db\b") {
    Write-Host "-> Inyectando catálogo y repuestos en 'digitalfix_catalog_db'..." -ForegroundColor Yellow
    & $psqlPath -U $dbUser -h $dbHost -p $dbPort -d digitalfix_catalog_db -f "$PSScriptRoot\ms-digitalfix-catalog\seed-catalog.sql"
}

if ($existingDbs -match "\bdigitalfix_workorders_db\b") {
    Write-Host "-> Inyectando órdenes de trabajo en 'digitalfix_workorders_db'..." -ForegroundColor Yellow
    & $psqlPath -U $dbUser -h $dbHost -p $dbPort -d digitalfix_workorders_db -f "$PSScriptRoot\ms-digitalfixworkorders\seed-workorders.sql"
}

Write-Host "Inyección completada exitosamente." -ForegroundColor Green
