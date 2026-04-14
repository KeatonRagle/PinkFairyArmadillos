#!/usr/bin/env bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════╗"
echo "║        PFA Backup Tool           ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Preflight ─────────────────────────────────────────────────
docker compose version >/dev/null 2>&1 || {
  echo -e "${RED}Docker Compose v2 required (docker compose)${NC}"
  exit 1
}

# ── Resolve Paths ─────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# ── Load .env ─────────────────────────────────────────────────
ENV_FILE="${REPO_ROOT}/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo -e "${RED}.env not found at ${ENV_FILE}${NC}"
  exit 1
fi

set -a
source <(grep -E '^MYSQL_' "$ENV_FILE")
set +a

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is not set}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE is not set}"

echo -e "Credentials: ${CYAN}${ENV_FILE}${NC}"

# ── Check Running ─────────────────────────────────────────────
echo -e "Checking stack status..."

RUNNING_DB=$(docker compose ps --status running --quiet db 2>/dev/null)
if [[ -z "$RUNNING_DB" ]]; then
  echo -e "${RED}db container is not running.${NC}"
  echo -e "Start it first: ${CYAN}docker compose up -d db${NC}"
  exit 1
fi

echo -e "${GREEN}✔ DB is running${NC}\n"

# ── Backup ────────────────────────────────────────────────────
echo -e "${CYAN}Running backup...${NC}\n"

START=$(date +%s)

docker compose exec -T -e "MYSQL_PWD=${MYSQL_ROOT_PASSWORD}" db sh -lc '
  FILENAME="/backups/pfa_$(date +%F_%H%M%S).sql.gz"
  mysqldump -u root \
    --single-transaction --routines --triggers \
    "$MYSQL_DATABASE" | gzip > "$FILENAME"
  echo "$FILENAME"
' | while IFS= read -r line; do
  echo -e "  File: ${CYAN}${line}${NC}"
done

ELAPSED=$(( $(date +%s) - START ))

echo ""
echo -e "${GREEN}${BOLD}✔ Backup complete!${NC}"
echo -e "  Duration: ${ELAPSED}s"
