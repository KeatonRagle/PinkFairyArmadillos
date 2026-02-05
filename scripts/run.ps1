#!/usr/bin/env pwsh
$ErrorActionPreference = "Stop"
$Required = @("MYSQL_DATABASE","MYSQL_USER","MYSQL_PASSWORD","MYSQL_ROOT_PASSWORD")
$EnvText = Get-Content $EnvFile -Raw
$Missing = @()

# check for docker compose v2
try {
    docker compose version | Out-Null
} catch {
    Write-Error "Docker Compose v2 required (docker compose)"
    exit 1
}

# print the keys missing
foreach ($key in $Required) {
  if ($EnvText -notmatch "(?m)^\s*$key\s*=") { $Missing += $key }
}

# print if .env is missing
if ($Missing.Count -gt 0) {
  Write-Error "Your .env is missing: $($Missing -join ', ')"
  exit 1
}

# resolve script directory and repo root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = (Resolve-Path (Join-Path $ScriptDir "..")).Path
Set-Location $RepoRoot

# pick env file
$EnvFile = Join-Path $RepoRoot ".env"

# run compose (explicit env file + explicit compose files)
docker compose `
  --env-file "$EnvFile" `
  -f (Join-Path $RepoRoot "compose.yml") `
  -f (Join-Path $RepoRoot "compose.local.yml") `
  up -d --build
