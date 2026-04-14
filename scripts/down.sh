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
echo "║         PFA Shutdown Tool        ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Preflight ─────────────────────────────────────────────────
docker compose version >/dev/null 2>&1 || {
  echo -e "${RED}Docker Compose v2 required (docker compose)${NC}"
  exit 1
}

# ── Check Running ─────────────────────────────────────────────
echo -e "Checking stack status..."

RUNNING_BACKEND=$(docker compose ps --status running --quiet backend 2>/dev/null)
RUNNING_DB=$(docker compose ps --status running --quiet db 2>/dev/null)

if [[ -z "$RUNNING_BACKEND" ]] || [[ -z "$RUNNING_DB" ]]; then
  echo -e "${YELLOW}⚠ PFA is already down.${NC}"
  exit 1
fi

echo -e "${GREEN}✔ Stack is running${NC}\n"

# ── Confirm ───────────────────────────────────────────────────
read -rp "$(echo -e ${YELLOW}Bring down the PFA stack? [y/N]:${NC} )" confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
  echo -e "${CYAN}Aborted.${NC}"
  exit 0
fi

# ── Shutdown ──────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

echo ""
echo -e "${CYAN}Bringing down stack...${NC}\n"

docker compose down

echo ""
echo -e "${GREEN}${BOLD}✔ PFA is down.${NC}"
