# ================================================================
# BUILD Y PUSH - SANOS Y SALVOS
# ================================================================
# Build y push de imagenes Docker a ECR.
# El login a ECR se hace manualmente (problema de pipes en Windows).
# ================================================================

param(
    [string]$AccountID
)

$ErrorActionPreference = "Stop"

$REGION = "us-east-1"

# Detectar Account ID si no se proporciono
if (-not $AccountID) {
    Write-Host "Detectando Account ID desde AWS..." -ForegroundColor Gray
    $AccountID = aws sts get-caller-identity --query Account --output text 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $AccountID) {
        Write-Host "[ERROR] No se pudo obtener el Account ID." -ForegroundColor Red
        exit 1
    }
}

$REPO_BASE = "$AccountID.dkr.ecr.$REGION.amazonaws.com"

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "  BUILD Y PUSH - SANOS Y SALVOS" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Account ID: $AccountID" -ForegroundColor Green
Write-Host "  Region:     $REGION" -ForegroundColor Green
Write-Host "  Registry:   $REPO_BASE" -ForegroundColor Green
Write-Host ""

# Paso manual de login
Write-Host "Paso 0: Login a AWS ECR (MANUAL)" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. Ejecuta este comando en esta misma terminal:" -ForegroundColor Yellow
Write-Host ""
Write-Host "     aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REPO_BASE" -ForegroundColor Cyan
Write-Host ""
Write-Host "  2. Deberias ver 'Login Succeeded'" -ForegroundColor Yellow
Write-Host ""

# Lista de servicios
$servicios = @(
    @{ Name = "auth-service";         Dir = "auth-service" }
    @{ Name = "bff-service";          Dir = "bff-service" }
    @{ Name = "ms-mascotas";          Dir = "ms-mascotas" }
    @{ Name = "ms-geolocalizacion";   Dir = "ms-geolocalizacion" }
    @{ Name = "ms-coincidencias";     Dir = "ms-coincidencias" }
)

$failed = @()

foreach ($svc in $servicios) {
    $nombre = $svc.Name
    $dir = $svc.Dir

    Write-Host ""
    Write-Host "=========================================================" -ForegroundColor Cyan
    Write-Host "  $nombre" -ForegroundColor Cyan
    Write-Host "=========================================================" -ForegroundColor Cyan

    # Build
    Write-Host ""
    Write-Host "  docker build --platform linux/amd64 --provenance=false -t sanos-y-salvos/$nombre -f $dir/Dockerfile ." -ForegroundColor Gray
    docker build --platform linux/amd64 --provenance=false -t "sanos-y-salvos/$nombre" -f "$dir/Dockerfile" .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERROR] Build fallo para $nombre" -ForegroundColor Red
        $failed += $nombre
        continue
    }
    Write-Host "  [OK] Build exitoso." -ForegroundColor Green

    # Tag
    Write-Host ""
    Write-Host "  docker tag sanos-y-salvos/$nombre`:latest $REPO_BASE/sanos-y-salvos/$nombre`:latest" -ForegroundColor Gray
    docker tag "sanos-y-salvos/$nombre`:latest" "$REPO_BASE/sanos-y-salvos/$nombre`:latest"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERROR] Tag fallo para $nombre" -ForegroundColor Red
        $failed += $nombre
        continue
    }
    Write-Host "  [OK] Tag exitoso." -ForegroundColor Green

    # Push
    Write-Host ""
    Write-Host "  docker push $REPO_BASE/sanos-y-salvos/$nombre`:latest" -ForegroundColor Gray
    docker push "$REPO_BASE/sanos-y-salvos/$nombre`:latest"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERROR] Push fallo para $nombre" -ForegroundColor Red
        $failed += $nombre
        continue
    }
    Write-Host "  [OK] Push exitoso." -ForegroundColor Green
}

# Resumen
Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan
if ($failed.Count -eq 0) {
    Write-Host "  TODAS LAS IMAGENES SUBIDAS EXITOSAMENTE" -ForegroundColor Green
} else {
    Write-Host "  SERVICIOS CON ERROR: $($failed -join ', ')" -ForegroundColor Red
}
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""

if ($failed.Count -gt 0) { exit 1 }
