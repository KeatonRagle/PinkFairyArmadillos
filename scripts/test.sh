#!/usr/bin/env bash
set -euo pipefail

docker compose version >/dev/null 2>&1 || {
  echo "Docker Compose v2 required (docker compose)"
  exit 1
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/../backend" && pwd)"
cd "$BACKEND_DIR"

./mvnw clean test -e
