#!/bin/bash
# ============================================================
# PFA Add Admin Script
# Place in: <repo>/scripts/add_admin.sh
# Usage:    ./scripts/add_admin.sh
# ============================================================

set -euo pipefail

docker compose version >/dev/null 2>&1 || {
  echo "Docker Compose v2 required (docker compose)"
  exit 1
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

BASE_URL="http://localhost:8080/api"
ADMIN_NAME="admin"
ADMIN_EMAIL="admin@pfa.com"

RUNNING=$(docker compose ps --status running --quiet backend 2>/dev/null)
if [ -z "$RUNNING" ]; then
    echo "PFA is not running"
    exit 1
fi

echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════╗"
echo "║       PFA Add Admin Tool         ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Get password ─────────────────────────────────────────────
read -rsp "Admin password: " ADMIN_PASSWORD
echo ""
echo ""

# ── Load .env for DB creds ────────────────────────────────────
ENV_FILE="${REPO_ROOT}/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo -e "${RED}.env not found at ${ENV_FILE}${NC}"
  exit 1
fi

set -a
source <(grep -E '^MYSQL_' "$ENV_FILE")
set +a

: "${MYSQL_USER:?MYSQL_USER is not set}"
: "${MYSQL_PASSWORD:?MYSQL_PASSWORD is not set}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE is not set}"

# ── Find DB container ─────────────────────────────────────────
DB_CONTAINER=$(docker ps \
  --filter "label=com.docker.compose.service=db" \
  --format "{{.Names}}" | head -1 || true)

if [[ -z "$DB_CONTAINER" ]]; then
  echo -e "${RED}No running db container found.${NC}"
  exit 1
fi

# ── Try register ──────────────────────────────────────────────
echo -e "Registering ${CYAN}${ADMIN_EMAIL}${NC}..."

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "${BASE_URL}/users/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"${ADMIN_NAME}\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}")

if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "201" ]]; then
  echo -e "${GREEN}✔ Registered successfully (HTTP ${HTTP_CODE})${NC}"
elif [[ "$HTTP_CODE" == "409" ]]; then
  echo -e "${YELLOW}⚠ User already exists (HTTP 409) — skipping registration${NC}"
else
  echo -e "${RED}Registration failed (HTTP ${HTTP_CODE})${NC}"
  exit 1
fi

# ── Promote to ROLE_ADMIN ─────────────────────────────────────
echo -e "Promoting to ${CYAN}ROLE_ADMIN${NC}..."

docker compose exec -T -e "MYSQL_PWD=${MYSQL_PASSWORD}" db mysql \
  -u"${MYSQL_USER}" \
  -D "${MYSQL_DATABASE}" \
  -e "UPDATE user SET role = 'ROLE_ADMIN' WHERE email = '${ADMIN_EMAIL}';"

ROWS=$(docker compose exec -T -e "MYSQL_PWD=${MYSQL_PASSWORD}" db mysql \
  -u"${MYSQL_USER}" \
  -D "${MYSQL_DATABASE}" \
  -sNe "SELECT COUNT(*) FROM user WHERE email = '${ADMIN_EMAIL}' AND role = 'ROLE_ADMIN';")

if [[ "$ROWS" == "1" ]]; then
  echo -e "${GREEN}${BOLD}✔ Admin created!${NC}"
  echo -e "  Email:    ${ADMIN_EMAIL}"
  echo -e "  Role:     ROLE_ADMIN"
else
  echo -e "${RED}Role update failed — user may not exist in DB.${NC}"
  exit 1
fi
