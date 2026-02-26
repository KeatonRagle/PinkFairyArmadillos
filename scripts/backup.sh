#!/usr/bin/env bash
set -euo pipefail

docker compose version >/dev/null 2>&1 || {
  echo "Docker Compose v2 required (docker compose)"
  exit 1
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

docker compose exec -T db sh -lc '
mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" \
  --single-transaction --routines --triggers \
  "$MYSQL_DATABASE" | gzip > "/backups/pfa_$(date +%F_%H%M%S).sql.gz"
'
