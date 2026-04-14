#!/bin/bash
# ============================================================
# PFA Database Restore Script
# Place in: <repo>/scripts/pfa-restore.sh
# Usage:    ./scripts/pfa-restore.sh [--env dev|prod]
# ============================================================

set -euo pipefail

docker compose version >/dev/null 2>&1 || {
  echo "Docker Compose v2 required (docker compose)"
  exit 1
}

# ── Resolve Paths ────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# ── Colors ───────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Defaults ─────────────────────────────────────────────────
PROD_BACKUP_DIR="/data/pfa_db/backups"
DEV_BACKUP_DIR="${REPO_ROOT}/backups"
ENV=""

# ── Argument Parsing ─────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --env) ENV="$2"; shift 2 ;;
    *) echo -e "${RED}Unknown argument: $1${NC}"; exit 1 ;;
  esac
done

# ── Banner ───────────────────────────────────────────────────
echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════╗"
echo "║     PFA Database Restore Tool    ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Environment Selection ─────────────────────────────────────
if [[ -z "$ENV" ]]; then
  echo -e "Select environment:"
  echo -e "  ${CYAN}1)${NC} dev"
  echo -e "  ${CYAN}2)${NC} prod"
  read -rp "Choice [1/2]: " env_choice
  case "$env_choice" in
    1) ENV="dev"  ;;
    2) ENV="prod" ;;
    *) echo -e "${RED}Invalid choice.${NC}"; exit 1 ;;
  esac
fi

echo -e "Environment: ${BOLD}${GREEN}${ENV}${NC}\n"

# ── Resolve Backup Dir ────────────────────────────────────────
if [[ "$ENV" == "prod" ]]; then
  BACKUP_DIR="$PROD_BACKUP_DIR"
else
  BACKUP_DIR="$DEV_BACKUP_DIR"
fi

if [[ ! -d "$BACKUP_DIR" ]]; then
  echo -e "${RED}Backup directory not found: $BACKUP_DIR${NC}"
  exit 1
fi

# ── Load .env ────────────────────────────────────────────────
if [[ "$ENV" == "prod" ]]; then
  ENV_SEARCH_DIRS=("$(pwd)" "/data/pfa_db")
else
  ENV_SEARCH_DIRS=("$REPO_ROOT")
fi

ENV_FILE=""
for dir in "${ENV_SEARCH_DIRS[@]}"; do
  if [[ -f "${dir}/.env" ]]; then
    ENV_FILE="${dir}/.env"
    break
  fi
done

if [[ -z "$ENV_FILE" ]]; then
  echo -e "${RED}.env file not found.${NC}"
  echo -e "Searched: ${ENV_SEARCH_DIRS[*]}"
  echo -e "Enter credentials manually:\n"
  read -rp  "MYSQL_DATABASE: " MYSQL_DATABASE
  read -rp  "MYSQL_USER: "     MYSQL_USER
  read -rsp "MYSQL_PASSWORD: " MYSQL_PASSWORD
  echo ""
else
  echo -e "Credentials: ${CYAN}${ENV_FILE}${NC}"
  set -a
  # shellcheck source=/dev/null
  source <(grep -E '^MYSQL_' "$ENV_FILE")
  set +a
fi

: "${MYSQL_DATABASE:?MYSQL_DATABASE is not set}"
: "${MYSQL_USER:?MYSQL_USER is not set}"
: "${MYSQL_PASSWORD:?MYSQL_PASSWORD is not set}"

# ── Find Running DB Container ─────────────────────────────────
echo ""
echo -e "Looking for running ${BOLD}db${NC} container..."

# Primary: match on compose service label
DB_CONTAINER=$(docker ps \
  --filter "label=com.docker.compose.service=db" \
  --format "{{.Names}}" | head -1 || true)

# Fallback: name grep
if [[ -z "$DB_CONTAINER" ]]; then
  DB_CONTAINER=$(docker ps --format "{{.Names}}" | grep -i '\bdb\b' | head -1 || true)
fi

if [[ -z "$DB_CONTAINER" ]]; then
  echo -e "${RED}No running 'db' container found.${NC}"
  echo -e "Start it first: ${CYAN}docker compose up -d db${NC}"
  exit 1
fi

echo -e "Container: ${GREEN}${DB_CONTAINER}${NC}"

# ── List Last 10 Backups ──────────────────────────────────────
echo ""
echo -e "${BOLD}Available backups (newest first):${NC}"
echo -e "Directory: ${CYAN}${BACKUP_DIR}${NC}\n"

mapfile -t BACKUPS < <(find "$BACKUP_DIR" -maxdepth 1 -name "*.sql.gz" -printf "%T@ %p\n" \
  | sort -rn | head -10 | awk '{print $2}')

if [[ ${#BACKUPS[@]} -eq 0 ]]; then
  echo -e "${RED}No .sql.gz backups found in ${BACKUP_DIR}${NC}"
  exit 1
fi

for i in "${!BACKUPS[@]}"; do
  FNAME=$(basename "${BACKUPS[$i]}")
  FSIZE=$(du -h "${BACKUPS[$i]}" | cut -f1)
  if stat --version &>/dev/null 2>&1; then
    FDATE=$(stat -c '%y' "${BACKUPS[$i]}" | cut -d'.' -f1)
  else
    FDATE=$(stat -f '%Sm' -t '%Y-%m-%d %H:%M:%S' "${BACKUPS[$i]}")
  fi
  printf "  ${CYAN}%2d)${NC}  %-45s ${YELLOW}%6s${NC}  %s\n" \
    "$((i+1))" "$FNAME" "$FSIZE" "$FDATE"
done

# ── Select Backup ─────────────────────────────────────────────
echo ""
read -rp "Select backup [1-${#BACKUPS[@]}]: " backup_choice

if ! [[ "$backup_choice" =~ ^[0-9]+$ ]] \
    || [[ "$backup_choice" -lt 1 ]] \
    || [[ "$backup_choice" -gt "${#BACKUPS[@]}" ]]; then
  echo -e "${RED}Invalid selection.${NC}"
  exit 1
fi

SELECTED="${BACKUPS[$((backup_choice-1))]}"
echo -e "\nSelected: ${GREEN}$(basename "$SELECTED")${NC}"

# ── Confirm ───────────────────────────────────────────────────
echo ""
echo -e "${YELLOW}${BOLD}⚠  WARNING${NC}${YELLOW}: This will OVERWRITE '${MYSQL_DATABASE}'"
echo -e "   in container '${DB_CONTAINER}' (${ENV}).${NC}"
echo ""
read -rp "Type 'yes' to confirm: " confirm

if [[ "$confirm" != "yes" ]]; then
  echo -e "${CYAN}Aborted. No changes made.${NC}"
  exit 0
fi

# ── Wait for MySQL to be ready ────────────────────────────────
echo -e "${CYAN}Waiting for MySQL to be ready...${NC}"
RETRIES=30
until docker exec "$DB_CONTAINER" mysqladmin ping \
  -u"${MYSQL_USER}" \
  -p"${MYSQL_PASSWORD}" \
  --silent 2>/dev/null; do
  RETRIES=$((RETRIES - 1))
  if [[ $RETRIES -eq 0 ]]; then
    echo -e "${RED}MySQL did not become ready in time.${NC}"
    exit 1
  fi
  echo -e "  waiting... (${RETRIES} retries left)"
  sleep 2
done
echo -e "${GREEN}MySQL is ready${NC}\n"

# ── Restore ───────────────────────────────────────────────────
echo -e "${CYAN}Decompressing and restoring...${NC}"

START=$(date +%s)

if ! gunzip < "$SELECTED" | docker compose exec -T db mysql \
  -u"${MYSQL_USER}" \
  -p"${MYSQL_PASSWORD}" \
  -D "${MYSQL_DATABASE}"; then
  echo -e "${RED}Restore failed — check MySQL logs.${NC}"
  exit 1
fi

ELAPSED=$(( $(date +%s) - START ))

echo ""
echo -e "${GREEN}${BOLD}✔ Restore complete!${NC}"
echo -e "  Backup:    $(basename "$SELECTED")"
echo -e "  Database:  ${MYSQL_DATABASE}"
echo -e "  Container: ${DB_CONTAINER}"
echo -e "  Duration:  ${ELAPSED}s"
