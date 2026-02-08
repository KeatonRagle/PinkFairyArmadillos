#!/usr/bin/env pwsh
$ErrorActionPreference = "Stop"
$Required = @(
  "MYSQL_DATABASE",
  "MYSQL_USER",
  "MYSQL_PASSWORD",
  "MYSQL_ROOT_PASSWORD"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot  = (Resolve-Path (Join-Path $ScriptDir "..")).Path
$EnvFile   = Join-Path $RepoRoot ".env"

if (-not (Test-Path $EnvFile)) {
  Write-Error ".env file not found at $EnvFile"
  exit 1
}

$EnvText = Get-Content $EnvFile -Raw

# check for docker compose v2
try {
    docker compose version | Out-Null
} catch {
    Write-Error "Docker Compose v2 required (docker compose)"
    exit 1
}

# get missing keys 
$Missing = @()
foreach ($key in $Required) {
  if ($EnvText -notmatch "(?m)^\s*(?!#)\s*$key\s*=") {
    $Missing += $key
  }
}

# print if .env is missing
if ($Missing.Count -gt 0) {
  Write-Error "Your .env is missing: $($Missing -join ', ')"
  exit 1
}

# run compose (explicit env file + explicit compose files)
try {
  docker compose `
    --env-file "$EnvFile" `
    -f (Join-Path $RepoRoot "compose.yml") `
    -f (Join-Path $RepoRoot "compose.local.yml") `
    up -d --build
} finally {
  Pop-Location
}